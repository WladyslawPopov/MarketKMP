package market.engine.fragments.root.main.favPages

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.MenuData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.Tab
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.dialogs.CustomDialogState
import market.engine.widgets.tooltip.TooltipData
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin


data class FavPagesState(
    val appState : SimpleAppBarData = SimpleAppBarData(),
    val isDragMode: Boolean = false
)

class FavPagesViewModel(val component: FavPagesComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    private val offersListOperations : OffersListOperations = getKoin().get()

    private val _tabsDataList = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "favoritesTabList",
        emptyList(),
        ListSerializer(FavoriteListItem.serializer())
    )

    val favoritesTabList = _tabsDataList.state.map { listItems ->
        listItems.map { item ->
            Tab(
                id = item.id,
                title = item.title ?: "",
                image = item.images.firstOrNull(),
                isPined = item.markedAsPrimary,
                onClick = {
                    selectPage(listItems.indexOf(item))
                }
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val _initPosition = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "initPosition",
        0,
        Int.serializer()
    )
    val initPosition = _initPosition.state

    private val _customDialogState = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "customDialogState",
        CustomDialogState(),
        CustomDialogState.serializer()
    )
    val customDialogState = _customDialogState.state

    private val _isDragMode = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "isDragMode",
        false,
        Boolean.serializer()
    )

    private val _isMenuVisible = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "isMenuVisible",
        false,
        Boolean.serializer()
    )

    val favPagesState : StateFlow<FavPagesState> = combine(
        _tabsDataList.state,
        _initPosition.state,
        _isDragMode.state,
        _isMenuVisible.state
    )
    { favTabList, currentTab, isDragMode, isMenuVisible ->

        val isVisibleMenu = if(favTabList.isNotEmpty() && favTabList.size > currentTab)
            favTabList[currentTab].id > 1000 else false

        val listItems = listOf(
            NavigationItem(
                title = "",
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                onClick = {
                    getFavTabList()
                }
            ),
            NavigationItem(
                title = getString(strings.createNewOffersListLabel),
                isVisible = !isDragMode,
                badgeCount = null,
                icon = drawables.addFolderIcon,
                tint = colors.steelBlue,
                tooltipData =
                    if (settings.getSettingValue("create_blank_offer_list_notify_badge", true) == true) {
                        TooltipData(
                            title = "",
                            subtitle = getString(strings.createOfferListTooltipDescription),
                            dismissIcon = drawables.cancelIcon
                        )
                    }else{
                        null
                    },
                onClick = {
                    makeOperation("create_blank_offer_list", UserData.login)
                }
            ),
            NavigationItem(
                title = getString(strings.menuTitle),
                hasNews = false,
                isVisible = isVisibleMenu && !isDragMode,
                badgeCount = null,
                icon = drawables.menuIcon,
                tint = colors.inactiveBottomNavIconColor,
                onClick = {
                    _isMenuVisible.value = true
                }
            )
        )

        FavPagesState(
            appState = SimpleAppBarData(
                menuData = MenuData(
                    isMenuVisible = isMenuVisible,
                    menuItems = if (isVisibleMenu)
                        getOperationFavTab(favTabList[currentTab].id)
                    else getDefOperationFavTab(),
                    closeMenu = {
                        _isMenuVisible.value = false
                    }
                ),
                listItems = listItems,
            ),
            isDragMode = isDragMode
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        FavPagesState()
    )

    init {
        getFavTabList()

        viewModelScope.launch {
            favoritesTabList.collect { updatedList ->
                withContext(Dispatchers.Main){
                    component.updateNavigationPages()
                }
            }
        }
    }

    fun getFavTabList() {
        viewModelScope.launch {
            val newList = arrayListOf(
                FavoriteListItem(
                    id = 111,
                    title = getString(strings.myFavoritesTitle),
                    owner = UserData.login,
                    position = 0
                ),
                FavoriteListItem(
                    id = 222,
                    title = getString(strings.mySubscribedTitle),
                    owner = UserData.login,
                    position = 1
                ),
                FavoriteListItem(
                    id = 333,
                    title = getString(strings.myNotesTitle),
                    owner = UserData.login,
                    position = 2
                )
            )

            val data = withContext(Dispatchers.IO) {
                offersListOperations.getOffersList()
            }

            val positionsFromDb = mutex.withLock {
                db.favoritesTabListItemQueries.selectAll(UserData.login).executeAsList()
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                val buf = mutableListOf<FavoriteListItem>()
                buf.addAll(res ?: emptyList())

                newList.addAll(buf)

                positionsFromDb.forEach { favoritesTabListItem ->
                    newList.find { it.id == favoritesTabListItem.itemId }?.position =
                        favoritesTabListItem.position.toInt()
                }

                newList.sortBy { it.position }
                newList.sortBy { !it.markedAsPrimary }

                if (newList != _tabsDataList.value) {
                    _tabsDataList.value = newList.toList()
                }
            }
        }
    }

    fun updateFavTabList(list: List<Tab>){
        viewModelScope.launch {
            try {
                _tabsDataList.value = list.map { item ->
                    FavoriteListItem(
                        id = item.id,
                        title = item.title,
                        images = item.image?.let { listOf(it) } ?: emptyList(),
                        markedAsPrimary = item.isPined,
                    )
                }

                mutex.withLock {
                    db.transaction {
                        list.forEachIndexed { index, it ->
                            db.favoritesTabListItemQueries.insertEntry(
                                itemId = it.id,
                                owner = UserData.login,
                                position = index.toLong()
                            )
                        }
                    }
                }
            }  catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    suspend fun getDefOperationFavTab() : List<MenuItem>{
        return listOf(
            MenuItem(
                icon = drawables.reorderIcon,
                id = "reorder",
                title = getString(strings.reorderTabLabel),
                onClick = {
                    makeOperation("reorder", 1L)
                }
            )
        )
    }

    suspend fun getOperationFavTab(id: Long) : List<MenuItem> {

        val data = withContext(Dispatchers.IO) { offersListOperations.getOperations(id) }

        return withContext(Dispatchers.Main) {
            val res = data.success

            val defReorderMenuItem = MenuItem(
                icon = drawables.reorderIcon,
                id = "reorder",
                title = getString(strings.reorderTabLabel),
                onClick = {
                    makeOperation("reorder", 1L)
                }
            )

            return@withContext buildList {
                addAll(
                    listOf(
                        defReorderMenuItem
                    )
                )
                if(res?.isNotEmpty() == true) {
                    addAll(
                        res.map {
                            MenuItem(
                                id = it.id ?: "",
                                title = it.name ?: "",
                                onClick = {
                                    makeOperation(it.id ?: "", id)
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    fun makeOperation(type: String, id: Long){
        when (type) {
            "create_blank_offer_list" -> {
                getOperationFields(
                    UserData.login,
                    type,
                    "users"
                ) { t, f ->
                    _customDialogState.value = CustomDialogState(
                        typeDialog = type,
                        title = t,
                        fields = f
                    )
                }
            }

            "copy_offers_list", "rename_offers_list" -> {
                getOperationFields(id, type, "offers_lists") { t, f ->
                    _customDialogState.value = CustomDialogState(
                        typeDialog = type,
                        title = t,
                        fields = f
                    )
                }
            }

            "reorder" -> {
                _isDragMode.value = true

                analyticsHelper.reportEvent(
                    "reorder_offers_list", mapOf(
                        "list_id" to id
                    )
                )
            }

            "delete_offers_list" -> {
                postOperationFields(
                    id,
                    type,
                    "offers_lists",
                    onSuccess = {
                        _tabsDataList.update { tabs ->
                            tabs.filter { it.id != id }
                        }
                        getFavTabList()
                        db.favoritesTabListItemQueries.deleteById(
                            itemId = id,
                            owner = UserData.login
                        )
                    },
                    errorCallback = {}
                )
            }

            "mark_as_primary_offers_list", "unmark_as_primary_offers_list" -> {
                postOperationFields(
                    id,
                    type,
                    "offers_lists",
                    onSuccess = {
                        getFavTabList()
                    },
                    errorCallback = {}
                )
            }

            else -> {

            }
        }
    }

    fun onClickOperation(type: String, id: Long){
        when(type){
            "create_blank_offer_list"->{
                postOperation(UserData.login, type,"users")
                _initPosition.value = favoritesTabList.value.lastIndex +1
            }
            "copy_offers_list", "rename_offers_list" -> {
                postOperation(id, type,"offers_lists")
            }
        }
    }

    fun postOperation(id: Long, type: String, method: String){
        val bodyPost = HashMap<String, JsonElement>()
        _customDialogState.value.fields.forEach { field ->
            if (field.data != null) {
                bodyPost[field.key ?: ""] = field.data!!
            }
        }

        postOperationFields(
            id,
            type,
            method,
            bodyPost,
            onSuccess = {
                closeDialog()
                getFavTabList()
            },
            errorCallback = { f ->
                if (f != null) {
                    _customDialogState.update {
                        it.copy(
                            fields = f
                        )
                    }
                } else {
                    closeDialog()
                }
            }
        )
    }

    fun closeDialog() {
        _customDialogState.value = CustomDialogState()
    }

    fun closeDragMode(){
        _isDragMode.value = false
    }

    fun selectPage(page: Int){
        _initPosition.value = page
        component.selectPage(page)
    }

    fun setNewField(field: Fields){
        _customDialogState.update { dialog->
            dialog.copy(
                fields = dialog.fields.map {
                    if(it.key == field.key){
                        field.copy()
                    }else{
                        it.copy()
                    }
                }
            )
        }
    }
}
