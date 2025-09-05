package market.engine.fragments.root.main.proposalPage

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.constants.countProposalMax
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MesHeaderItem
import market.engine.core.data.items.ProposalItem
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.Proposals
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getOfferImagePreview
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString



class ProposalViewModel(
    val type: ProposalType,
    val offerId: Long,
    val component: ProposalComponent,
    savedStateHandle: SavedStateHandle
): CoreViewModel(savedStateHandle) {

    private var _responseGetOffer = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetOffer",
        Offer(),
        Offer.serializer()
    )
    val responseGetOffer : StateFlow<Offer> = _responseGetOffer.state

    private val _body = savedStateHandle.getSavedStateFlow(
        scope,
        "body",
        BodyListPayload(),
        BodyListPayload.serializer(Proposals.serializer())
    )
    val body = _body.state

    private val _responseFields = savedStateHandle.getSavedStateFlow(
        scope,
        "responseFields",
        emptyList(),
        ListSerializer(ProposalItem.serializer())
    )
    val responseFields = _responseFields.state

    private val _subtitle = MutableStateFlow(AnnotatedString(""))
    val subtitle = _subtitle.asStateFlow()

    private val _mesHeaderItem = MutableStateFlow(MesHeaderItem())
    val mesHeaderItem = _mesHeaderItem.asStateFlow()

    init {
        update()

        when(type){
            ProposalType.ACT_ON_PROPOSAL ->
                analyticsHelper.reportEvent("view_act_on_proposal_page", mapOf())
            ProposalType.MAKE_PROPOSAL ->
                analyticsHelper.reportEvent("view_make_proposal_page", mapOf())
        }
    }

    fun update(){
        scope.launch {
            _responseFields.value = emptyList()
            _responseGetOffer.value = getOfferById(offerId) ?: Offer()
            refresh()
            getProposal()
        }
    }

    suspend fun getProposal() {
        setLoading(true)
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.postOperation(offerId, "get_proposals", "offers", HashMap())
            }

            if (!response.success) {
                throw ServerErrorException(
                    response.errorCode.toString(),
                    response.humanMessage.toString()
                )
            }

            val serializer = BodyListPayload.serializer(Proposals.serializer())
            val payload: BodyListPayload<Proposals> =
                deserializePayload(response.payload, serializer)

            val offer = responseGetOffer.value

            val prs = payload.bodyList.firstOrNull()

            val makeSubLabel = withContext(Dispatchers.IO){
                getString(strings.subtitleProposalCountLabel)
            }
            val offerLeftLabel = withContext(Dispatchers.IO){
                getString(strings.subtitleOfferCountLabel)
            }
            val countsSign = withContext(Dispatchers.IO){
                getString(strings.countsSign)
            }

            _subtitle.value = buildAnnotatedString {
                when (type) {
                    ProposalType.ACT_ON_PROPOSAL -> {
                        if (offer.id != 1L) {
                            append(offerLeftLabel)
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    color = colors.titleTextColor,
                                    fontWeight = FontWeight.Bold,
                                )
                            ) {
                                append(offer.currentQuantity.toString())
                            }
                            append(" ")
                            append(countsSign)
                        }
                    }

                    ProposalType.MAKE_PROPOSAL -> {
                        val countP =
                            countProposalMax - (prs?.proposals?.filter { !it.isResponserProposal }?.size
                                ?: 0)
                        append(makeSubLabel)

                        withStyle(
                            SpanStyle(
                                color = colors.priceTextColor,
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(" ")
                            append(countP.toString())
                        }
                    }
                }
            }

            _mesHeaderItem.value = MesHeaderItem(
                title = buildAnnotatedString {
                    append(offer.title)
                },
                subtitle = withContext(Dispatchers.IO){
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = colors.grayText,
                            )
                        ) {
                            append(getString(strings.priceParameterName))
                            append(": ")
                        }
                        withStyle(
                            SpanStyle(
                                color = colors.priceTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(offer.currentPricePerItem.toString())
                            append(" ")
                            append(getString(strings.currencyCode))
                        }
                    }
                },
                image = offer.getOfferImagePreview(),
            ) {
                component.goToOffer(offer.id)
            }

            _body.value = payload

            payload.bodyList.forEach {
                getFieldsProposal(it.buyerInfo?.id)
            }

        } catch (exception: ServerErrorException) {
            onError(exception)
        } catch (exception: Exception) {
            onError(ServerErrorException(exception.message.toString(), ""))
        } finally {
            setLoading(false)
        }
    }

    fun getFieldsProposal(buyerId : Long?) {
        scope.launch {
            val buffer = withContext(Dispatchers.IO) {
                operationsMethods.getOperationFields(
                    offerId,
                    if(type == ProposalType.MAKE_PROPOSAL)
                        "make_proposal" else "act_on_proposal",
                    "offers"
                )
            }
            val payload = buffer.success
            val error = buffer.error

            if (payload != null) {
                payload.fields.find { it.widgetType == "select" }?.data = JsonPrimitive(if(buyerId != null) 0 else 2)
                _responseFields.value += ProposalItem(buyerId ?: 0L , payload.fields)
            } else {
                if (error != null) {
                    onError(error)
                }
            }
        }
    }

    fun confirmProposal(buyerId: Long) {
        setLoading(true)
        scope.launch {
            val bodyProposals = HashMap<String,JsonElement>()

            responseFields.value.find { it.userId == buyerId }?.fields?.forEach { field ->
                if (field.data != null){
                    bodyProposals[field.key ?: ""] = field.data!!
                }
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    if (type == ProposalType.MAKE_PROPOSAL)
                        apiService.postOperation(offerId,"make_proposal", "offers", bodyProposals)
                    else
                        apiService.postOperation(offerId,"act_on_proposal", "offers", bodyProposals)
                }

                if (response.success) {
                    val serializer = DynamicPayload.serializer(OperationResult.serializer())
                    val payload: DynamicPayload<OperationResult> =
                        deserializePayload(response.payload, serializer)

                    if (payload.operationResult?.result == "ok") {
                        val eventParams = mapOf(
                            "lot_id" to offerId,
                            "buyer_id" to UserData.login,
                        )

                        when (type) {
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

                        update()
                    } else {
                        val eventParams = mapOf(
                            "lot_id" to offerId,
                            "buyer_id" to UserData.login,
                            "body" to bodyProposals.toString()
                        )

                        when (type) {
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

                        _responseFields.update { field ->
                            field.map {
                                if (it.userId == buyerId) {
                                    it.copy(fields = payload.recipe?.fields ?: payload.fields)
                                } else {
                                    it.copy()
                                }
                            }
                        }
                    }
                }else{
                    throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
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

    fun onValueChange(newField: Fields, id: Long) {
        _responseFields.update { list ->
            list.map { oldField ->
                if (oldField.userId == id) {
                    oldField.copy(
                        fields = oldField.fields.map {
                            if (it.key == newField.key) {
                                newField.copy()
                            }else {
                                it.copy()
                            }
                        }
                    )
                }else{
                    oldField.copy()
                }
            }
        }
    }

    suspend fun getOfferById(offerId: Long) : Offer? {
        return try {
            val response = offerOperations.getOffer(offerId)
            response.success?.let {
                return it
            }
        } catch (_: Exception) {
            null
        }
    }
}
