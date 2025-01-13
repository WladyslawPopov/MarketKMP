package market.engine.fragments.home

import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.fragments.base.BaseViewModel

class HomeViewModel(private val apiService: APIService) : BaseViewModel() {

    private val _responseOffersPromotedOnMainPage1 = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffersPromotedOnMainPage1: StateFlow<List<Offer>> = _responseOffersPromotedOnMainPage1.asStateFlow()

    private val _responseOffersPromotedOnMainPage2 = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffersPromotedOnMainPage2: StateFlow<List<Offer>> = _responseOffersPromotedOnMainPage2.asStateFlow()

    fun getOffersPromotedOnMainPage(page: Int, ipp: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                    apiService.getOffersPromotedOnMainPage(page, ipp)
                }
                val serializer = Payload.serializer(Offer.serializer())
                val payload: Payload<Offer> = deserializePayload(response.payload, serializer)
                when(page){
                    0 -> _responseOffersPromotedOnMainPage1.value = payload.objects
                    1 -> _responseOffersPromotedOnMainPage2.value = payload.objects
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", "An error occurred"))
            } finally {
                setLoading(false)
            }
        }
    }
}
