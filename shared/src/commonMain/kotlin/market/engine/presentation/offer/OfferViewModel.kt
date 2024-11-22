package market.engine.presentation.offer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.presentation.base.BaseViewModel

class OfferViewModel(
    val apiService: APIService,
) : BaseViewModel() {
    private val _responseOffer = MutableStateFlow(Offer())
    val responseOffer: StateFlow<Offer> = _responseOffer.asStateFlow()

    fun getOffer(id: Long) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                    apiService.getOffer(id)
                }
                val payload: ArrayList<Offer> = deserializePayload(response.payload)
                _responseOffer.value = payload[0]
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            } finally {
                setLoading(false)
            }
        }
    }
}
