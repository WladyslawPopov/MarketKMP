package market.engine.core.repositories

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.fragments.root.main.profile.myOrders.MyOrdersViewModel
import market.engine.widgets.dialogs.CustomDialogState
import org.jetbrains.compose.resources.getString


class OrderRepository(
    val order : Order,
    val type : DealType,
    val viewModel: MyOrdersViewModel,
    val events: OrderItemEvents
) {
    val typeGroup = if (type in arrayOf(
            DealType.BUY_ARCHIVE,
            DealType.BUY_IN_WORK
        )
    ) DealTypeGroup.SELL else DealTypeGroup.BUY

    private val _customDialogState = MutableStateFlow(CustomDialogState())
    val customDialogState = _customDialogState.asStateFlow()

    private val _menuList = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuList: StateFlow<List<MenuItem>> = _menuList.asStateFlow()

    private val _messageText = MutableStateFlow(TextFieldValue(""))
    val messageText = _messageText.asStateFlow()

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
                viewModel.apiService.getPage(url)
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
            viewModel.onError(exception)
            null
        } catch (exception: Exception) {
            viewModel.onError(
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
        viewModel.viewModelScope.launch {
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
                viewModel.setUpdateItem(null)
            }
        }
    }

    fun getOperations() {
        viewModel.viewModelScope.launch {
            viewModel.getOrderOperations(order.id) { listOperations ->
                _menuList.value = buildList {
                    addAll(listOperations.map { operation ->
                        MenuItem(
                            id = operation.id ?: "",
                            title = operation.name ?: "",
                            onClick = {
                                operation.run {
                                    when{
                                        !isDataless -> {
                                            viewModel.getOperationFields(
                                                order.id,
                                                id ?: "",
                                                "orders",
                                            )
                                            { t, f ->
                                                _customDialogState.value = CustomDialogState(
                                                    title = AnnotatedString(t),
                                                    typeDialog = id?: "",
                                                    fields = f,
                                                    onDismiss = {
                                                        clearDialogFields()
                                                    },
                                                    onSuccessful = {
                                                        val body = HashMap<String, JsonElement>()
                                                        f.forEach {
                                                            if (it.data != null) {
                                                                body[it.key ?: ""] = it.data!!
                                                            }
                                                        }

                                                        viewModel.postOperationFields(
                                                            order.id,
                                                            id ?: "",
                                                            "orders",
                                                            body = body,
                                                            onSuccess = {
                                                                updateItem(order)
                                                                clearDialogFields()
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
                                                )
                                            }
                                        }
                                        else -> {
                                            viewModel.postOperationFields(
                                                order.id,
                                                id ?: "",
                                                "orders",
                                                onSuccess = {
                                                    val eventParameters = mapOf(
                                                        "order_id" to order.id,
                                                        "seller_id" to order.sellerData?.id,
                                                        "buyer_id" to order.buyerData?.id
                                                    )

                                                    viewModel.analyticsHelper.reportEvent(
                                                        operation.id ?: "",
                                                        eventParameters
                                                    )

                                                    viewModel.setUpdateItem(order.id)
                                                },
                                                errorCallback = {

                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    })
                }
            }
        }
    }

    fun clearDialogFields(){
        _customDialogState.value = CustomDialogState()
    }

    fun copyTrackId() {
        viewModel.viewModelScope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.trackId.toString())

            viewModel.showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }

    fun copyOrderId() {
        viewModel.viewModelScope.launch {
            val idString = getString(strings.idCopied)
            clipBoardEvent(order.id.toString())

            viewModel.showToast(
                ToastItem(
                    isVisible = true,
                    message = idString,
                    type = ToastType.SUCCESS
                )
            )
        }
    }

    fun setMessageText(text : TextFieldValue){
        _messageText.value = text
    }

    fun sendMessage() {
        viewModel.viewModelScope.launch {
            val userName = if (typeGroup != DealTypeGroup.BUY) {
                order.sellerData?.login ?: ""
            } else {
                order.buyerData?.login ?: ""
            }

            val conversationTitle = getString(strings.createConversationLabel)
            val aboutOrder = getString(strings.aboutOrderLabel)

            viewModel.postOperationAdditionalData(
                order.id,
                "checking_conversation_existence",
                "orders",
                onSuccess = { body ->
                    val dialogId = body?.operationResult?.additionalData?.conversationId
                    if (dialogId != null) {
                        events.goToDialog(dialogId)
                    } else {
                        _customDialogState.value = CustomDialogState(
                            title = buildAnnotatedString {
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
                            },
                            typeDialog = "send_message",
                            onDismiss = {
                                clearDialogFields()
                            },
                            onSuccessful = {
                                viewModel.postOperationAdditionalData(
                                    order.id,
                                    "write_to_partner",
                                    "orders",
                                    hashMapOf("message" to JsonPrimitive(messageText.value.text)),
                                    onSuccess = {
                                        val dialogId = it?.operationResult?.additionalData?.conversationId
                                        clearDialogFields()
                                        events.goToDialog(dialogId)
                                    }
                                )
                            }
                        )
                    }
                }
            )
        }
    }

    fun openOrderDetails() {
        viewModel.viewModelScope.launch {
            _customDialogState.value = CustomDialogState(
                title = AnnotatedString(getString(strings.paymentAndDeliveryLabel)),
                typeDialog = "order_details",
                onDismiss = {
                    clearDialogFields()
                }
            )
        }
    }

    fun showReportDialog(type : String){
        viewModel.run {
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
                    title = AnnotatedString(type),
                    typeDialog = if(type == def) "show_report_to_me" else "show_my_report",
                    onDismiss = {
                        clearDialogFields()
                    }
                )
            }
        }
    }
}
