package market.engine.fragments.listing

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
import market.engine.core.data.filtersObjects.EmptyFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.paging.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class ListingViewModel(
    private val apiService: APIService,
    private val categoryOperations : CategoryOperations,
    val userRepository: UserRepository
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val regionOptions = mutableStateOf(arrayListOf<Options>())

    private var _responseOffersRecommendedInListing = MutableStateFlow<ArrayList<Offer>?>(null)
    val responseOffersRecommendedInListing : StateFlow<ArrayList<Offer>?> = _responseOffersRecommendedInListing.asStateFlow()

     fun init(listingData: ListingData) : Flow<PagingData<Offer>> {
         listingData.data.value.methodServer = "get_public_listing"
         listingData.data.value.objServer = "offers"

         if (listingData.data.value.filters.isEmpty()) {
             listingData.data.value.filters.addAll(EmptyFilters.getEmpty())
         }

         listingData.data.value.listingType = settings.getSettingValue("listingType", 0) ?: 0

         getRegions()

         getOffersRecommendedInListing(listingData.searchData.value.searchCategoryID)

         getCategories(listingData.searchData.value, listingData.data.value)

        return pagingRepository.getListing(listingData, apiService, Offer.serializer()).cachedIn(viewModelScope)
     }

    fun refresh(){
        pagingRepository.refresh()
    }

    fun updateUserInfo() {
        viewModelScope.launch {
            userRepository.updateUserInfo()
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
}
