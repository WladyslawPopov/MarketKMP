package market.engine.fragments.root.main.proposalPage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.Proposals
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel

class ProposalViewModel: BaseViewModel() {

    private var _responseGetProposal = MutableStateFlow<BodyListPayload<Proposals>?>(null)
    val responseGetProposal : StateFlow<BodyListPayload<Proposals>?> = _responseGetProposal.asStateFlow()

    val firstVisibleItem = MutableStateFlow(0)

    fun getProposal(offerId : Long){
        viewModelScope.launch {
            setLoading(true)
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getProposal(offerId)
                }
                withContext(Dispatchers.Main) {
                    val serializer = BodyListPayload.serializer(Proposals.serializer())
                    val payload: BodyListPayload<Proposals> =
                        deserializePayload(response.payload, serializer)
                    _responseGetProposal.value = payload
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message.toString(), ""))
            } finally {
                setLoading(false)
            }
        }
    }
}
