package market.engine.fragments.root.main.listing

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.SearchHistory

class ListingViewModel(private val db : MarketDB) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val regionOptions = mutableStateOf(arrayListOf<Options>())

    val listingData = mutableStateOf(ListingData())

    val openCategory = mutableStateOf(false)
    val openSearch = mutableStateOf(false)

    private var _responseOffersRecommendedInListing = MutableStateFlow<ArrayList<Offer>?>(null)
    val responseOffersRecommendedInListing : StateFlow<ArrayList<Offer>?> = _responseOffersRecommendedInListing.asStateFlow()

    private val _responseHistory = MutableStateFlow<List<SearchHistoryItem>>(emptyList())
    val responseHistory: StateFlow<List<SearchHistoryItem>> = _responseHistory.asStateFlow()

     fun init(listingData: ListingData) : Flow<PagingData<Offer>> {
         this.listingData.value = listingData

         listingData.data.value.methodServer = "get_public_listing"
         listingData.data.value.objServer = "offers"

         if (listingData.data.value.filters.isEmpty()) {
             listingData.data.value.filters = ListingFilters.getEmpty()
         }

         listingData.data.value.listingType = settings.getSettingValue("listingType", 0) ?: 0

         getRegions()

         getOffersRecommendedInListing(listingData.searchData.value.searchCategoryID)

         getHistory()

         return pagingRepository.getListing(listingData, apiService, Offer.serializer())
             .cachedIn(viewModelScope)
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
                        _responseOffersRecommendedInListing.value = payload.objects
                    }catch (e : Exception){
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
                if (res != null) {
                    res.firstOrNull()?.options?.sortedBy { it.weight }?.let { regionOptions.value.addAll(it) }
                }
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
