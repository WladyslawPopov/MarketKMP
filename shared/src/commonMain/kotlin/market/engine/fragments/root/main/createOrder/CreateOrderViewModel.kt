package market.engine.fragments.root.main.createOrder

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class CreateOrderViewModel: BaseViewModel() {

    private var _responsePostPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseCreateOrder : StateFlow<DynamicPayload<OperationResult>?> = _responsePostPage.asStateFlow()

    private var _responseGetOffers = MutableStateFlow<List<Offer>>(emptyList())
    val responseGetOffers : StateFlow<List<Offer>> = _responseGetOffers.asStateFlow()

    private var _responseGetAdditionalData = MutableStateFlow<AdditionalDataForNewOrder?>(null)
    val responseGetAdditionalData  : StateFlow<AdditionalDataForNewOrder?> = _responseGetAdditionalData.asStateFlow()

    val selectDeliveryMethod = mutableStateOf(0)
    val selectDealType = mutableStateOf(0)
    val selectPaymentType = mutableStateOf(0)

    val responseGetLoadCards = mutableStateOf(emptyList<DeliveryAddress>())
    val deliveryFields = mutableStateOf<List<Fields>>(emptyList())

    fun updateDeliveryFields() {
        viewModelScope.launch {
            responseGetLoadCards.value = getDeliveryCards() ?: emptyList()
            deliveryFields.value = getDeliveryFields() ?: emptyList()
        }
    }

    fun getOffers(listOffersId : List<Long>) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    _responseGetOffers.value = emptyList()
                    setLoading(true)
                    listOffersId.forEach {
                        val response = offerOperations.getOffer(it)

                        if (response.success != null){
                            _responseGetOffers.value += response.success!!
                        }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            } finally {
                setLoading(false)
            }
        }
    }

    fun getAdditionalFields(sellerId: Long, lotIds: List<Long>?, lotCounts: List<Int>?) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val additionalBody = buildJsonObject {
                    put("seller_id", JsonPrimitive(sellerId))
                    val cartItems = arrayListOf<JsonObject>()
                    if (lotIds != null && lotCounts != null) {
                        for (i in lotIds.indices) {
                            val offer = buildJsonObject {
                                put("offer_id", JsonPrimitive(lotIds[i]))
                                put("quantity", JsonPrimitive(lotCounts[i]))
                            }
                            cartItems.add(offer)
                        }
                    }
                    put("internal_cart", JsonArray(cartItems))
                }

                val buf = withContext(Dispatchers.IO) {
                   userOperations.postUserOperationsGetAdditionalDataBeforeCreateOrder(
                        UserData.login,
                        additionalBody
                   )
                }

                val addData = buf.success
                val error = buf.error

                if (addData != null) {
                    if (addData.operationResult?.result == "ok") {
                        addData.operationResult.additionalData?.let { data ->
                            _responseGetAdditionalData.value = data
                            selectDealType.value = data.dealTypes.firstOrNull()?.code ?: 0
                            selectPaymentType.value = data.paymentMethods.firstOrNull()?.code ?: 0
                            selectDeliveryMethod.value = data.deliveryMethods.firstOrNull()?.code ?: 0
                        }
                    }
                }else{
                    throw error ?: ServerErrorException(errorCode = "Error", humanMessage = "")
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            } finally {
                setLoading(false)
            }
        }
    }

    fun postPage(deliveryFields : List<Fields>, basketItem:  Pair<Long, List<SelectedBasketItem>>) {
        val items = basketItem.second

        val jsonBody = buildJsonObject {
            put("seller_id", JsonPrimitive(basketItem.first))

            val cartItems = arrayListOf<JsonObject>()
            items.forEach { item ->
                val offer = buildJsonObject {
                    put("offer_id", JsonPrimitive(item.offerId))
                    put("quantity", JsonPrimitive(item.selectedQuantity))
                }
                cartItems.add(offer)
            }

            deliveryFields.forEach { field ->
                when (field.widgetType) {
                    "input" -> {
                        if (field.data != null)
                            put(field.key.toString(), field.data!!)
                    }
                }
            }

            put("internal_cart", JsonArray(cartItems))

            put(
                "delivery_method",
                JsonPrimitive(selectDeliveryMethod.value)
            )

            put(
                "deal_type",
                JsonPrimitive(selectDealType.value)
            )

            put(
                "payment_method",
                JsonPrimitive(selectPaymentType.value)
            )
        }

        val eventParameters = mapOf(
            "user_id" to UserData.login,
            "profile_source" to "settings",
            "body" to jsonBody
        )
        analyticsHelper.reportEvent("click_submit_order", eventParameters)

        viewModelScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                   apiService.postCrateOrder(UserData.login, jsonBody)
                }

                withContext(Dispatchers.Main) {
                    try {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                        if (payload.status == "operation_success"){
                            showToast(
                                ToastItem(
                                    isVisible = true,
                                    message = payload.operationResult?.message ?: getString(
                                        strings.operationSuccess),
                                    type = ToastType.SUCCESS
                                )
                            )

                            val id = payload.operationResult?.message?.toLong()

                            val ep = mapOf(
                                "buyer_id" to UserData.login,
                                "order_id" to id
                            )
                            analyticsHelper.reportEvent("create_order_success", ep)
                            _responsePostPage.value = payload
                        }else{
                            showToast(
                                ToastItem(
                                    isVisible = true,
                                    message = payload.operationResult?.message ?: getString(
                                        strings.operationFailed),
                                    type = ToastType.ERROR
                                )
                            )
                            val ep = mapOf(
                                "buyer_id" to UserData.login,
                                "body" to jsonBody,
                            )
                            analyticsHelper.reportEvent("create_order_failed", ep)
                            _responsePostPage.value = payload
                        }
                    }catch (e: Exception){
                        throw ServerErrorException(errorCode = response.errorCode.toString(), humanMessage = response.errorCode.toString())
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            } finally {
                setLoading(false)
            }
        }
    }
}
