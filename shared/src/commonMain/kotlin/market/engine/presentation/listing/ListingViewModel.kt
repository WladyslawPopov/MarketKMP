package market.engine.presentation.listing

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
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class ListingViewModel(
    private val apiService: APIService,
    private val offerPagingRepository: OfferPagingRepository,
) : BaseViewModel() {

    private val categoryOperations : CategoryOperations = getKoin().get()

    var listingData = mutableStateOf(CategoryBaseFilters.filtersData)

    val regionOptions = mutableStateOf(arrayListOf<Options>())

    val pagingDataFlow : Flow<PagingData<Offer>>

    private var _responseOffersRecommendedInListing = MutableStateFlow<ArrayList<Offer>?>(null)
    val responseOffersRecommendedInListing : StateFlow<ArrayList<Offer>?> = _responseOffersRecommendedInListing.asStateFlow()

     init {
         listingData.value.data.value.methodServer = "get_public_listing"
         if (listingData.value.data.value.filters.isNullOrEmpty()) {
             listingData.value.data.value.filters = arrayListOf()
             listingData.value.data.value.filters?.addAll(EmptyFilters.getEmpty())
         }
         listingData.value.data.value.listingType = settings.getSettingValue("listingType", 0) ?: 0
         pagingDataFlow = offerPagingRepository.getListing(listingData.value).cachedIn(viewModelScope)
         getRegions()

         firstVisibleItemScrollOffset = 0
         firstVisibleItemIndex = 0
         getOffersRecommendedInListing(listingData.value.searchData.value.searchCategoryID ?: 1L)
     }

    fun refresh(){
        offerPagingRepository.refresh()
    }

    private fun getOffersRecommendedInListing(categoryID:Long) {
        viewModelScope.launch{
            try {
                withContext(Dispatchers.IO){
                    val response = apiService.getOffersRecommendedInListing(categoryID)

                    withContext(Dispatchers.Main) {
                        try {
                            val payload : Payload<Offer> = deserializePayload(response.payload)
                            _responseOffersRecommendedInListing.value = payload.objects
                        }catch (e : Exception){
                            throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                        }
                    }
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }

    private fun getRegions(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val res = categoryOperations.getRegions()
                withContext(Dispatchers.Main) {
                    if (res != null) {
                        res.firstOrNull()?.options?.sortedBy { it.weight }
                            ?.let { regionOptions.value.addAll(it) }
                    }
                }
            }
        }
    }
}
