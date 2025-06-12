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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.shared.SearchHistory
import market.engine.widgets.bars.SimpleAppBarData
import market.engine.widgets.filterContents.categories.CategoryViewModel
import market.engine.widgets.items.FilterListingBtnItem
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import kotlin.String

data class SearchUiState(
    val searchString: String = "",
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val userSearch: Boolean = false,
    val userLogin: String? = null,
    val userFinished: Boolean = false,
    val searchCategoryID: Long = 1L,
    val searchCategoryName: String = "",
    val searchParentID: Long = 1L,
    val searchParentName: String? = null,
    val selectedTabIndex: Int = 0,
    val tabs: List<Tab> = listOf<Tab>(),
    val searchIsLeaf: Boolean = false,
    val isRefreshing: Boolean = false,
    val openSearch: Boolean = false,
    val openCategory: Boolean = false,
    val appBarData: SimpleAppBarData? = null,
    val closeAppBar: SimpleAppBarData? = null,
    val categoryViewModel: CategoryViewModel
)

data class ListingDataUiState(
    val pagingDataFlow: Flow<PagingData<OfferItem>>? = null,
    val promoOffers: List<OfferItem>? = null,
    val regions: List<Options> = emptyList(),
    val filterBarData: FilterBarUiState = FilterBarUiState(),
)

data class ListingUiState(
    val appBarData: SimpleAppBarData? = null,
    val openCategory: Boolean = false,
    val isLoading: Boolean = false,
    val error: ServerErrorException = ServerErrorException(),
    val categoryViewModel: CategoryViewModel
)

data class FilterBarUiState(
    val listFiltersButtons: List<FilterListingBtnItem> = emptyList(),
    val listNavigation: List<NavigationItem> = emptyList(),
    val isShowFilters: Boolean = true,
    val isShowGrid: Boolean = false,
)

class ListingViewModel(val component: ListingComponent) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _responseOffersRecommendedInListing = MutableStateFlow<List<OfferItem>?>(null)

    private val _responseHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())

    private val _pagingDataFlow = MutableStateFlow<Flow<PagingData<OfferItem>>?>(null)

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())

    private val _changeSearchTab = MutableStateFlow<Int>(0)

    val listingData = MutableStateFlow(ListingData())

    val searchData = MutableStateFlow(SD())

    private val _openSearch = MutableStateFlow(false)

    private val openSearchCategory = MutableStateFlow(false)

    private var searchTitle = ""

    val searchString = mutableStateOf(TextFieldValue(""))

    val errorString = mutableStateOf("")

    val updateFilters = MutableStateFlow(false)

    val searchCategoryModel = CategoryViewModel(
        isFilters = true,
    )

    val listingCategoryModel = CategoryViewModel()

    val uiDataState: StateFlow<ListingDataUiState> = combine(
        _pagingDataFlow,
        _responseOffersRecommendedInListing,
        _regionOptions,
    ) { pagingDataFlow, promoOffers, regionOptions ->
        ListingDataUiState(
            pagingDataFlow = pagingDataFlow,
            promoOffers = promoOffers,
            regions = regionOptions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ListingDataUiState()
    )

    val uiFilterBarUiState: StateFlow<FilterBarUiState> = combine(
        activeFiltersType,
        updateFilters,
        listingData
    ) { type, update, listingData ->
        val userDef = getString(strings.searchUsersSearch)
        val ld = listingData.data.value
        val searchData = listingData.searchData.value
        val filters = ld.filters.filter { it.interpretation != "" && it.interpretation != null }
        FilterBarUiState(
            listFiltersButtons = buildList {
                filters.forEach { filter ->
                    filter.interpretation?.let { text ->
                        add(
                            FilterListingBtnItem(
                                text = text,
                                itemClick = {
                                    activeFiltersType.value = "filters"
                                },
                                removeFilter = {
                                    ld.filters.find {
                                        it.key == filter.key && it.operation == filter.operation
                                    }?.value = ""

                                    ld.filters.find {
                                        it.key == filter.key && it.operation == filter.operation
                                    }?.interpretation = null

                                    component.refresh()
                                }
                            )
                        )
                    }
                }
                if (searchData.userSearch && searchData.userLogin != null) {
                    add(
                        FilterListingBtnItem(
                            text = getString(strings.searchUsersSearch) + ": " + (searchData.userLogin
                                ?: userDef),
                            itemClick = {
                                changeOpenSearch(true)
                            },
                            removeFilter = {
                                searchData.userLogin = null
                                searchData.userSearch = false
                                searchData.searchFinished = false
                                component.refresh()
                            }
                        )
                    )
                }
                if (searchData.searchString.isNotEmpty()) {
                    add(
                        FilterListingBtnItem(
                            text = getString(strings.searchTitle) + ": " + searchData.searchString,
                            itemClick = {
                                changeOpenSearch(true)
                            },
                            removeFilter = {
                                searchData.searchString = ""
                                component.refresh()
                            }
                        )
                    )
                }
                if (searchData.searchFinished) {
                    add(
                        FilterListingBtnItem(
                            text = getString(strings.searchUserFinishedStringChoice),
                            itemClick = {
                                changeOpenSearch(true)
                            },
                            removeFilter = {
                                searchData.searchFinished = false
                                component.refresh()
                            }
                        )
                    )
                }
                if (ld.sort != null) {
                    add(
                        FilterListingBtnItem(
                            text = getString(strings.searchUserFinishedStringChoice),
                            itemClick = {
                                activeFiltersType.value = "sorting"
                            },
                            removeFilter = {
                                ld.sort = null
                                component.refresh()
                            }
                        )
                    )
                }
            },
            listNavigation = buildList {
                add(
                    NavigationItem(
                        title = getString(strings.filter),
                        icon = drawables.filterIcon,
                        tint = colors.black,
                        hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                        badgeCount = if(filters.isNotEmpty()) filters.size else null,
                        onClick = {
                            activeFiltersType.value = "filters"
                        }
                    )
                )
                add(
                    NavigationItem(
                        title = getString(strings.sort),
                        icon = drawables.sortIcon,
                        tint = colors.black,
                        hasNews = ld.sort != null,
                        badgeCount = null,
                        onClick = {
                            activeFiltersType.value = "sorting"
                        }
                    )
                )
                add(
                    NavigationItem(
                        title = getString(strings.menuTitle),
                        icon = if (ld.listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal,
                        tint = colors.black,
                        hasNews = false,
                        badgeCount = null,
                        isVisible = true,
                        onClick = {
                            val newType = if (ld.listingType == 0) 1 else 0
                            settings.setSettingValue("listingType", newType)
                            ld.listingType = newType
                            updateFilters.value = !updateFilters.value
                            component.refresh()
                        }
                    )
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FilterBarUiState()
    )

    val uiState: StateFlow<ListingUiState> = combine(
        isShowProgress,
        errorMessage,
    ) { isLoading, error ->
        searchData.value = listingData.value.searchData.value
        val subs = getString(strings.subscribersLabel)
        val searchTitle = getString(strings.searchTitle)
        ListingUiState(
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
                                text = if (searchData.value.searchCategoryName.isNotEmpty())
                                    searchData.value.searchCategoryName
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
                        if (activeFiltersType.value.isEmpty()) {
                            component.goBack()
                            changeOpenCategory()
                        } else {
                            activeFiltersType.value = ""
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
                            onClick = { component.refresh() }
                        ),
                        NavigationItem(
                            title = subs,
                            icon = drawables.favoritesIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = (searchData.value.searchCategoryID != 1L || searchData.value.userSearch || searchData.value.searchString != ""),
                            onClick = {
                                if (UserData.token != "") {
                                    addNewSubscribe(
                                        listingData.value.data.value,
                                        searchData.value,
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
                            title = searchTitle,
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
            isLoading = isLoading,
            error = error,
            categoryViewModel = listingCategoryModel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ListingUiState(
            categoryViewModel = listingCategoryModel
        )
    )

    val uiSearchState: StateFlow<SearchUiState> = combine(
        _responseHistory,
        _openSearch,
        _changeSearchTab,
        searchData,
        openSearchCategory
    ) { responseHistory, openSearch, changeSearchTab, searchData, openSearchCategory ->
        SearchUiState(
            searchHistory = responseHistory,
            openSearch = openSearch,
            openCategory = openSearchCategory,
            searchString = searchData.searchString,
            searchCategoryID = searchData.searchCategoryID,
            searchCategoryName = searchData.searchCategoryName,
            userSearch = searchData.userSearch,
            userLogin = searchData.userLogin,
            userFinished = searchData.searchFinished,
            searchIsLeaf = searchData.searchIsLeaf,
            tabs = buildList {
                add(
                    Tab(getString(strings.searchHistory))
                )
                if (UserData.token != "") {
                    add(Tab(getString(strings.mySubscribedTitle)))
                }
            },
            selectedTabIndex = changeSearchTab,
            searchParentID = searchData.searchParentID ?: 1,
            searchParentName = searchData.searchParentName,
            appBarData = object : SimpleAppBarData {
                override val modifier: Modifier
                    get() = Modifier
                override val color: Color
                    get() = colors.white
                override val content: @Composable () -> Unit
                    get() = {
                        SearchTextField(
                            openSearch,
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
                            title = searchTitle,
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
            },
            closeAppBar = object : SimpleAppBarData {
                override val modifier: Modifier
                    get() = Modifier
                override val color: Color
                    get() = colors.white
                override val content: @Composable () -> Unit
                    get() = {}
                override val onBackClick: () -> Unit
                    get() = {
                        openSearchCategory(false)
                    }
                override val listItems: List<NavigationItem>
                    get() = emptyList()
                override val showMenu: MutableState<Boolean>
                    get() = mutableStateOf(false)
                override val menuItems: List<MenuItem>
                    get() = emptyList()
            },
            categoryViewModel = searchCategoryModel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchUiState(
            categoryViewModel = searchCategoryModel
        )
    )

    init {
        viewModelScope.launch {
            searchTitle = getString(strings.searchTitle)
        }
    }

    fun clearSearch() {
        searchString.value = TextFieldValue("")
    }

    fun setSearchFilters(){
        val selectedUser = searchData.value.userSearch
        val selectedUserLogin = searchData.value.userLogin
        val selectedUserFinished = searchData.value.searchFinished

        val searchData = searchData.value

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

        listingData.value = listingData.value.copy(searchData = mutableStateOf(searchData))
    }

    fun init(ld: ListingData) {
         listingData.value = ld
        searchData.value = ld.searchData.value

        listingData.value.data.value.methodServer = "get_public_listing"
        listingData.value.data.value.objServer = "offers"

         if (listingData.value.data.value.filters.isEmpty()) {
             listingData.value.data.value.filters = ListingFilters.getEmpty()
         }

        listingData.value.data.value.listingType = settings.getSettingValue("listingType", 0) ?: 0

         getRegions()

         getOffersRecommendedInListing(listingData.value.searchData.value.searchCategoryID)

         _pagingDataFlow.value = pagingRepository.getListing(listingData.value, apiService, Offer.serializer())
             .map { offer ->
                 offer.map {
                     if (it.promoOptions != null && it.sellerData?.id != UserData.login) {
                         val isBackLight = it.promoOptions.find { it.id == "backlignt_in_listing" }
                         if (isBackLight != null) {
                             val eventParameters = mapOf(
                                 "catalog_category" to it.catpath.lastOrNull(),
                                 "lot_category" to if (it.catpath.isEmpty()) 1 else it.catpath.firstOrNull(),
                                 "offer_id" to it.id,
                             )

                             analyticsHelper.reportEvent("show_top_lots", eventParameters)
                         }
                     }

                     it.parseToOfferItem()
                 }
             }.cachedIn(viewModelScope)
     }

    fun changeOpenCategory() {
        if (activeFiltersType.value == "") {
            uiState.value.categoryViewModel.updateFromSearchData(searchData.value)
            uiState.value.categoryViewModel.initialize(listingData.value.data.value.filters)

            activeFiltersType.value = "categories"
            val eventParameters = mapOf(
                "category_name" to searchData.value.searchCategoryName,
                "category_id" to searchData.value.searchCategoryID,
            )
            analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
        } else {
            activeFiltersType.value = ""
        }
    }

    fun changeOpenSearch(value : Boolean) {
        val sd = listingData.value.searchData.value
        if (value) {
            searchData.value = listingData.value.searchData.value

            searchString.value = TextFieldValue(
                searchData.value.searchString,
                TextRange(searchData.value.searchString.length)
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
        }else{
            setSearchFilters()
            if (searchData.value.isRefreshing) {
                refresh()
                searchData.value.isRefreshing = false
            }
        }

        updateFilters.value = !updateFilters.value
        _openSearch.value = value
    }

    fun openSearchCategory(value : Boolean) {
        openSearchCategory.value = value
        uiSearchState.value.categoryViewModel.run {
            if (!value) {
                if (searchData.value.searchCategoryID != categoryId.value) {
                    searchData.value.searchCategoryID = categoryId.value
                    searchData.value.searchCategoryName = categoryName.value
                    searchData.value.searchParentID = parentId.value
                    searchData.value.searchIsLeaf = isLeaf.value
                    searchData.value.isRefreshing = true
                }
            } else {
                searchCategoryModel.run {
                    if (searchData.value.searchCategoryID != categoryId.value || categories.value.isEmpty()) {
                        updateFromSearchData(searchData.value)
                        initialize()
                    }
                }
            }
        }
    }

    fun refresh(){
        listingData.value = listingData.value.copy()
        pagingRepository.refresh(listingData.value)
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
        val sd = searchData.value.copy()
        sd.searchCategoryID = 1L
        sd.searchCategoryName = catDef.value
        sd.clear(catDef.value)
        searchData.value = sd
    }

    fun changeSearchTab(tab: Int) {
        _changeSearchTab.value = tab
    }

    fun selectUserSearch(){
        searchData.value = searchData.value.copy(userSearch = searchData.value.userSearch.not())
    }

    fun selectUserFinished(){
        searchData.value = searchData.value.copy(searchFinished = searchData.value.searchFinished.not())
    }

    fun clearUserSearch(){
        searchData.value = searchData.value.copy(userLogin = null, userSearch = false, userID = 1L)
    }

    fun onClickHistoryItem(item: SearchHistoryItem) {
        searchString.value = searchString.value.copy(text = item.query)

        searchData.value = searchData.value.copy(
            userSearch = item.isUsersSearch,
            searchFinished = item.isFinished
        )

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
