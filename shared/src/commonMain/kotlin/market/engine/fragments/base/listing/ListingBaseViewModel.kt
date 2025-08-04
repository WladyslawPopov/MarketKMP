package market.engine.fragments.base.listing

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.events.SearchEvents
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.data.items.Tab
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.SearchUiState
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.states.SwipeTabsBarState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.main.listing.ListingComponent
import market.engine.shared.SearchHistory
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString

data class SearchEventsImpl(
    val viewModel : ListingBaseViewModel
) : SearchEvents
{
    override fun onRefresh() = viewModel.searchRefresh()

    override fun goToListing() = viewModel.changeOpenSearch(false)

    override fun onDeleteHistory() = viewModel.deleteHistory()

    override fun onDeleteHistoryItem(id: Long) = viewModel.deleteItemHistory(id)

    override fun onHistoryItemClicked(item: SearchHistoryItem) = viewModel.onClickHistoryItem(item)

    override fun editHistoryItem(item: SearchHistoryItem) = viewModel.editHistoryItem(item)

    override fun openSearchCategory(value: Boolean, complete: Boolean) = viewModel.openSearchCategory(value, complete)

    override fun clearCategory() = viewModel.clearSearchCategory()

    override fun clickUser() = viewModel.selectUserSearch()

    override fun clearUser() = viewModel.clearUserSearch()

    override fun clickSearchFinished() = viewModel.selectSearchFinished()

    override fun onTabSelect(tab : Int) = viewModel.changeSearchTab(tab)

    override fun updateSearch(value: String) = viewModel.onUpdateSearchString(value)
    override fun clearSearch() = viewModel.clearSearch()
}

class ListingBaseViewModel(
    listingData: ListingData = ListingData(),
    isOpenSearch : Boolean = false,
    showSwipeTabs : Boolean = false,
    val listingComponent: ListingComponent? = null,
    val deleteSelectedItems: () -> Unit = {},
    val searchCategoryViewModel: CategoryViewModel? = null,
    savedStateHandle: SavedStateHandle,

) : CoreViewModel(savedStateHandle) {

    private val _selectItems = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "selectItems",
        initialValue = emptyList(),
        serializer = ListSerializer(
            Long.serializer()
        )
    )
    val selectItems : StateFlow<List<Long>> = _selectItems.state

    private val _totalCount = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "totalCount",
        initialValue = 0,
        serializer = Int.serializer()
    )
    val totalCount : StateFlow<Int> = _totalCount.state

    private val _listingType = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "listingType",
        initialValue = 0,
        serializer = Int.serializer()
    )
    val listingType : StateFlow<Int> = _listingType.state

    private val _listingData = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "listingData",
        initialValue = listingData,
        serializer = ListingData.serializer()
    )
    val listingData : StateFlow<ListingData> = _listingData.state

    private val _activeWindowType = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "activeWindowType",
        initialValue = if(isOpenSearch) ActiveWindowListingType.SEARCH else ActiveWindowListingType.LISTING,
        serializer = ActiveWindowListingType.serializer()
    )
    val activeWindowType : StateFlow<ActiveWindowListingType> = _activeWindowType.state

    private val _changeSearchTab = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "changeSearchTab",
        initialValue = 0,
        serializer = Int.serializer()
    )

    private val _responseHistory = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "responseHistory",
        initialValue = emptyList(),
        serializer = ListSerializer(SearchHistoryItem.serializer())
    )

    private val _promoList = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "promoList",
        initialValue = emptyList(),
        serializer = ListSerializer(OfferItem.serializer())
    )
    val promoList : StateFlow<List<OfferItem>> = _promoList.state

    private val _isReversingPaging = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "isReversingPaging",
        initialValue = false,
        serializer = Boolean.serializer()
    )
    val isReversingPaging : StateFlow<Boolean> = _isReversingPaging.state

    private val _listItemsNavigationFilterBar = MutableStateFlow<List<NavigationItem>>(emptyList())

    private val _searchString = savedStateHandle.getSavedStateFlow(
        scope = viewModelScope,
        key = "searchString",
        initialValue = "",
        serializer = String.serializer()
    )

    val filterBarUiState : StateFlow<FilterBarUiState> = combine(
        _listingType.state,
        _listingData.state,
        _listItemsNavigationFilterBar
    )
    { listingType, listingData, listItemsNavigation ->
        val ld = listingData.data
        val searchData = listingData.searchData
        val searchTitle = getString(strings.searchTitle)
        val auctionString = getString(strings.ordinaryAuction)
        val buyNowString = getString(strings.buyNow)
        val allString = getString(strings.allOffers)
        val userDef = getString(strings.searchUsersSearch)

        val searchFinishedString = getString(strings.searchUserFinishedStringChoice)
        val userString = getString(strings.searchUsersSearch)

        val filterString = getString(strings.filter)
        val sortString = getString(strings.sort)
        val chooseAction = getString(strings.chooseAction)

        val filters = ld.filters.filter { it.value != "" && it.interpretation?.isNotBlank() == true }

        val curTab = when (ld.filters.find { filter-> filter.key == "sale_type" }?.value) {
            "auction" -> auctionString
            "buynow" -> buyNowString
            else -> allString
        }

        val updatedListNavigation = listItemsNavigation.map { item ->
            when (item.title) {
                getString(strings.filter) -> {
                    item.copy(badgeCount = if (filters.isNotEmpty()) filters.size else null)
                }
                getString(strings.sort) -> {
                    item.copy(hasNews = ld.sort != null)
                }
                else -> {
                    item
                }
            }
        }

        val tabs = listOf(
            Tab(
                title = allString,
            ),
            Tab(
                title = auctionString,
            ),
            Tab(
                title = buyNowString,
            ),
        )

        FilterBarUiState(
            listFiltersButtons = buildList {
                filters.forEach { filter ->
                    filter.interpretation?.let { text ->
                        add(
                            FilterListingBtnItem(
                                text = text,
                                itemClick = {
                                    setActiveWindowType(ActiveWindowListingType.FILTERS)
                                },
                                removeFilter = {
                                    removeFilter(filter)
                                }
                            )
                        )
                    }
                }
                if (ld.sort != null && ld.sort?.interpretation?.isNotBlank() == true){
                    add(
                        FilterListingBtnItem(
                            text = ld.sort?.interpretation ?: sortString,
                            itemClick = {
                                setActiveWindowType(ActiveWindowListingType.SORTING)
                            },
                            removeFilter = {
                                removeSort()
                            }
                        )
                    )
                }
                if (searchData.userSearch && searchData.userLogin != null) {
                    add(
                        FilterListingBtnItem(
                            text = userString + ": " + (searchData.userLogin ?: userDef),
                            itemClick = {
                                setActiveWindowType(ActiveWindowListingType.SEARCH)
                            },
                            removeFilter = {
                                removeUserSearch()
                            }
                        )
                    )
                }
                if (searchData.searchString.isNotEmpty()) {
                    add(
                        FilterListingBtnItem(
                            text = searchTitle + ": " + searchData.searchString,
                            itemClick = {
                                setActiveWindowType(ActiveWindowListingType.SEARCH)
                            },
                            removeFilter = {
                                clearSearch()
                            }
                        )
                    )
                }
                if (searchData.searchFinished) {
                    add(
                        FilterListingBtnItem(
                            text = searchFinishedString,
                            itemClick = {
                                setActiveWindowType(ActiveWindowListingType.SEARCH)
                            },
                            removeFilter = {
                                selectSearchFinished()
                            }
                        )
                    )
                }
            },
            listNavigation = updatedListNavigation,
            swipeTabsBarState = if (showSwipeTabs) {
                SwipeTabsBarState(
                    tabs = tabs,
                    currentTab = curTab,
                    isTabsVisible = true,
                    onClick = {
                        when(curTab){
                            allString -> {
                                changeSaleTab("")
                            }
                            auctionString -> {
                                changeSaleTab("auction")
                            }
                            buyNowString -> {
                                changeSaleTab("buynow")
                            }
                        }
                    }
                )
            } else {
                null
            },
            onClick = { item ->
                when(item.title){
                     filterString-> {
                        setActiveWindowType(ActiveWindowListingType.FILTERS)
                    }
                    sortString -> {
                        setActiveWindowType(ActiveWindowListingType.SORTING)
                    }
                    chooseAction -> {
                        val newType = if (listingType == 0) 1 else 0
                        settings.setSettingValue("listingType", newType)
                        setListingType(newType)
                    }
                }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FilterBarUiState()
    )

    val searchDataState: StateFlow<SearchUiState?> = combine(
        _responseHistory.state,
        _changeSearchTab.state,
        _listingData.state,
        _searchString.state,
        _activeWindowType.state
    )
    { responseHistory, changeSearchTab, listingData, searchString, activeType ->
        val searchHistory = getString(strings.searchHistory)
        val subTitle = getString(strings.mySubscribedTitle)
        val searchTitle = getString(strings.searchTitle)

        if (listingComponent != null) {
            SearchUiState(
                searchData = listingData.searchData,
                searchString = searchDataState.value?.searchString ?: searchString,
                searchHistory = responseHistory,
                selectedTabIndex = changeSearchTab,
                tabs = buildList {
                    add(
                        Tab(searchHistory)
                    )
                    if (UserData.token != "") {
                        add(Tab(subTitle))
                    }
                },
                appBarData = SimpleAppBarData(
                    onBackClick = {
                        changeOpenSearch(false)
                    },
                    listItems = listOf(
                        NavigationItem(
                            title = searchTitle,
                            hasNews = false,
                            badgeCount = null,
                            icon = drawables.searchIcon,
                            tint = colors.black,
                            onClick = {
                                changeOpenSearch(false)
                            }
                        )
                    ),
                ),
                categoryState = searchCategoryViewModel?.let {
                    CategoryState(
                        categoryViewModel = it,
                        openCategory = activeType == ActiveWindowListingType.CATEGORY_FILTERS,
                    )
                },
                searchEvents = SearchEventsImpl(this),
            )
        }else{
            null
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun isHideFilterBar(listingState : ScrollState, noFound : Boolean) : Boolean {
        return listingState.areBarsVisible.value &&
                activeWindowType.value == ActiveWindowListingType.LISTING ||
                activeWindowType.value == ActiveWindowListingType.CATEGORY ||
                noFound
    }

    fun setListingData(data : ListingData){
        _listingData.update {
            it.copy(
                data = data.data,
                searchData = data.searchData
            )
        }
    }

    fun clearSearch() {
        _searchString.value = ""
        _listingData.update {
            it.copy(searchData = it.searchData.copy(searchString = ""))
        }
    }

    fun setListItemsFilterBar(list : List<NavigationItem>){
        _listItemsNavigationFilterBar.value = list
    }

    fun setListingType(type : Int){
        _listingType.value = type
    }

    fun addSelectItem(id : Long){
        _selectItems.value = buildList {
            addAll(selectItems.value)
            add(id)
        }
    }

    fun removeSelectItem(id : Long) {
        _selectItems.value = buildList {
            addAll(selectItems.value)
            remove(id)
        }
    }

    fun setActiveWindowType(type : ActiveWindowListingType){
        _activeWindowType.value = type
    }

    fun setTotalCount(count : Int){
        _totalCount.value = count
    }

    fun clearAllFilters() {
        _listingData.update {
            it.copy(
                data = it.data.copy(filters = ListingFilters.getEmpty())
            )
        }
        setActiveWindowType(ActiveWindowListingType.LISTING)
    }

    fun changeOpenSearch(value : Boolean) {
        val sd = listingData.value.searchData
        if (value) {

            _searchString.value = sd.searchString

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

            setActiveWindowType(ActiveWindowListingType.SEARCH)
        }else{
            setSearchFilters()

            setActiveWindowType(ActiveWindowListingType.LISTING)
        }
    }

    fun clearSelectedItems(){
        _selectItems.value = emptyList()
    }

    fun changeSearchTab(tab: Int) {
        _changeSearchTab.value = tab
        listingComponent?.onTabSelect(tab)
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

    fun setSearchFilters() {
        val searchData = listingData.value.searchData.copy()

        val selectedUser = searchData.userSearch
        val selectedUserLogin = searchData.userLogin
        val selectedUserFinished = searchData.searchFinished

        addHistory(
            _searchString.value,
            if(selectedUserLogin == null) selectedUser else false,
            selectedUserFinished
        )

        if (selectedUser && selectedUserLogin == null){
            if (_searchString.value != "") {
                searchData.isRefreshing = true
                searchData.userLogin = _searchString.value
                searchData.userSearch = selectedUser
                _searchString.value = ""
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

        if (searchData.searchString != _searchString.value) {
            searchData.searchString = _searchString.value
            searchData.isRefreshing = true
        }

        if (searchData.searchFinished != selectedUserFinished){
            searchData.searchFinished = selectedUserFinished
            searchData.isRefreshing = true
        }

        searchAnalytic(searchData)

        if (searchData.isRefreshing) {
            refresh()
            searchData.isRefreshing = false
            _listingData.update {
                it.copy(
                    searchData = searchData
                )
            }
        }
    }

    fun searchRefresh() {
        setLoading(true)
        onError(ServerErrorException())
        getHistory(_searchString.value)
        setSearchFilters()
        viewModelScope.launch {
            delay(1000)
            setLoading(false)
        }
    }

    fun onClickHistoryItem(item: SearchHistoryItem) {
        _searchString.value = item.query

        _listingData.update {
            it.copy(
                searchData = it.searchData.copy(
                    searchString = item.query,
                    userSearch = item.isUsersSearch,
                    searchFinished = item.isFinished
                )
            )
        }

        setSearchFilters()
        changeOpenSearch(false)
    }

    fun editHistoryItem(item: SearchHistoryItem) {
        _searchString.value = item.query
        deleteItemHistory(item.id)
    }

    fun deleteItemHistory(id: Long) {
        val sh = db.searchHistoryQueries
        sh.deleteById(id, UserData.login)
        getHistory()
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

    fun onUpdateSearchString(value: String) {
        _searchString.value = value
        getHistory(value)
    }

    fun deleteHistory() {
        val sh = db.searchHistoryQueries
        sh.deleteAll()
        getHistory()
    }

    fun setPromoList(list : List<OfferItem>){
        _promoList.value = list
    }

    fun setReversingPaging(value : Boolean){
        _isReversingPaging.value = value
    }

    fun openSearchCategory(value : Boolean, complete: Boolean) {
        searchCategoryViewModel?.run {
            if (!value) {
                if (complete) {
                    if (listingData.value.searchData.searchCategoryID != searchData.value.searchCategoryID) {
                        _listingData.update {
                            it.copy(
                                searchData = searchData.value.copy()
                            )
                        }
                    }
                }
                setActiveWindowType(ActiveWindowListingType.SEARCH)
            } else {
                if (listingData.value.searchData.searchCategoryID != searchData.value.searchCategoryID || categories.value.isEmpty()) {
                    updateFromSearchData(listingData.value.searchData.copy())
                    initialize()
                }

                setActiveWindowType(ActiveWindowListingType.CATEGORY_FILTERS)
            }
        }
    }

    fun changeSaleTab(tab : String){
        _listingData.update { currentListingData ->
            val currentData = currentListingData.data
            val newFilters = currentData.filters.map { filterItem ->
                if (filterItem.key == "sale_type") {
                    if (tab != "") {
                        filterItem.copy(value = tab, interpretation = "")
                    }else{
                        filterItem.copy(value = "", interpretation = null)
                    }
                } else {
                    filterItem
                }
            }
            currentListingData.copy(
                data = currentData.copy(filters = newFilters)
            )
        }
    }

    fun removeUserSearch(){
        _listingData.update {
            it.copy(
                searchData = it.searchData.copy(
                    userSearch = false,
                    userLogin = null,
                    searchFinished = false
                )
            )
        }
    }

    fun applyFilters(newFilters: List<Filter>) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    filters = newFilters
                )
            )
        }
        setActiveWindowType(ActiveWindowListingType.LISTING)
    }

    fun applySorting(newSort: Sort?) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    sort = newSort
                )
            )
        }
        setActiveWindowType(ActiveWindowListingType.LISTING)
    }

    fun removeFilter(filter: Filter){
        _listingData.update { currentState ->
            val currentData = currentState.data
            val newFilters = currentData.filters.map { filterItem ->
                if (filterItem.key == filter.key && filterItem.operation == filter.operation) {
                    filterItem.copy(value = "", interpretation = null)
                } else {
                    filterItem
                }
            }

            currentState.copy(
                data = currentData.copy(
                    filters = newFilters
                )
            )
        }
    }

    fun removeSort(){
        _listingData.update {
            it.copy(data = it.data.copy(sort = null))
        }
    }

    fun clearListingData() {
        viewModelScope.launch {
            _listingData.asyncUpdate { listingData ->
                val sd = listingData.searchData.copy()
                sd.clear(getString(strings.categoryMain))

                listingData.copy(
                    searchData = sd,
                    data = listingData.data.copy(
                        filters = ListingFilters.getEmpty()
                    )
                )
            }
        }
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

    fun selectSearchFinished(){
        _listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    searchFinished = !currentListingData.searchData.searchFinished
                )
            )
        }
    }

    fun clearSearchCategory() {
        viewModelScope.launch {
            _listingData.asyncUpdate { currentListingData ->
                currentListingData.searchData.clear(getString(strings.categoryMain))
                currentListingData.copy()
            }
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
}
