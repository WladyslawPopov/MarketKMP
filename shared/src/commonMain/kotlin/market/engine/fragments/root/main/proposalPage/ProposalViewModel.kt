package market.engine.fragments.root.main.proposalPage

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.Proposals
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class ProposalViewModel: BaseViewModel() {

    private var _responseGetOffer = MutableStateFlow(Offer())
    val responseGetOffer : StateFlow<Offer> = _responseGetOffer.asStateFlow()

    val body = mutableStateOf<BodyListPayload<Proposals>?>(null)

    val firstVisibleItem = MutableStateFlow(0)

    val rememberFields = mutableStateOf<MutableMap<Long, ArrayList<Fields>?>>(mutableMapOf())

    val rememberChoice = mutableStateOf<MutableMap<Long, Int>>(mutableMapOf())

    fun getProposal(offerId : Long, onSuccess: (BodyListPayload<Proposals>) -> Unit, error: () -> Unit){
        viewModelScope.launch {
            setLoading(true)
            try {
                val response = withContext(Dispatchers.IO) {
                    _responseGetOffer.value = getOfferById(offerId) ?: Offer()
                    apiService.getProposal(offerId)
                }
                withContext(Dispatchers.Main) {
                    val serializer = BodyListPayload.serializer(Proposals.serializer())
                    val payload: BodyListPayload<Proposals> =
                        deserializePayload(response.payload, serializer)
                    onSuccess(payload)
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
                error()
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message.toString(), ""))
                error()
            } finally {
                setLoading(false)
            }
        }
    }

    suspend fun getFieldsProposal(offerId : Long, buyerId : Long, proposalType : ProposalType) : ArrayList<Fields>?{
        val buffer = withContext(Dispatchers.IO) {
            if (proposalType == ProposalType.MAKE_PROPOSAL) {
                offerOperations.getMakeProposal(offerId)
            } else {
                offerOperations.getActOnProposal(offerId)
            }
        }
        val payload = buffer.success
        val error = buffer.error
        return withContext(Dispatchers.Main) {
            if (payload != null) {
                rememberChoice.value = if (buyerId == 0L) {
                    mutableMapOf(Pair(buyerId, 2))
                }else{
                    mutableMapOf(Pair(buyerId, 0))
                }
                return@withContext payload.fields
            }else{
                if (error != null) {
                    onError(error)
                }
                return@withContext null
            }
        }
    }

    fun confirmProposal(offerId : Long, proposalType : ProposalType, fields: ArrayList<Fields>, onSuccess: () -> Unit, onError: (ArrayList<Fields>) -> Unit) {
        setLoading(true)
        viewModelScope.launch {
            val bodyProposals = HashMap<String,JsonElement>()

            fields.forEach { field ->
                if (field.data != null){
                    bodyProposals[field.key ?: ""] = field.data!!
                }
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    if (proposalType == ProposalType.MAKE_PROPOSAL)
                        apiService.postMakeProposal(offerId, bodyProposals)
                    else
                        apiService.postActOnProposal(offerId, bodyProposals)
                }

                withContext(Dispatchers.Main) {
                    if (response.success) {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload: DynamicPayload<OperationResult> =
                            deserializePayload(response.payload, serializer)

                        if (payload.operationResult?.result == "ok") {
                            val eventParams = mapOf(
                                "lot_id" to offerId,
                                "buyer_id" to UserData.login,
                            )

                            when (proposalType) {
                                ProposalType.MAKE_PROPOSAL -> analyticsHelper.reportEvent(
                                    "make_proposal",
                                    eventParams
                                )

                                ProposalType.ACT_ON_PROPOSAL -> analyticsHelper.reportEvent(
                                    "act_on_proposal",
                                    eventParams
                                )
                            }

                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )

                            onSuccess()
                        } else {
                            val eventParams = mapOf(
                                "lot_id" to offerId,
                                "buyer_id" to UserData.login,
                                "body" to bodyProposals.toString()
                            )

                            when (proposalType) {
                                ProposalType.MAKE_PROPOSAL -> analyticsHelper.reportEvent(
                                    "make_proposal_failed",
                                    eventParams
                                )

                                ProposalType.ACT_ON_PROPOSAL -> analyticsHelper.reportEvent(
                                    "act_on_proposal_failed",
                                    eventParams
                                )
                            }

                            showToast(
                                errorToastItem.copy(
                                    message = getString(strings.operationFailed)
                                )
                            )

                            onError(payload.recipe?.fields ?: payload.fields)
                        }
                    }else{
                        throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                    }
                }
            }catch (e : ServerErrorException) {
                onError(e)
            } catch (e : Exception){
                 onError(ServerErrorException(e.message.toString(), ""))
            }finally {
                setLoading(false)
            }
        }
    }
}
