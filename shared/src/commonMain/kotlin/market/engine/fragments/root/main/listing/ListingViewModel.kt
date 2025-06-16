package market.engine.fragments.root.main.listing

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.events.ListingEvents
import market.engine.core.data.events.SearchEvents
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
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ListingStateContent
import market.engine.core.data.states.SearchUiState
import market.engine.core.data.types.ActiveWindowListingType
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
import market.engine.widgets.bars.appBars.SimpleAppBarData
import market.engine.widgets.filterContents.categories.CategoryViewModel
import market.engine.widgets.items.offer_Items.OfferItemState
import org.jetbrains.compose.resources.getString
import kotlin.String


@OptIn(ExperimentalCoroutinesApi::class)
class ListingViewModel(val component: ListingComponent) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _responseOffersRecommendedInListing = MutableStateFlow<List<OfferItem>?>(null)

    private val _responseHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())

    private val _changeSearchTab = MutableStateFlow<Int>(0)

    private val _listingData = MutableStateFlow(ListingData())

    private val _searchString = MutableStateFlow(TextFieldValue(""))

    private val _activeWindowType = MutableStateFlow(ActiveWindowListingType.LISTING)

    val errorString = MutableStateFlow("")

    private val searchCategoryModel = CategoryViewModel(
        isFilters = true,
    )
    private val listingCategoryModel = CategoryViewModel()

    var searchTitle = mutableStateOf("")

    private val searchAppBar = SimpleAppBarData(
        onBackClick = {
            changeOpenSearch(false)
        },
        listItems = listOf(
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
        ),
    )

    private val searchEvents : SearchEvents = object : SearchEvents {
        override fun onRefresh() {
            setLoading(true)
            onError(ServerErrorException())
            getHistory(_searchString.value.text)
            setSearchFilters()
            viewModelScope.launch {
                delay(1000)
                setLoading(false)
            }
        }

        override fun goToListing() {
            changeOpenSearch(false)
        }

        override fun onDeleteHistory() {
            deleteHistory()
        }

        override fun onDeleteHistoryItem(id: Long) {
            deleteItemHistory(id)
        }

        override fun onHistoryItemClicked(item: SearchHistoryItem) {
            this@ListingViewModel.onClickHistoryItem(item)
        }

        override fun editHistoryItem(item: SearchHistoryItem) {
            this@ListingViewModel.editHistoryItem(item)
        }

        override fun openSearchCategory(value: Boolean, complete: Boolean) {
            this@ListingViewModel.openSearchCategory(value, complete)
        }

        override fun clearCategory() {
            clearSearchCategory()
        }

        override fun clickUser() {
            selectUserSearch()
        }

        override fun clearUser() {
            clearUserSearch()
        }

        override fun clickSearchFinished() {
            selectUserFinished()
        }

        override fun onTabSelect(tab : Int) {
            component.model.value.searchNavigator.select(tab)
            changeSearchTab(tab)
        }

        override fun updateSearch(value: TextFieldValue) {
            _searchString.value = value
            getHistory(value.text)
        }
    }
    private val listingEvents : ListingEvents = object : ListingEvents {
        override fun onRefresh() {
            refresh()
        }

        override fun changeActiveWindowType(type: ActiveWindowListingType) {
            _activeWindowType.value = type
        }

        override fun clearListingData() {
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
        }

        override fun backClick() {
            when {
                _activeWindowType.value == ActiveWindowListingType.CATEGORY_FILTERS &&
                        searchCategoryModel.categoryId.value != 1L -> {
                    searchCategoryModel.navigateBack()
                }

                _activeWindowType.value == ActiveWindowListingType.CATEGORY &&
                        listingCategoryModel.categoryId.value != 1L -> {
                    listingCategoryModel.navigateBack()
                }

                _activeWindowType.value != ActiveWindowListingType.LISTING -> {
                    _activeWindowType.value = ActiveWindowListingType.LISTING
                }

                else -> {
                    component.goBack()
                }
            }
        }

        override fun clickCategory(complete: Boolean) {
            changeOpenCategory(complete)
        }

        override fun closeFilters(ld: LD, clear: Boolean) {
            if (clear) {
                _listingData.update {
                    it.copy(
                        data = it.data.copy(
                            filters = ListingFilters.getEmpty()
                        )
                    )
                }
            }else{
                _listingData.update { currentState ->
                    // Создаем совершенно новый список, состоящий из совершенно новых копий фильтров.
                    // Это гарантирует, что даже если UI мутировал объекты Filter, мы создадим "чистые" копии.
                    val newCleanFilters = ld.filters.map { it.copy() }

                    // Создаем новую копию LD со всеми новыми данными.
                    val newCleanLd = currentState.data.copy(
                        filters = newCleanFilters,
                        sort = ld.sort?.copy(), // sort тоже копируем
                        listingType = ld.listingType // и остальные поля
                    )

                    // Создаем новую копию ListingData
                    currentState.copy(data = newCleanLd)
                }
            }

            _activeWindowType.value = ActiveWindowListingType.LISTING
        }

        override fun updateItem(item: OfferItem?) {
            this@ListingViewModel.updateItem(item)
        }

        override fun clearError() {
            errorString.value = ""
        }
    }

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
        _listingData,
        _searchString
    ){ activeWindowType, responseHistory, changeSearchTab, listingData, searchString ->
        val searchHistory = getString(strings.searchHistory)
        val subTitle = getString(strings.mySubscribedTitle)

        SearchUiState(
            openSearch = activeWindowType == ActiveWindowListingType.SEARCH,
            searchData = listingData.searchData,
            searchString = searchString,
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
            appBarData = searchAppBar,
            categoryState = CategoryState(
                openCategory = activeWindowType == ActiveWindowListingType.CATEGORY_FILTERS,
                categoryViewModel = searchCategoryModel
            ),
            searchEvents = searchEvents,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SearchUiState(
            searchEvents = searchEvents
        )
    )

    val listingDataState: StateFlow<ListingStateContent> = combine(
        _responseOffersRecommendedInListing,
        _activeWindowType,
        _regionOptions,
        _listingData
    ) { promoOffers, activeType, regionOptions, listingData ->
        val ld = listingData.data
        val searchData = listingData.searchData

        val subs = getString(strings.subscribersLabel)

        val userDef = getString(strings.searchUsersSearch)
        val filterString = getString(strings.filter)
        val sortString = getString(strings.sort)
        val menuString = getString(strings.menuTitle)
        val searchFinishedString = getString(strings.searchUserFinishedStringChoice)
        val userString = getString(strings.searchUsersSearch)

        val filters = ld.filters.filter { it.value != "" && it.interpretation?.isNotBlank() == true }

        ListingStateContent(
            regions = regionOptions,
            appBarData = SimpleAppBarData(
                color = colors.primaryColor,
                onBackClick = {
                    if (activeType == ActiveWindowListingType.LISTING) {
                        component.goBack()
                        changeOpenCategory()
                    } else {
                        _activeWindowType.value = ActiveWindowListingType.LISTING
                    }
                },
                listItems = listOf(
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
                        title = subs,
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
            ),
            listingCategoryState = CategoryState(
                openCategory = activeType == ActiveWindowListingType.CATEGORY,
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
                                        _activeWindowType.value = ActiveWindowListingType.FILTERS
                                    },
                                    removeFilter = {
                                        _listingData.update { currentListingData ->
                                            val currentData = currentListingData.data
                                            val newFilters = currentData.filters.map { filterItem ->
                                                if (filterItem.key == filter.key && filterItem.operation == filter.operation) {
                                                    filterItem.copy(value = "", interpretation = null)
                                                } else {
                                                    filterItem
                                                }
                                            }
                                            currentListingData.copy(
                                                data = currentData.copy(filters = newFilters)
                                            )
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
                                text = sortString,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowListingType.SORTING
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
                                text = userString + ": " + (searchData.userLogin ?: userDef),
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowListingType.SEARCH
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
                                    _activeWindowType.value = ActiveWindowListingType.SEARCH
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
                                text = searchFinishedString,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowListingType.SEARCH
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
                            title = filterString,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowListingType.FILTERS
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.sort != null,
                            badgeCount = null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowListingType.SORTING
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = menuString,
                            icon = if (ld.listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal,
                            tint = colors.black,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = true,
                            onClick = {
                                val newType = if (ld.listingType == 0) 1 else 0
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
                activeWindowType = activeType,
                columns = if(ld.listingType == 1) 2 else 1,
            ),
            listingEvents = listingEvents
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListingStateContent(
            listingEvents = listingEvents
        )
    )


    fun applyFilters(newFilters: List<Filter>) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    filters = newFilters
                )
            )
        }
        refresh()
        _activeWindowType.value = ActiveWindowListingType.LISTING
    }

    fun init(ld: ListingData) {
        viewModelScope.launch {
            searchTitle.value = getString(strings.searchTitle)
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

    fun refresh(){
        resetScroll()
        clearError()
//        pagingRepository.refresh()
    }

    fun changeOpenCategory(complete: Boolean = false) {
        listingCategoryModel.run {
            if (_activeWindowType.value == ActiveWindowListingType.LISTING) {
                listingCategoryModel.updateFromSearchData(_listingData.value.searchData)
                listingCategoryModel.initialize(_listingData.value.data.filters)

                _activeWindowType.value = ActiveWindowListingType.CATEGORY
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

                _activeWindowType.value = ActiveWindowListingType.LISTING
            }
        }
    }

    fun changeOpenSearch(value : Boolean) {
        val sd = _listingData.value.searchData
        if (value) {
            _searchString.value = TextFieldValue(
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

            _activeWindowType.value = ActiveWindowListingType.SEARCH
        }else{
            setSearchFilters()

            _activeWindowType.value = ActiveWindowListingType.LISTING
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
                _activeWindowType.value = ActiveWindowListingType.SEARCH
            } else {
                searchCategoryModel.run {
                    if (_listingData.value.searchData.searchCategoryID != categoryId.value || categories.value.isEmpty()) {
                        updateFromSearchData(_listingData.value.searchData)
                        initialize()
                    }
                }
                _activeWindowType.value = ActiveWindowListingType.CATEGORY_FILTERS
            }
        }
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

    fun setSearchFilters(){
        val searchData = _listingData.value.searchData

        val selectedUser = searchData.userSearch
        val selectedUserLogin = searchData.userLogin
        val selectedUserFinished = searchData.searchFinished

        addHistory(
            _searchString.value.text,
            if(selectedUserLogin == null) selectedUser else false,
            selectedUserFinished
        )

        if (selectedUser && selectedUserLogin == null){
            if (_searchString.value.text != "") {
                searchData.isRefreshing = true
                searchData.userLogin = _searchString.value.text
                searchData.userSearch = selectedUser
                _searchString.value = TextFieldValue()
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

        if (searchData.searchString != _searchString.value.text) {
            searchData.searchString = _searchString.value.text
            searchData.isRefreshing = true
        }

        if (searchData.searchFinished != selectedUserFinished){
            searchData.searchFinished = selectedUserFinished
            searchData.isRefreshing = true
        }

        searchAnalytic(searchData)

        _listingData.value = _listingData.value.copy(searchData = searchData)

        if (searchData.isRefreshing) {
            refresh()
            searchData.isRefreshing = false
        }
    }

    fun deleteHistory() {
        val sh = db.searchHistoryQueries
        sh.deleteAll()
        getHistory()
    }

    fun clearSearch() {
        _searchString.value = TextFieldValue("")
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
        _searchString.value = _searchString.value.copy(text = item.query)

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
        _searchString.value = _searchString.value.copy(text = item.query, selection = TextRange(item.query.length))
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
