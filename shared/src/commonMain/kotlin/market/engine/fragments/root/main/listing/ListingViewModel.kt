package market.engine.fragments.root.main.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.SearchHistory
import market.engine.widgets.bars.SimpleAppBarData
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.getString

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
    val searchIsLeaf: Boolean = false,
    val isRefreshing: Boolean = false,
    val openSearch: Boolean = false,
    val openCategory: Boolean = false,
    val appBarData: SimpleAppBarData? = null,
)

data class ListingDataUiState(
    val pagingDataFlow: Flow<PagingData<OfferItem>>? = null,
    val promoOffers: List<OfferItem>? = null,
    val regions: List<Options> = emptyList(),
)

data class ListingUiState(
    val openCategory: Boolean = false,
    val isLoading: Boolean = false,
    val error: ServerErrorException = ServerErrorException(),
)

class ListingViewModel : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _responseOffersRecommendedInListing = MutableStateFlow<List<OfferItem>?>(null)

    private val _responseHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())

    private val _pagingDataFlow = MutableStateFlow<Flow<PagingData<OfferItem>>?>(null)

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())

    val listingData = mutableStateOf(ListingData())

    val searchData = MutableStateFlow(SD())

    private val _openCategory = MutableStateFlow(false)

    private val _openSearch = MutableStateFlow(false)

    private val openSearchCategory = MutableStateFlow(false)

    private var searchTitle = ""

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

    val uiState: StateFlow<ListingUiState> = combine(
        _openCategory,
        isShowProgress,
        errorMessage
    ) { openCategory, isLoading, error ->
        searchData.value = listingData.value.searchData.value
        ListingUiState(
            openCategory = openCategory,
            isLoading = isLoading,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ListingUiState()
    )

    val searchString = mutableStateOf(TextFieldValue(""))

    val uiSearchState: StateFlow<SearchUiState> = combine(
        _responseHistory,
        _openSearch,
        searchData,
        openSearchCategory
    ) { responseHistory, openSearch,searchData, openSearchCategory ->
//        searchData.value = listingData.value.searchData.value
//        searchString.value = TextFieldValue(searchData.value.searchString, TextRange(searchData.value.searchString.length))
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
            searchParentID = searchData.searchParentID ?: 1,
            searchParentName = searchData.searchParentName,
            appBarData = object : SimpleAppBarData {
                override val modifier: Modifier
                    get() = Modifier
                override val content: @Composable () -> Unit
                    get() = {
                        SearchTextField(
                            openSearch,
                            searchString,
                            onUpdateHistory = { string ->
                                getHistory(string)
                            },
                            goToListing = {
                                setSearchFilters()
                                changeOpenSearch(false)
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
                                    setSearchFilters()
                                    changeOpenSearch(false)
                                }
                        )
                    )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchUiState()
    )

    init {
        viewModelScope.launch {
            searchTitle = getString(strings.searchTitle)
        }
    }

    fun setSearchFilters(){
        val selectedUser = searchData.value.userSearch
        val selectedUserLogin = searchData.value.userLogin
        val selectedUserFinished = searchData.value.searchFinished

        val searchData = searchData.value.copy()

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

        listingData.value.searchData.value = searchData
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

    fun changeOpenCategory(value : Boolean){
        _openCategory.value = value

        if (value) {
            val eventParameters = mapOf(
                "category_name" to searchData.value.searchCategoryName,
                "category_id" to searchData.value.searchCategoryID,
            )
            analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
        }
    }

    fun changeOpenSearch(value : Boolean){
        _openSearch.value = value
        val searchData = searchData.value
        if (value) {
            val eventParameters = mapOf(
                "search_string" to searchData.searchString,
                "category_id" to searchData.searchCategoryID,
                "category_name" to searchData.searchCategoryName,
                "user_login" to searchData.userLogin,
                "user_search" to searchData.userSearch,
                "user_finished" to searchData.searchFinished
            )
            analyticsHelper.reportEvent("open_search_listing", eventParameters)

            getHistory()
        }
    }

    fun refresh(){
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

    fun openSearchCategory() {
        openSearchCategory.value = true
    }

    fun clearSearchCategory() {
        val sd = searchData.value.copy()
        sd.searchCategoryID = 1L
        sd.searchCategoryName = catDef.value
        sd.clear(catDef.value)
        searchData.value = sd
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
