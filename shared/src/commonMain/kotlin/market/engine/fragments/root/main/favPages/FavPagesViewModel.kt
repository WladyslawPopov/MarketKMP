package market.engine.fragments.root.main.favPages

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.tooltip.TooltipData
import org.jetbrains.compose.resources.getString


data class FavPagesState(
    val appState : SimpleAppBarData = SimpleAppBarData(),
    val favTabList: List<FavoriteListItem> = emptyList(),
    val initPosition: Int = 0,
    val isDragMode: Boolean = false
)


class FavPagesViewModel(val fullRefresh: () -> Unit) : BaseViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val _favoritesTabList = MutableStateFlow(emptyList<FavoriteListItem>())
    val favoritesTabList = _favoritesTabList.asStateFlow()

    private val _initPosition = MutableStateFlow(0)
    val initPosition = _initPosition.asStateFlow()

    private val _isDragMode = MutableStateFlow(false)

    val showCreatedDialog = mutableStateOf("")

    val method = mutableStateOf("offers_lists")

    val titleDialog = MutableStateFlow(AnnotatedString(""))
    val fieldsDialog = MutableStateFlow<List<Fields>>(emptyList())
    val dialogItemId = MutableStateFlow(1L)

    private val _menuItems = MutableStateFlow(
        listOf<MenuItem>()
    )

    val favPagesState : StateFlow<FavPagesState> = combine(
        _favoritesTabList,
        _initPosition,
        _isDragMode,
        _menuItems
    ) { favTabList, currentTab, isDragMode, menuItems ->

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
                    fullRefresh()
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
                    getOperationFavTab(favTabList[currentTab].id){ menuItems ->
                       _menuItems.value = menuItems
                    }
                }
            ),
        )

        FavPagesState(
            appState = SimpleAppBarData(
                menuItems = menuItems,
                listItems = listItems,
                closeMenu = {
                    _menuItems.value = emptyList()
                }
            ),
            favTabList = favTabList,
            initPosition = currentTab,
            isDragMode = isDragMode
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        FavPagesState()
    )

    fun getFavTabList(onSuccess: () -> Unit) {
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
                    _favoritesTabList.value = newList
                }

                onSuccess()
            }
        }
    }

    fun updateFavTabList(list: List<FavoriteListItem>){
        viewModelScope.launch {
            try {
                list.forEachIndexed { index, it ->
                    db.favoritesTabListItemQueries.insertEntry(
                        itemId = it.id,
                        owner = UserData.login,
                        position = index.toLong()
                    )
                }
                analyticsHelper.reportEvent(
                    "update_offers_list", mapOf()
                )

                _favoritesTabList.value = list
            }  catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    fun getOperationFavTab(id: Long, onSuccess: (List<MenuItem>) -> Unit){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOperations(id) }

            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                if (!res.isNullOrEmpty()){
                    val defReorderMenuItem = MenuItem(
                        icon = drawables.reorderIcon,
                        id = "reorder",
                        title = getString(strings.reorderTabLabel),
                        onClick = {
                            makeOperation("reorder", 1L)
                        }
                    )

                    onSuccess(
                        buildList {
                        addAll(
                            listOf(
                                defReorderMenuItem
                            )
                        )
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
                    )
                }else{
                    if (error != null)
                        onError(error)
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
                    titleDialog.value = AnnotatedString(t)
                    fieldsDialog.value = f
                    showCreatedDialog.value = type
                    dialogItemId.value = UserData.login
                    method.value = "users"
                }
            }

            "copy_offers_list", "rename_offers_list" -> {
                getOperationFields(id, type, "offers_lists") { t, f ->
                    titleDialog.value = AnnotatedString(t)
                    fieldsDialog.value = f
                    showCreatedDialog.value = type
                    dialogItemId.value = id
                    method.value = "offers_lists"
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
                        db.favoritesTabListItemQueries.deleteById(
                            itemId = id,
                            owner = UserData.login
                        )
                        _initPosition.value =
                            _initPosition.value.coerceIn(
                                0,
                                _favoritesTabList.value.size - 1
                            )
                        fullRefresh()
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
                        fullRefresh()
                    },
                    errorCallback = {}
                )
            }

            else -> {

            }
        }
    }

    fun postOperation(){
        val bodyPost = HashMap<String, JsonElement>()
        fieldsDialog.value.forEach { field ->
            if (field.data != null) {
                bodyPost[field.key ?: ""] = field.data!!
            }
        }

        postOperationFields(
            dialogItemId.value,
            showCreatedDialog.value,
            method.value,
            bodyPost,
            onSuccess = {
                closeDialog()
                getFavTabList {
                    fullRefresh()
                    _initPosition.value = _favoritesTabList.value.lastIndex + 1
                }
            },
            errorCallback = { f ->
                if (f != null) {
                    fieldsDialog.value = f
                } else {
                    closeDialog()
                }
            }
        )
    }

    fun closeDialog() {
        titleDialog.value = AnnotatedString("")
        fieldsDialog.value = emptyList()
        showCreatedDialog.value = ""
    }

    fun closeDragMode(){
        _isDragMode.value = false
    }

    fun selectPage(page: Int){
        _initPosition.value = page
    }
}
