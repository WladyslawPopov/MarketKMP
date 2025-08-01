package market.engine.core.repositories

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.clipBoardEvent
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.events.OrderItemEvents
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.UrlBuilder
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.dialogs.CustomDialogState
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.collections.contains


class OrderBaseViewModel(
    val order : Order,
    val type : DealType,
    val events: OrderItemEvents,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {
    val typeGroup = if (type in arrayOf(
            DealType.BUY_ARCHIVE,
            DealType.BUY_IN_WORK
        )
    ) DealTypeGroup.SELL else DealTypeGroup.BUY

    val orderOperations : OrderOperations by lazy { getKoin().get() }

    private val _customDialogState = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "customDialogState",
        CustomDialogState(),
        CustomDialogState.serializer()
    )
    val customDialogState = _customDialogState.state



    private val _operationsList = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "operationsList",
        emptyList(),
        ListSerializer(Operations.serializer())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val menuList = _operationsList.state.flatMapLatest { operations ->

        val commentText = getString(strings.defaultCommentReport)

        flowOf(
            operations.map { operation ->
                MenuItem(
                    id = operation.id ?: "",
                    title = operation.name ?: "",
                    onClick = {
                        operation.run {
                            when {
                                !isDataless -> {
                                    getOperationFields(
                                        order.id,
                                        id ?: "",
                                        "orders",
                                    )
                                    { t, fields ->

                                        val fieldFeedbackType =
                                            fields.find { it.key == "feedback_type" }
                                        if (fieldFeedbackType != null) {
                                            fields.find { it.key == "comment" }?.let {
                                                fieldFeedbackType.data = JsonPrimitive(1)
                                                it.data = JsonPrimitive(commentText)
                                            }
                                        }

                                        _customDialogState.value = CustomDialogState(
                                            title = t,
                                            typeDialog = id ?: "",
                                            fields = fields
                                        )
                                    }
                                }

                                else -> {
                                    postOperationFields(
                                        order.id,
                                        id ?: "",
                                        "orders",
                                        onSuccess = {
                                            val eventParameters = mapOf(
                                                "order_id" to order.id,
                                                "seller_id" to order.sellerData?.id,
                                                "buyer_id" to order.buyerData?.id
                                            )

                                            analyticsHelper.reportEvent(
                                                operation.id ?: "",
                                                eventParameters
                                            )
                                            getOperations()
                                            setUpdateItem(order.id)
                                        },
                                        errorCallback = {

                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val _messageText = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "messageText",
        "",
        String.serializer()
    )
    val messageText = _messageText.state

    val annotatedTitle = mutableStateOf(AnnotatedString(""))

    init {
        getOperations()
    }

    private suspend fun getItem(id : Long): Order? {
        return try {
            val ld = ListingData()
            ld.data.filters = DealFilters.getByTypeFilter(type)

            val method = if (type in arrayOf(
                    DealType.BUY_ARCHIVE,
                    DealType.BUY_IN_WORK
                )
            ) "purchases" else "sales"
            ld.data.objServer = "orders"

            ld.data.methodServer = "get_cabinet_listing_$method"

            ld.data.filters.find { it.key == "id" }?.value = id.toString()
            ld.data.filters.find { it.key == "id" }?.interpretation = ""

            val url = UrlBuilder()
                .addPathSegment(ld.data.objServer)
                .addPathSegment(ld.data.methodServer)
                .addFilters(ld.data, ld.searchData)
                .build()

            val res = withContext(Dispatchers.IO) {
                apiService.getPage(url)
            }
            return withContext(Dispatchers.Main) {
                ld.data.filters.find { it.key == "id" }?.value = ""
                ld.data.filters.find { it.key == "id" }?.interpretation = null

                if (res.success) {
                    val serializer = Payload.serializer(Order.serializer())
                    val payload = deserializePayload(res.payload, serializer)


                    return@withContext payload.objects.firstOrNull()
                }else{
                    return@withContext null
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            null
        }
    }

    fun updateItem(oldOrder: Order) {
        getOperations()
        viewModelScope.launch {
            val buf = withContext(Dispatchers.IO) {
                getItem(oldOrder.id)
            }

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    oldOrder.owner = buf.owner
                    oldOrder.trackId = buf.trackId
                    oldOrder.marks = buf.marks
                    oldOrder.feedbacks = buf.feedbacks
                    oldOrder.comment = buf.comment
                    oldOrder.paymentMethod = buf.paymentMethod
                    oldOrder.deliveryMethod = buf.deliveryMethod
                    oldOrder.deliveryAddress = buf.deliveryAddress
                    oldOrder.dealType = buf.dealType
                    oldOrder.lastUpdatedTs = buf.lastUpdatedTs
                }else {
                    oldOrder.owner = 1L
                }
                setUpdateItem(null)
            }
        }
    }

    fun getOperations() {
        viewModelScope.launch {
            getOrderOperations(order.id) { listOperations ->
                _operationsList.value = listOperations
            }
        }
    }

    fun getOrderOperations(orderId : Long, onSuccess: (List<Operations>) -> Unit){
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) { orderOperations.getOperationsOrder(orderId) }
            withContext(Dispatchers.Main){
                val buf = res.success?.filter {
                    it.id !in listOf("refund")
                }
                if (buf != null) {
                    onSuccess(buf)
                }
            }
        }
    }

    fun clearDialogFields(){
        _customDialogState.value = CustomDialogState()
    }

    fun copyTrackId() {
        viewModelScope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.trackId.toString())

            showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }

    fun copyOrderId() {
        viewModelScope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.id.toString())

            showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }

    fun setMessageText(text : String){
        _messageText.value = text
    }

    fun sendMessage() {
        viewModelScope.launch {
            val userName = if (typeGroup != DealTypeGroup.BUY) {
                order.sellerData?.login ?: ""
            } else {
                order.buyerData?.login ?: ""
            }

            val conversationTitle = getString(strings.createConversationLabel)
            val aboutOrder = getString(strings.aboutOrderLabel)

            postOperationAdditionalData(
                order.id,
                "checking_conversation_existence",
                "orders",
                onSuccess = { body ->
                    val dialogId = body?.operationResult?.additionalData?.conversationId
                    if (dialogId != null) {
                        events.goToDialog(dialogId)
                    } else {
                        annotatedTitle.value = buildAnnotatedString {
                            withStyle(SpanStyle(
                                color = colors.grayText,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(
                                    conversationTitle
                                )
                            }

                            withStyle(SpanStyle(
                                color = colors.actionTextColor,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(" $userName ")
                            }

                            withStyle(SpanStyle(
                                color = colors.grayText,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(aboutOrder)
                            }

                            withStyle(SpanStyle(
                                color = colors.titleTextColor,
                            )) {
                                append(" #${order.id}")
                            }
                        }

                        _customDialogState.value = CustomDialogState(
                            title = "",
                            typeDialog = "send_message",
                        )
                    }
                }
            )
        }
    }

    fun openOrderDetails() {
        viewModelScope.launch {
            _customDialogState.value = CustomDialogState(
                title = getString(strings.paymentAndDeliveryLabel),
                typeDialog = "order_details",
            )
        }
    }

    fun makeOperation(type : String){
        when(type){
            "write_to_partner" -> {
                postOperationAdditionalData(
                    order.id,
                    "write_to_partner",
                    "orders",
                    hashMapOf("message" to JsonPrimitive(messageText.value)),
                    onSuccess = {
                        val dialogId = it?.operationResult?.additionalData?.conversationId
                        clearDialogFields()
                        events.goToDialog(dialogId)
                    }
                )
            }
            else -> {
                val body = HashMap<String, JsonElement>()
                _customDialogState.value.fields.forEach {
                    if (it.data != null) {
                        body[it.key ?: ""] = it.data!!
                    }
                }

                postOperationFields(
                    order.id,
                    type,
                    "orders",
                    body = body,
                    onSuccess = {
                        clearDialogFields()
                        getOperations()
                        setUpdateItem(order.id)
                    },
                    errorCallback = { errFields ->
                        if (errFields != null) {
                            _customDialogState.update {
                                it.copy(
                                    fields = errFields
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    fun showReportDialog(type : String){
        run {
            viewModelScope.launch {
                val def = getString(strings.toMeFeedbacksLabel)

                val eventParameters = mapOf(
                    "order_id" to order.id.toString(),
                    "buyer_id" to order.buyerData?.id.toString(),
                    "seller_id" to order.sellerData?.id.toString(),
                )

                if (type == def) {
                    analyticsHelper.reportEvent("click_review_to_seller", eventParameters)
                } else {
                    analyticsHelper.reportEvent("click_review_to_buyer", eventParameters)
                }

                _customDialogState.value = CustomDialogState(
                    title = type,
                    typeDialog = if(type == def) "show_report_to_me" else "show_my_report",
                )
            }
        }
    }

    fun setNewField(field : Fields){
        _customDialogState.update {
            it.copy(fields = it.fields.map { oldField ->
                if (oldField.key == field.key) field.copy() else oldField.copy()
            })
        }
    }
}
