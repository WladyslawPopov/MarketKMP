package market.engine.core.repositories

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.dialogs.CustomDialogState
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.collections.contains

class OrderRepository(
    val order : Order,
    val type : DealType,
    val events: OrderItemEvents,
    val core: CoreViewModel
) {
    val typeGroup = if (type in arrayOf(
            DealType.BUY_ARCHIVE,
            DealType.BUY_IN_WORK
        )
    ) DealTypeGroup.SELL else DealTypeGroup.BUY

    val orderOperations : OrderOperations by lazy { getKoin().get() }

    private val _customDialogState = MutableStateFlow(CustomDialogState())
    val customDialogState = _customDialogState.asStateFlow()

    private val _operationsList = MutableStateFlow<List<Operations>>(emptyList())

    val menuList = _operationsList.map { operations ->
        val commentText = getString(strings.defaultCommentReport)
        operations.map { operation ->
            MenuItem(
                id = operation.id ?: "",
                title = operation.name ?: "",
                onClick = {
                    core.scope.launch {
                        operation.run {
                            when {
                                !isDataless -> {
                                    withContext(Dispatchers.IO) {
                                            core.getOperationFields(
                                            order.id,
                                            id ?: "",
                                            "orders",
                                        )
                                    }?.let { (t, fields) ->

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
                                    val res = withContext(Dispatchers.IO) {
                                        core.postOperationFields(
                                            order.id,
                                            id ?: "",
                                            "orders"
                                        )
                                    }

                                    if (res) {
                                        val eventParameters = mapOf(
                                            "order_id" to order.id,
                                            "seller_id" to order.sellerData?.id,
                                            "buyer_id" to order.buyerData?.id
                                        )

                                        core.analyticsHelper.reportEvent(
                                            operation.id ?: "",
                                            eventParameters
                                        )
                                        getOperations()
                                        core.setUpdateItem(order.id)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }.stateIn(
        core.scope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val _messageText = MutableStateFlow("")
    val messageText = _messageText.asStateFlow()

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
                core.apiService.getPage(url)
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
            core.onError(exception)
            null
        } catch (exception: Exception) {
            core.onError(
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
        core.scope.launch {
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
                core.setUpdateItem(null)
            }
        }
    }

    fun getOperations() {
        core.scope.launch {
            getOrderOperations(order.id) { listOperations ->
                _operationsList.value = listOperations
            }
        }
    }

    fun getOrderOperations(orderId : Long, onSuccess: (List<Operations>) -> Unit){
        core.scope.launch {
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
        core.scope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.trackId.toString())

            core.showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }

    fun copyOrderId() {
        core.scope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.id.toString())

            core.showToast(
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
        core.scope.launch {
            val userName = if (typeGroup != DealTypeGroup.BUY) {
                order.sellerData?.login ?: ""
            } else {
                order.buyerData?.login ?: ""
            }

            val conversationTitle = getString(strings.createConversationLabel)
            val aboutOrder = getString(strings.aboutOrderLabel)

            val res = withContext(Dispatchers.IO) {
                core.postOperationAdditionalData(
                    order.id,
                    "checking_conversation_existence",
                    "orders"
                )
            }
            if (res != null){
                val dialogId = res.operationResult?.additionalData?.conversationId

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
        }
    }

    fun openOrderDetails() {
        core.scope.launch {
            _customDialogState.value = CustomDialogState(
                title = getString(strings.paymentAndDeliveryLabel),
                typeDialog = "order_details",
            )
        }
    }

    fun makeOperation(type : String){
        core.scope.launch {
            when (type) {
                "send_message" -> {
                    val res = withContext(Dispatchers.IO) {
                        core.postOperationAdditionalData(
                            order.id,
                            "write_to_partner",
                            "orders",
                            hashMapOf("message" to JsonPrimitive(messageText.value)),
                        )
                    }

                    if (res != null) {
                        val dialogId = res.operationResult?.additionalData?.conversationId
                        clearDialogFields()
                        events.goToDialog(dialogId)
                    }
                }

                else -> {
                    val body = HashMap<String, JsonElement>()
                    _customDialogState.value.fields.forEach {
                        if (it.data != null) {
                            body[it.key ?: ""] = it.data!!
                        }
                    }

                    val res = withContext(Dispatchers.IO) {
                        core.postOperationFields(
                            order.id,
                            type,
                            "orders",
                            body = body,
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

                    if (res) {
                        clearDialogFields()
                        getOperations()
                        core.setUpdateItem(order.id)
                    }
                }
            }
        }
    }

    fun showReportDialog(type : String){
        run {
            core.scope.launch {
                val def = getString(strings.toMeFeedbacksLabel)

                val eventParameters = mapOf(
                    "order_id" to order.id.toString(),
                    "buyer_id" to order.buyerData?.id.toString(),
                    "seller_id" to order.sellerData?.id.toString(),
                )

                if (type == def) {
                    core.analyticsHelper.reportEvent("click_review_to_seller", eventParameters)
                } else {
                    core.analyticsHelper.reportEvent("click_review_to_buyer", eventParameters)
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
