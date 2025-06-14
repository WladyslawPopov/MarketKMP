package market.engine.fragments.root.main.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.cachedIn
import androidx.paging.map
import app.cash.paging.PagingData
import com.arkivanov.decompose.router.pages.select
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.Platform
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.data.items.Tab
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.shared.SearchHistory
import market.engine.widgets.bars.SimpleAppBarData
import market.engine.widgets.filterContents.categories.CategoryViewModel
import market.engine.widgets.items.FilterListingBtnItem
import market.engine.widgets.items.offer_Items.OfferItemState
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import kotlin.String

data class ListingEvents(
    val onRefresh: () -> Unit = {},
    val changeActiveWindowType : (ActiveWindowType) -> Unit = {},
    val clearListingData: () -> Unit = {},
    val backClick: () -> Unit = {},
    val clickCategory: (complete : Boolean) -> Unit = {},
    val closeFilters: (update : Boolean, clear: Boolean) -> Unit = {_,_ ->},
    val updateItem: (OfferItem?) -> Unit = {},
    val clearError: () -> Unit = {},
)

data class SearchEvents(
    val onRefresh : () -> Unit = {},
    val goToListing: () -> Unit = {},
    val onDeleteHistory: () -> Unit = {},
    val onDeleteHistoryItem: (Long) -> Unit = {},
    val openSearchCategory: (value : Boolean, complete : Boolean) -> Unit = { _, _ -> },
    val clearCategory: () -> Unit = {},
    val clickUser: () -> Unit = {},
    val clearUser: () -> Unit = {},
    val clickUserFinished: () -> Unit = {},
    val onHistoryItemClicked: (SearchHistoryItem) -> Unit = {},
    val editHistoryItem: (SearchHistoryItem) -> Unit = {},
    val onTabSelect : (Int) -> Unit = {}
)

data class CategoryState(
    val openCategory: Boolean = false,
    val categoryViewModel: CategoryViewModel = CategoryViewModel(),
)

data class SearchUiState(
    val openSearch: Boolean = false,
    val searchData: SD = SD(),

    val selectedTabIndex: Int = 0,
    val tabs: List<Tab> = listOf<Tab>(),

    val searchHistory: List<SearchHistoryItem> = emptyList(),

    val appBarData: SimpleAppBarData,
    val closeAppBar: SimpleAppBarData,
    val categoryState: CategoryState = CategoryState(),
    val searchEvents: SearchEvents = SearchEvents(),
)

data class FilterBarUiState(
    val listFiltersButtons: List<FilterListingBtnItem> = emptyList(),
    val listNavigation: List<NavigationItem> = emptyList(),
    val isShowFilters: Boolean = true,
    val isShowGrid: Boolean = false,
)

data class ListingStateContent(
    val appBarData: SimpleAppBarData,
    val listingData: ListingData = ListingData(),
    val regions: List<Options> = emptyList(),

    val filterBarData: FilterBarUiState = FilterBarUiState(),
    val listingCategoryState: CategoryState = CategoryState(),
    val listingBaseState: ListingBaseState = ListingBaseState(),

    val listingEvents: ListingEvents = ListingEvents(),
)

enum class ActiveWindowType {
    SEARCH, FILTERS, SORTING, CATEGORY, LISTING, CATEGORY_FILTERS
}

@OptIn(ExperimentalCoroutinesApi::class)
class ListingViewModel(val component: ListingComponent) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _responseOffersRecommendedInListing = MutableStateFlow<List<OfferItem>?>(null)

    private val _responseHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())

    private val _changeSearchTab = MutableStateFlow<Int>(0)

    private val _listingData = MutableStateFlow(ListingData())

    private val _activeWindowType = MutableStateFlow(ActiveWindowType.LISTING)

    private val searchString = mutableStateOf(TextFieldValue(""))

    val errorString = MutableStateFlow("")

    private val searchCategoryModel = CategoryViewModel(
        isFilters = true,
    )
    private val listingCategoryModel = CategoryViewModel()

    var searchTitle = mutableStateOf("")
    var searchHistory = mutableStateOf("")
    var subTitle = mutableStateOf("")
    val userDef = mutableStateOf("")
    val filterString = mutableStateOf("")
    val sortString = mutableStateOf("")
    val menuString = mutableStateOf("")
    val userString = mutableStateOf("")
    val searchFinishedString = mutableStateOf("")

    private val searchAppBar = object : SimpleAppBarData {
        override val modifier: Modifier
            get() = Modifier
        override val color: Color
            get() = colors.white
        override val content: @Composable () -> Unit
            get() = {
                SearchTextField(
                    _activeWindowType.value == ActiveWindowType.SEARCH,
                    searchString.value,
                    onValueChange = { newVal ->
                        searchString.value = newVal
                        getHistory(newVal.text)
                    },
                    goToListing = {
                        changeOpenSearch(false)
                    },
                    onClearSearch = {
                        clearSearch()
                    }
                )
            }
        override val onBackClick: () -> Unit
            get() = {
                changeOpenSearch(false)
            }
        override val listItems: List<NavigationItem>
            get() = listOf(
                NavigationItem(
                    title = searchTitle.value,
                    icon = drawables.searchIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null,
                    onClick = {
                        changeOpenSearch(false)
                    }
                )
            )
        override val showMenu: MutableState<Boolean>
            get() = mutableStateOf(false)
        override val menuItems: List<MenuItem>
            get() = emptyList()
    }
    private val closeAppBar = object : SimpleAppBarData {
        override val modifier: Modifier
            get() = Modifier
        override val color: Color
            get() = colors.white
        override val content: @Composable () -> Unit
            get() = {}
        override val onBackClick: () -> Unit
            get() = {
                openSearchCategory(false, false)
            }
        override val listItems: List<NavigationItem>
            get() = emptyList()
        override val showMenu: MutableState<Boolean>
            get() = mutableStateOf(false)
        override val menuItems: List<MenuItem>
            get() = emptyList()
    }

    private val searchEvents = SearchEvents(
        onRefresh = {
            setLoading(true)
            onError(ServerErrorException())
            getHistory(searchString.value.text)
            setSearchFilters()
            viewModelScope.launch {
                delay(1000)
                setLoading(false)
            }
        },
        goToListing = {
            changeOpenSearch(false)
        },
        onDeleteHistory = {
            deleteHistory()
        },
        onDeleteHistoryItem = { id ->
            deleteItemHistory(id)
        },
        openSearchCategory = { value, completed ->
            this@ListingViewModel.openSearchCategory(value, completed)
        },
        clearCategory = {
            clearSearchCategory()
        },
        clickUser = {
            selectUserSearch()
        },
        clearUser = {
            clearUserSearch()
        },
        clickUserFinished = {
            selectUserFinished()
        },
        onHistoryItemClicked = { item ->
            this@ListingViewModel.onClickHistoryItem(item)
        },
        editHistoryItem = { item ->
            this@ListingViewModel.editHistoryItem(item)
        },
        onTabSelect = { tab ->
            component.model.value.searchNavigator.select(tab)
            changeSearchTab(tab)
        }
    )

    private val listingEvents = ListingEvents(
        onRefresh = {
            refresh()
        },
        changeActiveWindowType = { type ->
            _activeWindowType.value = type
        },
        clearListingData = {
            _listingData.update { listingData ->
                listingData.searchData.clear(catDef.value)
                listingData.copy(
                    searchData = listingData.searchData,
                    data = listingData.data.copy(
                        filters = ListingFilters.getEmpty()
                    )
                )
            }

            refresh()
        },
        backClick = {
            when {
                _activeWindowType.value == ActiveWindowType.CATEGORY_FILTERS &&
                        searchCategoryModel.categoryId.value != 1L -> {
                    searchCategoryModel.navigateBack()
                }
                _activeWindowType.value == ActiveWindowType.CATEGORY &&
                        listingCategoryModel.categoryId.value != 1L -> {
                    listingCategoryModel.navigateBack()
                }
                _activeWindowType.value != ActiveWindowType.LISTING -> {
                    _activeWindowType.value = ActiveWindowType.LISTING
                }
                else -> {
                    component.goBack()
                }
            }
        },
        clickCategory = { complete ->
            changeOpenCategory(complete)
        },
        closeFilters = { update, clear ->
            if (clear){
                _listingData.update {
                    it.copy(
                        data = it.data.copy(
                            filters = ListingFilters.getEmpty()
                        )
                    )
                }
            }
            if (update){
                refresh()
            }
            _activeWindowType.value = ActiveWindowType.LISTING
        },
        updateItem = { item ->
            this@ListingViewModel.updateItem(item)
        },
        clearError = {
            errorString.value = ""
        }
    )

    val pagingDataFlow: Flow<PagingData<OfferItemState>> = _listingData.flatMapLatest { listingParams ->
        pagingRepository.getListing(listingParams, apiService, Offer.serializer()).map { pagingData ->
            pagingData.map { offer ->
                if (offer.promoOptions != null && offer.sellerData?.id != UserData.login) {
                    val isBackLight =
                        offer.promoOptions.find { it.id == "backlignt_in_listing" }
                    if (isBackLight != null) {
                        val eventParameters = mapOf(
                            "catalog_category" to offer.catpath.lastOrNull(),
                            "lot_category" to if (offer.catpath.isEmpty()) 1 else offer.catpath.firstOrNull(),
                            "offer_id" to offer.id,
                        )

                        analyticsHelper.reportEvent("show_top_lots", eventParameters)
                    }
                }
                val item = offer.parseToOfferItem()

                OfferItemState(
                    item = item,
                    onItemClick = {
                        component.goToOffer(item)
                    },
                    addToFavorites = {
                        addToFavorites(item) {
                            updateItem.value = offer.id
                        }
                    },
                    updateItemState = {
                        updateItem(item)
                    }
                )
            }
        }
    }.cachedIn(viewModelScope)

    val searchDataState: StateFlow<SearchUiState> = combine(
        _activeWindowType,
        _responseHistory,
        _changeSearchTab,
        _listingData
    ){ activeWindowType, responseHistory, changeSearchTab, listingData ->
        SearchUiState(
            openSearch = activeWindowType == ActiveWindowType.SEARCH,
            searchData = listingData.searchData,
            searchHistory = responseHistory,
            selectedTabIndex = changeSearchTab,
            tabs = buildList {
                add(
                    Tab(searchHistory.value)
                )
                if (UserData.token != "") {
                    add(Tab(subTitle.value))
                }
            },
            appBarData = searchAppBar,
            closeAppBar = closeAppBar,
            categoryState = CategoryState(
                openCategory = activeWindowType == ActiveWindowType.CATEGORY_FILTERS,
                categoryViewModel = searchCategoryModel
            ),
            searchEvents = searchEvents,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SearchUiState(
            appBarData = searchAppBar,
            closeAppBar = closeAppBar,
        )
    )

    val listingDataState: StateFlow<ListingStateContent> = combine(
        _responseOffersRecommendedInListing,
        _activeWindowType,
        _regionOptions,
        _listingData
    ) { promoOffers, activeWindowType, regionOptions, listingData ->
        val ld = listingData.data
        val searchData = listingData.searchData

        val filters = ld.filters.filter { it.value != "" }

        ListingStateContent(
            regions = regionOptions,
            appBarData = object : SimpleAppBarData {
                override val modifier: Modifier
                    get() = Modifier.fillMaxWidth()
                override val color: Color
                    get() = colors.primaryColor
                override val content: @Composable (() -> Unit)
                    get() = {
                        Row(
                            modifier = Modifier
                                .background(colors.white, MaterialTheme.shapes.small)
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    changeOpenCategory()
                                }
                                .fillMaxWidth()
                                .padding(dimens.smallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Icon(
                                painterResource(drawables.listIcon),
                                contentDescription = null,
                                tint = colors.black,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )

                            Text(
                                text = if (searchData.searchCategoryName.isNotEmpty())
                                    searchData.searchCategoryName
                                else catDef.value,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.black,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Icon(
                                painterResource(drawables.nextArrowIcon),
                                contentDescription = null,
                                tint = colors.black,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                        }
                    }
                override val onBackClick: () -> Unit
                    get() = {
                        if (activeWindowType == ActiveWindowType.LISTING) {
                            component.goBack()
                            changeOpenCategory()
                        } else {
                            _activeWindowType.value = ActiveWindowType.LISTING
                        }
                    }
                override val listItems: List<NavigationItem>
                    get() = listOf(
                        NavigationItem(
                            title = "",
                            icon = drawables.recycleIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                            badgeCount = null,
                            onClick = { refresh() }
                        ),
                        NavigationItem(
                            title = subTitle.value,
                            icon = drawables.favoritesIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = (searchData.searchCategoryID != 1L || searchData.userSearch || searchData.searchString != ""),
                            onClick = {
                                if (UserData.token != "") {
                                    addNewSubscribe(
                                        ld,
                                        searchData,
                                        onSuccess = {},
                                        errorCallback = { es ->
                                            errorString.value = es
                                        }
                                    )
                                } else {
                                    goToLogin(false)
                                }
                            }
                        ),
                        NavigationItem(
                            title = searchTitle.value,
                            icon = drawables.searchIcon,
                            tint = colors.black,
                            hasNews = false,
                            badgeCount = null,
                            onClick = { changeOpenSearch(true) }
                        ),
                    )
                override val showMenu: MutableState<Boolean>
                    get() = mutableStateOf(false)
                override val menuItems: List<MenuItem>
                    get() = emptyList()
            },
            listingCategoryState = CategoryState(
                openCategory = activeWindowType == ActiveWindowType.CATEGORY,
                categoryViewModel = listingCategoryModel
            ),
            listingData = listingData,
            filterBarData = FilterBarUiState(
                listFiltersButtons = buildList {
                    filters.forEach { filter ->
                        filter.interpretation?.let { text ->
                            add(
                                FilterListingBtnItem(
                                    text = text,
                                    itemClick = {
                                        _activeWindowType.value = ActiveWindowType.FILTERS
                                    },
                                    removeFilter = {
                                        filters.find {
                                            it.key == filter.key && it.operation == filter.operation
                                        }?.value = ""

                                        filters.find {
                                            it.key == filter.key && it.operation == filter.operation
                                        }?.interpretation = null

                                        _listingData.update {
                                            it.copy(data = it.data.copy(filters = ArrayList(filters)))
                                        }

                                        refresh()
                                    }
                                )
                            )
                        }
                    }

                    if (ld.sort != null) {
                        add(
                            FilterListingBtnItem(
                                text = sortString.value,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowType.SORTING
                                },
                                removeFilter = {
                                    _listingData.update {
                                        it.copy(data = it.data.copy(sort = null))
                                    }
                                    refresh()
                                }
                            )
                        )
                    }

                    if (searchData.userSearch && searchData.userLogin != null) {
                        add(
                            FilterListingBtnItem(
                                text = userString.value + ": " + (searchData.userLogin ?: userDef),
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowType.SEARCH
                                },
                                removeFilter = {
                                    _listingData.update {
                                        it.copy(
                                            searchData = it.searchData.copy(
                                                userSearch = false,
                                                userLogin = null,
                                                searchFinished = false

                                            )
                                        )
                                    }
                                    refresh()
                                }
                            )
                        )
                    }
                    if (searchData.searchString.isNotEmpty()) {
                        add(
                            FilterListingBtnItem(
                                text = searchTitle.value + ": " + searchData.searchString,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowType.SEARCH
                                },
                                removeFilter = {
                                    _listingData.update {
                                        it.copy(searchData = it.searchData.copy(searchString = ""))
                                    }
                                    refresh()
                                }
                            )
                        )
                    }
                    if (searchData.searchFinished) {
                        add(
                            FilterListingBtnItem(
                                text = searchFinishedString.value,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowType.SEARCH
                                },
                                removeFilter = {
                                    _listingData.update {
                                        it.copy(searchData = it.searchData.copy(searchFinished = false))
                                    }
                                    refresh()
                                }
                            )
                        )
                    }

                },
                listNavigation = buildList {
                    add(
                        NavigationItem(
                            title = filterString.value,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowType.FILTERS
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString.value,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.sort != null,
                            badgeCount = null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowType.SORTING
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = menuString.value,
                            icon = if (ld.listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal,
                            tint = colors.black,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = true,
                            onClick = {
                                val newType = if (_listingData.value.data.listingType == 0) 1 else 0
                                settings.setSettingValue("listingType", newType)
                                _listingData.update {
                                    it.copy(data = it.data.copy(listingType = newType))
                                }
                                refresh()
                            }
                        )
                    )
                }
            ),
            listingBaseState = ListingBaseState(
                listingData = listingData.data,
                searchData = listingData.searchData,
                promoList = promoOffers,
                activeWindowType = activeWindowType,
                columns = if(ld.listingType == 1) 2 else 1,
            ),
            listingEvents = listingEvents
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListingStateContent(
            appBarData = object : SimpleAppBarData {
                override val modifier: Modifier
                    get() = Modifier
                override val color: Color
                    get() = colors.white
                override val content: @Composable () -> Unit
                    get() = {}
                override val onBackClick: () -> Unit
                    get() = {}
                override val listItems: List<NavigationItem>
                    get() = emptyList()
                override val showMenu: MutableState<Boolean>
                    get() = mutableStateOf(false)
                override val menuItems: List<MenuItem>
                    get() = emptyList()
            },
        )
    )

    fun clearSearch() {
        searchString.value = TextFieldValue("")
    }

    fun updateItem(item : OfferItem?){
        viewModelScope.launch {
            val offer = withContext(Dispatchers.IO) {
                getOfferById(item?.id ?: 1L)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    item?.setNewParams(offer)
                }
                updateItem.value = null
            }
        }
    }

    fun setSearchFilters(){
        val searchData = _listingData.value.searchData

        val selectedUser = searchData.userSearch
        val selectedUserLogin = searchData.userLogin
        val selectedUserFinished = searchData.searchFinished

        addHistory(
            searchString.value.text,
            if(selectedUserLogin == null) selectedUser else false,
            selectedUserFinished
        )

        if (selectedUser && selectedUserLogin == null){
            if (searchString.value.text != "") {
                searchData.isRefreshing = true
                searchData.userLogin = searchString.value.text
                searchData.userSearch = selectedUser
                searchString.value = TextFieldValue()
            }
        }else{
            if (searchData.userLogin != selectedUserLogin){
                searchData.userLogin = selectedUserLogin
                searchData.isRefreshing = true
            }

            if (searchData.userSearch != selectedUser){
                searchData.userSearch = selectedUser
                searchData.isRefreshing = true
            }
        }

        if (searchData.searchString != searchString.value.text) {
            searchData.searchString = searchString.value.text
            searchData.isRefreshing = true
        }

        if (searchData.searchFinished != selectedUserFinished){
            searchData.searchFinished = selectedUserFinished
            searchData.isRefreshing = true
        }

        searchAnalytic(searchData)

        _listingData.value = _listingData.value.copy(searchData = searchData)
    }

    fun init(ld: ListingData) {
        viewModelScope.launch {
            searchTitle.value = getString(strings.searchTitle)
            searchHistory.value = getString(strings.searchHistory)
            subTitle.value = getString(strings.mySubscribedTitle)
            userDef.value = getString(strings.searchUsersSearch)
            filterString.value = getString(strings.filter)
            sortString.value = getString(strings.sort)
            menuString.value = getString(strings.menuTitle)
            searchFinishedString.value = getString(strings.searchUserFinishedStringChoice)
            userString.value = getString(strings.searchUsersSearch)
        }

        ld.data.methodServer = "get_public_listing"
        ld.data.objServer = "offers"

        if (ld.data.filters.isEmpty()) {
            ld.data.filters = ListingFilters.getEmpty()
        }

        ld.data.listingType = settings.getSettingValue("listingType", 0) ?: 0

        _listingData.update { ld }

        getRegions()

        getOffersRecommendedInListing(_listingData.value.searchData.searchCategoryID)
    }

    fun changeOpenCategory(complete: Boolean = false) {
        listingCategoryModel.run {
            if (_activeWindowType.value == ActiveWindowType.LISTING) {
                if (_listingData.value.searchData.searchCategoryID != categoryId.value || categories.value.isEmpty()) {
                    listingCategoryModel.updateFromSearchData(_listingData.value.searchData)
                    listingCategoryModel.initialize(_listingData.value.data.filters)
                }
                _activeWindowType.value = ActiveWindowType.CATEGORY
                val eventParameters = mapOf(
                    "category_name" to _listingData.value.searchData.searchCategoryName,
                    "category_id" to _listingData.value.searchData.searchCategoryID,
                )
                analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
            } else {
                if (complete) {
                    listingCategoryModel.run {
                        if (_listingData.value.searchData.searchCategoryID != categoryId.value) {
                            _listingData.update { currentListingData ->
                                currentListingData.copy(
                                    searchData = currentListingData.searchData.copy(
                                        searchCategoryID = categoryId.value,
                                        searchCategoryName = categoryName.value,
                                        searchParentID = parentId.value,
                                        searchIsLeaf = isLeaf.value,
                                    )
                                )
                            }

                            refresh()
                        }
                    }
                }

                _activeWindowType.value = ActiveWindowType.LISTING
            }
        }
    }

    fun changeOpenSearch(value : Boolean) {
        val sd = _listingData.value.searchData
        if (value) {
            searchString.value = TextFieldValue(
                sd.searchString,
                TextRange(sd.searchString.length)
            )

            val eventParameters = mapOf(
                "search_string" to sd.searchString,
                "category_id" to sd.searchCategoryID,
                "category_name" to sd.searchCategoryName,
                "user_login" to sd.userLogin,
                "user_search" to sd.userSearch,
                "user_finished" to sd.searchFinished
            )
            analyticsHelper.reportEvent("open_search_listing", eventParameters)

            getHistory()

            _activeWindowType.value = ActiveWindowType.SEARCH
        }else{
            setSearchFilters()
            if (sd.isRefreshing) {
                refresh()
                sd.isRefreshing = false
            }
            _activeWindowType.value = ActiveWindowType.LISTING
        }
    }

    fun openSearchCategory(value : Boolean, complete: Boolean) {
        searchCategoryModel.run {
            if (!value) {
                if (complete) {
                    if (_listingData.value.searchData.searchCategoryID != categoryId.value) {
                        _listingData.update { currentListingData ->
                            currentListingData.copy(
                                searchData = currentListingData.searchData.copy(
                                    searchCategoryID = categoryId.value,
                                    searchCategoryName = categoryName.value,
                                    searchParentID = parentId.value,
                                    searchIsLeaf = isLeaf.value,
                                    isRefreshing = true
                                )
                            )
                        }
                    }
                }
                _activeWindowType.value = ActiveWindowType.SEARCH
            } else {
                searchCategoryModel.run {
                    if (_listingData.value.searchData.searchCategoryID != categoryId.value || categories.value.isEmpty()) {
                        updateFromSearchData(_listingData.value.searchData)
                        initialize()
                    }
                }
                _activeWindowType.value = ActiveWindowType.CATEGORY_FILTERS
            }
        }
    }

    fun refresh(){
        resetScroll()
        onError(ServerErrorException())
        pagingRepository.refresh()
    }

    private fun getOffersRecommendedInListing(categoryID:Long) {
        viewModelScope.launch{
            try {
                val response = withContext(Dispatchers.IO){
                    apiService.getOffersRecommendedInListing(categoryID)
                }

                withContext(Dispatchers.Main) {
                    try {
                        val serializer = Payload.serializer(Offer.serializer())
                        val payload : Payload<Offer> = deserializePayload(response.payload, serializer)
                        _responseOffersRecommendedInListing.value = payload.objects.map { it.parseToOfferItem() }.toList()
                    }catch (_ : Exception){
                        throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                    }
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError (
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
            }
        }
    }

    private fun getRegions(){
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                categoryOperations.getRegions()
            }
            withContext(Dispatchers.Main) {
                res?.firstOrNull()?.options?.sortedBy { it.weight }?.let { _regionOptions.value = it }
            }
        }
    }

    fun getHistory(searchString : String = ""){
        try {
            val sh = db.searchHistoryQueries
            val searchHistory : List<SearchHistory> =
                sh.selectSearch("${searchString.trim()}%", UserData.login).executeAsList()

            _responseHistory.value = searchHistory.map {
                SearchHistoryItem(
                    id = it.id,
                    query = it.query.split("_", limit = 2)[0].trim(),
                    isUsersSearch = it.query.contains("_user"),
                    isFinished = it.query.contains("_finished")
                )
            }
        }catch (e : Exception){
            onError(ServerErrorException(e.message.toString(), ""))
        }
    }

    fun deleteHistory() {
        val sh = db.searchHistoryQueries
        sh.deleteAll()
        getHistory()
    }

    fun deleteItemHistory(id: Long) {
        val sh = db.searchHistoryQueries
        sh.deleteById(id, UserData.login)
        getHistory()
    }

    fun clearSearchCategory() {
        _listingData.update { currentListingData ->
            currentListingData.searchData.clear(catDef.value)
            currentListingData.copy()
        }
    }

    fun changeSearchTab(tab: Int) {
        _changeSearchTab.value = tab
    }

    fun selectUserSearch(){
        _listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    userSearch = !currentListingData.searchData.userSearch
                )
            )
        }
    }

    fun selectUserFinished(){
        _listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    searchFinished = !currentListingData.searchData.searchFinished
                )
            )
        }
    }

    fun clearUserSearch(){
        _listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    userSearch = false,
                    userLogin = null,
                    userID = 1L
                )
            )
        }
    }

    fun onClickHistoryItem(item: SearchHistoryItem) {
        searchString.value = searchString.value.copy(text = item.query)

        _listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    userSearch = item.isUsersSearch,
                    searchFinished = item.isFinished,
                )
            )
        }

        setSearchFilters()
        changeOpenSearch(false)
    }

    fun editHistoryItem(item: SearchHistoryItem) {
        searchString.value = searchString.value.copy(text = item.query, selection = TextRange(item.query.length))
        deleteItemHistory(item.id)
    }

    fun addHistory(searchString: String, isUsersSearch : Boolean = false, isFinished : Boolean = false) {
        if (searchString != "") {
            val sh = db.searchHistoryQueries
            val s = searchString.trim() + if (isUsersSearch) " _user" else "" + if (isFinished) " _finished" else ""
            if (sh.selectSearch("${s}%", UserData.login).executeAsList().isEmpty()){
                sh.insertEntry(s, UserData.login)
            }
        }
    }

    fun searchAnalytic(searchData : SD){
        if (searchData.isRefreshing) {
            val event = mapOf(
                "search_query" to searchData.searchString,
                "visitor_id" to UserData.login,
                "search_cat_id" to searchData.searchCategoryID,
                "user_search" to searchData.userSearch,
                "user_search_login" to searchData.userLogin,
                "user_search_id" to searchData.userID
            )
            analyticsHelper.reportEvent("search_for_item", event)
        }
    }
}
