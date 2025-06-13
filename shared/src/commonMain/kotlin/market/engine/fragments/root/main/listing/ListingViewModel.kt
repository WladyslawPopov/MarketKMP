package market.engine.fragments.root.main.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomSheetValue
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
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.base.ListingBaseData
import market.engine.fragments.base.ListingBaseEvents
import market.engine.fragments.base.ScrollDataState
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.shared.SearchHistory
import market.engine.widgets.bars.SimpleAppBarData
import market.engine.widgets.filterContents.categories.CategoryViewModel
import market.engine.widgets.items.FilterListingBtnItem
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import kotlin.String


interface SearchEvents {
    fun onRefresh()
    fun goToListing()
    fun onDeleteHistory()
    fun onDeleteHistoryItem(id: Long)
    fun openSearchCategory(value : Boolean)
    fun clearCategory()
    fun clickUser()
    fun clearUser()
    fun clickUserFinished()
    fun onHistoryItemClicked(item: SearchHistoryItem)
    fun editHistoryItem(item: SearchHistoryItem)
    fun onTabSelect(tab: Int)
}

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

    val appBarData: SimpleAppBarData? = null,
    val closeAppBar: SimpleAppBarData? = null,
    val categoryState: CategoryState = CategoryState(),
)

data class FilterBarUiState(
    val listFiltersButtons: List<FilterListingBtnItem> = emptyList(),
    val listNavigation: List<NavigationItem> = emptyList(),
    val isShowFilters: Boolean = true,
    val isShowGrid: Boolean = false,
)

data class ListingStateContent(
    val appBarData: SimpleAppBarData? = null,

    val pagingDataFlow: Flow<PagingData<OfferItem>>? = null,
    val regions: List<Options> = emptyList(),

    val filterBarData: FilterBarUiState = FilterBarUiState(),
    val listingCategoryState: CategoryState = CategoryState(),
    val searchState: SearchUiState = SearchUiState(),
    val searchEvents: SearchEvents? = null,
    val listingBaseData: ListingBaseData? = null,
    val listingBaseEvents: ListingBaseEvents? = null
)

class ListingViewModel(val component: ListingComponent) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _responseOffersRecommendedInListing = MutableStateFlow<List<OfferItem>?>(null)

    private val _responseHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())

    private val _pagingDataFlow = MutableStateFlow<Flow<PagingData<OfferItem>>?>(null)

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())

    private val _changeSearchTab = MutableStateFlow<Int>(0)

    val listingData = MutableStateFlow(ListingData())

    val searchString = mutableStateOf(TextFieldValue(""))

    val errorString = mutableStateOf("")

    val updateFilters = MutableStateFlow(false)

    val searchCategoryModel = CategoryViewModel(
        isFilters = true,
    )

    val listingCategoryModel = CategoryViewModel()

    enum class ActiveWindowType {
        SEARCH, FILTERS, SORTING, CATEGORY, LISTING, CATEGORY_FILTERS
    }

    val activeWindowType = MutableStateFlow(ActiveWindowType.LISTING)

    val listingDataState: StateFlow<ListingStateContent> = combine(
        _responseOffersRecommendedInListing,
        activeWindowType,
        _responseHistory,
        _changeSearchTab,
        listingData
    ) { responseOffersRecommendedInListing, activeWindowType, responseHistory, changeSearchTab, listingData ->
        var searchTitle = getString(strings.searchTitle)
        val searchState =  SearchUiState(
            openSearch = activeWindowType == ActiveWindowType.SEARCH,
            searchData = listingData.searchData,
            searchHistory = responseHistory,
            tabs = buildList {
                add(
                    Tab(getString(strings.searchHistory))
                )
                if (UserData.token != "") {
                    add(Tab(getString(strings.mySubscribedTitle)))
                }
            },
            selectedTabIndex = changeSearchTab,
            appBarData = object : SimpleAppBarData {
                override val modifier: Modifier
                    get() = Modifier
                override val color: Color
                    get() = colors.white
                override val content: @Composable () -> Unit
                    get() = {
                        SearchTextField(
                            activeWindowType == ActiveWindowType.SEARCH,
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
            categoryState = CategoryState(
                openCategory = activeWindowType == ActiveWindowType.CATEGORY_FILTERS,
                categoryViewModel = searchCategoryModel
            )
        )
        Triple(responseOffersRecommendedInListing, listingData, searchState)
    }.combine(
        _regionOptions,
    ) { searchState, regionOptions ->
        val ld = searchState.second
        val userDef = getString(strings.searchUsersSearch)
        val searchData = ld.searchData
        val filters = ld.data.filters.filter { it.interpretation != "" && it.interpretation != null }

        Triple(searchState, regionOptions, FilterBarUiState(
            listFiltersButtons = buildList {
                filters.forEach { filter ->
                    filter.interpretation?.let { text ->
                        add(
                            FilterListingBtnItem(
                                text = text,
                                itemClick = {
                                    activeWindowType.value = ActiveWindowType.FILTERS
                                },
                                removeFilter = {
                                    filters.find {
                                        it.key == filter.key && it.operation == filter.operation
                                    }?.value = ""

                                    filters.find {
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
                                activeWindowType.value = ActiveWindowType.SEARCH
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
                                activeWindowType.value = ActiveWindowType.SEARCH
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
                                activeWindowType.value = ActiveWindowType.SEARCH
                            },
                            removeFilter = {
                                searchData.searchFinished = false
                                component.refresh()
                            }
                        )
                    )
                }
                if (ld.data.sort != null) {
                    add(
                        FilterListingBtnItem(
                            text = getString(strings.searchUserFinishedStringChoice),
                            itemClick = {
                                activeWindowType.value = ActiveWindowType.SORTING
                            },
                            removeFilter = {
                                ld.data.sort = null
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
                        badgeCount = if (filters.isNotEmpty()) filters.size else null,
                        onClick = {
                            activeWindowType.value = ActiveWindowType.FILTERS
                        }
                    )
                )
                add(
                    NavigationItem(
                        title = getString(strings.sort),
                        icon = drawables.sortIcon,
                        tint = colors.black,
                        hasNews = ld.data.sort != null,
                        badgeCount = null,
                        onClick = {
                            activeWindowType.value = ActiveWindowType.SORTING
                        }
                    )
                )
                add(
                    NavigationItem(
                        title = getString(strings.menuTitle),
                        icon = if (ld.data.listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal,
                        tint = colors.black,
                        hasNews = false,
                        badgeCount = null,
                        isVisible = true,
                        onClick = {
                            val newType = if (ld.data.listingType == 0) 1 else 0
                            settings.setSettingValue("listingType", newType)
                            ld.data.listingType = newType
                            updateFilters.value = !updateFilters.value
                            component.refresh()
                        }
                    )
                )
            }
        ))
    }.combine(
        _pagingDataFlow
    ) { state , pagingDataFlow ->
        val subs = getString(strings.subscribersLabel)
        val searchTitle = getString(strings.searchTitle)

        val listingData = state.first.second
        val promoOffers = state.first.first
        val searchState = state.first.third

        val regionOptions = state.second

        val filterState = state.third

        ListingStateContent(
            pagingDataFlow = pagingDataFlow,
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
                                text = if (listingData.searchData.searchCategoryName.isNotEmpty())
                                    listingData.searchData.searchCategoryName
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
                            isVisible = (listingData.searchData.searchCategoryID != 1L || listingData.searchData.userSearch || listingData.searchData.searchString != ""),
                            onClick = {
                                if (UserData.token != "") {
                                    addNewSubscribe(
                                        listingData.data,
                                        listingData.searchData,
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
            listingCategoryState = CategoryState(
                openCategory = activeWindowType.value == ActiveWindowType.CATEGORY,
                categoryViewModel = listingCategoryModel
            ),
            filterBarData = filterState,
            searchState = searchState,
            listingBaseData = ListingBaseData(
                bottomSheetState = bottomSheetState.value,
                listingData = listingData.data,
                searchData = listingData.searchData,
                promoList = promoOffers,
                activeWindowType = activeWindowType.value,
                columns = 1,
                scrollDataState = ScrollDataState(
                    scrollItem = scrollItem.value,
                    offsetScrollItem = offsetScrollItem.value
                )
            ),
            listingBaseEvents = object : ListingBaseEvents {
                override fun changeBottomSheetState(state: BottomSheetValue) {
                    bottomSheetState.value = state
                }
                override fun saveScrollState(state: ScrollDataState) {
                    scrollItem.value = state.scrollItem
                    offsetScrollItem.value = state.offsetScrollItem
                }
            },
            searchEvents =  object : SearchEvents {
                override fun onRefresh() {
                    setLoading(true)
                    onError(ServerErrorException())
                    getHistory(searchString.value.text)
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

                override fun openSearchCategory(value : Boolean) {
                    this@ListingViewModel.openSearchCategory(value)
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

                override fun clickUserFinished() {
                    selectUserFinished()
                }

                override fun onHistoryItemClicked(item: SearchHistoryItem) {
                    this@ListingViewModel.onClickHistoryItem(item)
                }

                override fun editHistoryItem(item: SearchHistoryItem) {
                    this@ListingViewModel.editHistoryItem(item)
                }

                override fun onTabSelect(tab: Int) {
                    component.model.value.searchNavigator.select(tab)
                    changeSearchTab(tab)
                }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ListingStateContent()
    )

    fun clearSearch() {
        searchString.value = TextFieldValue("")
    }

    fun setSearchFilters(){
        val searchData = listingData.value.searchData

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

        listingData.value = listingData.value.copy(searchData = searchData)
    }

    fun init(ld: ListingData) {
        listingData.value = ld

        listingData.value.data.methodServer = "get_public_listing"
        listingData.value.data.objServer = "offers"

        if (listingData.value.data.filters.isEmpty()) {
            listingData.value.data.filters = ListingFilters.getEmpty()
        }

        listingData.value.data.listingType = settings.getSettingValue("listingType", 0) ?: 0

        getRegions()

        getOffersRecommendedInListing(listingData.value.searchData.searchCategoryID)

        _pagingDataFlow.value =
            pagingRepository.getListing(listingData.value, apiService, Offer.serializer())
                .map { offer ->
                    offer.map {
                        if (it.promoOptions != null && it.sellerData?.id != UserData.login) {
                            val isBackLight =
                                it.promoOptions.find { it.id == "backlignt_in_listing" }
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
        listingCategoryModel.run {
            if (activeWindowType.value == ActiveWindowType.LISTING) {
                if (listingData.value.searchData.searchCategoryID != categoryId.value || categories.value.isEmpty()) {
                    listingCategoryModel.updateFromSearchData(listingData.value.searchData)
                    listingCategoryModel.initialize(listingData.value.data.filters)
                }

                activeWindowType.value = ActiveWindowType.CATEGORY
                val eventParameters = mapOf(
                    "category_name" to listingData.value.searchData.searchCategoryName,
                    "category_id" to listingData.value.searchData.searchCategoryID,
                )
                analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
            } else {

                if (listingData.value.searchData.searchCategoryID != categoryId.value) {
                    listingData.update { currentListingData ->
                        currentListingData.copy(
                            searchData = currentListingData.searchData.copy(
                                searchCategoryID = categoryId.value,
                                searchCategoryName = categoryName.value,
                                searchParentID = parentId.value,
                                searchIsLeaf = isLeaf.value,
                            )
                        )
                    }

                    component.refresh()
                }

                activeWindowType.value = ActiveWindowType.LISTING
            }
        }
    }

    fun changeOpenSearch(value : Boolean) {
        val sd = listingData.value.searchData
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

            activeWindowType.value = ActiveWindowType.SEARCH
        }else{
            setSearchFilters()
            if (sd.isRefreshing) {
                component.refresh()
                sd.isRefreshing = false
            }
            activeWindowType.value = ActiveWindowType.LISTING
        }
    }

    fun openSearchCategory(value : Boolean) {
        searchCategoryModel.run {
            if (!value) {
                if (listingData.value.searchData.searchCategoryID != categoryId.value) {
                    listingData.update { currentListingData ->
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
                activeWindowType.value = ActiveWindowType.SEARCH
            } else {
                searchCategoryModel.run {
                    if (listingData.value.searchData.searchCategoryID != categoryId.value || categories.value.isEmpty()) {
                        updateFromSearchData(listingData.value.searchData)
                        initialize()
                    }
                }
                activeWindowType.value = ActiveWindowType.CATEGORY_FILTERS
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
        listingData.update { currentListingData ->
            currentListingData.searchData.clear(catDef.value)
            currentListingData.copy()
        }
    }

    fun changeSearchTab(tab: Int) {
        _changeSearchTab.value = tab
    }

    fun selectUserSearch(){
        listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    userSearch = !currentListingData.searchData.userSearch
                )
            )
        }
    }

    fun selectUserFinished(){
        listingData.update { currentListingData ->
            currentListingData.copy(
                searchData = currentListingData.searchData.copy(
                    searchFinished = !currentListingData.searchData.searchFinished
                )
            )
        }
    }

    fun clearUserSearch(){
        listingData.update { currentListingData ->
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

        listingData.update { currentListingData ->
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
