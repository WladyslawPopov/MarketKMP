package market.engine.fragments.root.main.proposalPage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.Proposals
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel

class ProposalViewModel: BaseViewModel() {

    private var _responseGetOffer = MutableStateFlow<Offer?>(null)
    val responseGetOffer : StateFlow<Offer?> = _responseGetOffer.asStateFlow()

    private var _responseGetProposal = MutableStateFlow<BodyListPayload<Proposals>?>(null)
    val responseGetProposal : StateFlow<BodyListPayload<Proposals>?> = _responseGetProposal.asStateFlow()

    private var _responseGetFields = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetFields : StateFlow<DynamicPayload<OperationResult>?> = _responseGetFields.asStateFlow()

    val firstVisibleItem = MutableStateFlow(0)

    fun getProposal(offerId : Long, proposalType: ProposalType){
        viewModelScope.launch {
            setLoading(true)
            try {
                val response = withContext(Dispatchers.IO) {
                    getFieldsProposal(offerId, proposalType)
                    _responseGetOffer.value = getOfferById(offerId)
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

    private fun getFieldsProposal(offerId : Long, proposalType : ProposalType){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val buffer = if (proposalType == ProposalType.MAKE_PROPOSAL) {
                    offerOperations.getMakeProposal(offerId)
                } else {
                    offerOperations.getActOnProposal(offerId)
                }
                val payload = buffer.success
                val error = buffer.error
                withContext(Dispatchers.Main) {
                    if (payload != null) {
                        _responseGetFields.value = payload
                    }else{
                        if (error != null) {
                            onError(error)
                        }
                    }
                }
            }
        }
    }
}
