package market.engine.fragments.root.main.favPages

import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.Tab
import market.engine.core.data.states.MenuData
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.dialogs.CustomDialogState
import market.engine.widgets.tooltip.TooltipData
import org.jetbrains.compose.resources.getString

data class FavPagesState(
    val appState : SimpleAppBarData = SimpleAppBarData(),
    val isDragMode: Boolean = false
)

class FavPagesViewModel(val component: FavPagesComponent) : CoreViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val _favoritesTabList = MutableStateFlow(emptyList<Tab>())
    val favoritesTabList = _favoritesTabList.asStateFlow()

    private val _initPosition = MutableStateFlow(0)
    val initPosition = _initPosition.asStateFlow()

    private val _isDragMode = MutableStateFlow(false)

    private val _isMenuVisible = MutableStateFlow(false)

    private val _customDialogState = MutableStateFlow(CustomDialogState())
    val customDialogState = _customDialogState.asStateFlow()

    val favPagesState : StateFlow<FavPagesState> = combine(
        _favoritesTabList,
        _initPosition,
        _isDragMode,
        _isMenuVisible
    )
    { favTabList, currentTab, isDragMode, isMenuVisible ->

        val isVisibleMenu = if(favTabList.isNotEmpty() && favTabList.size > currentTab)
            favTabList[currentTab].id > 1000 else false

        val listItems = listOf(
            NavigationItem(
                title = "",
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                onClick = {
                    getFavTabList()
                }
            ),
            NavigationItem(
                title = getString(strings.createNewOffersListLabel),
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
                isVisible = !isDragMode,
                badgeCount = null,
                onClick = {
                    makeOperation("create_blank_offer_list", UserData.login)
                }
            ),
            NavigationItem(
                title = getString(strings.menuTitle),
                icon = drawables.menuIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                isVisible = isVisibleMenu && !isDragMode,
                badgeCount = null,
                onClick = {
                    _isMenuVisible.value = true
                }
            ),
        )


        FavPagesState(
            appState = SimpleAppBarData(
                menuData = MenuData(
                    isMenuVisible = isMenuVisible,
                    menuItems = if(isVisibleMenu)
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

            withContext(Dispatchers.Main) {
                val res = data.success
                val buf = arrayListOf<FavoriteListItem>()
                buf.addAll(res ?: emptyList())

                newList.addAll(buf)

                val listPosition = db.favoritesTabListItemQueries
                val lh = listPosition.selectAll(UserData.login).executeAsList()
                lh.forEach { favoritesTabListItem ->
                    newList.find { it.id == favoritesTabListItem.itemId }?.position =
                        favoritesTabListItem.position.toInt()
                }

                newList.sortBy { it.position }
                newList.sortBy { !it.markedAsPrimary }

                if (newList != _favoritesTabList.value) {
                    _favoritesTabList.value = newList.map {
                        Tab(
                            id = it.id,
                            title = it.title ?: "",
                            image = it.images.firstOrNull(),
                            isPined = it.markedAsPrimary,
                            onClick = {
                                selectPage(newList.indexOf(it))
                            },
                        )
                    }

                    component.updateNavigationPages()
                }
            }
        }
    }

    fun updateFavTabList(list: List<Tab>){
        viewModelScope.launch {
            try {
                _favoritesTabList.value = list

                withContext(Dispatchers.Unconfined) {
                    list.forEachIndexed { index, it ->
                        db.favoritesTabListItemQueries.insertEntry(
                            itemId = it.id,
                            owner = UserData.login,
                            position = index.toLong()
                        )
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
                        title = AnnotatedString(t),
                        fields = f,
                        onDismiss = {
                            closeDialog()
                        },
                        onSuccessful = {
                            postOperation(UserData.login, type,"users")
                            _initPosition.value = favoritesTabList.value.lastIndex +1
                        }
                    )
                }
            }

            "copy_offers_list", "rename_offers_list" -> {
                getOperationFields(id, type, "offers_lists") { t, f ->
                    _customDialogState.value = CustomDialogState(
                        typeDialog = type,
                        title = AnnotatedString(t),
                        fields = f,
                        onDismiss = {
                            closeDialog()
                        },
                        onSuccessful = {
                            postOperation(id, type,"offers_lists")
                        }
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
                        _favoritesTabList.update { tabs ->
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
