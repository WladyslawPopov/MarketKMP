package market.engine.fragments.root.main.createOrder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.filterContents.deliveryCardsContents.DeliveryCardsViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin


data class CreateOrderState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val responseGetOffers: List<OfferItem> = emptyList(),
    val responseGetAdditionalData: AdditionalDataForNewOrder? = null,
    val selectDeliveryMethod: Int = 0,
    val selectDealType: Int = 0,
    val selectPaymentType: Int = 0
)

class CreateOrderViewModel(
    val basketItem:  Pair<Long, List<SelectedBasketItem>>,
    val component: CreateOrderComponent
): CoreViewModel() {

    private var _responseGetOffers = MutableStateFlow<List<OfferItem>>(emptyList())
    private var _responseGetAdditionalData = MutableStateFlow<AdditionalDataForNewOrder?>(null)

    val userOperations : UserOperations by lazy { getKoin().get() }
    val offerOperations : OfferOperations by lazy { getKoin().get() }

    val deliveryCardsViewModel = DeliveryCardsViewModel()

    private val _selectDeliveryMethod = MutableStateFlow(0)
    private val _selectDealType = MutableStateFlow(0)
    private val _selectPaymentType = MutableStateFlow(0)

    val createOrderState : StateFlow<CreateOrderState> = combine(
        _responseGetOffers,
        _responseGetAdditionalData,
        _selectDeliveryMethod,
        _selectDealType,
        _selectPaymentType
    ){ responseGetOffers, responseGetAdditionalData, selectDeliveryMethod, selectDealType, selectPaymentType ->
        CreateOrderState(
            appBarData = SimpleAppBarData(
                onBackClick = {
                    component.onBackClicked()
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        onClick = {
                            refreshPage()
                        }
                    ),
                )
            ),
            responseGetOffers = responseGetOffers,
            responseGetAdditionalData = responseGetAdditionalData,
            selectDeliveryMethod = selectDeliveryMethod,
            selectDealType = selectDealType,
            selectPaymentType = selectPaymentType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = CreateOrderState()
    )

    init {
        getOffers(basketItem.second.map { it.offerId })
        getAdditionalFields(
            basketItem.first,
            basketItem.second.map { it.offerId },
            basketItem.second.map { it.selectedQuantity }
        ){
            component.onBackClicked()
        }

        analyticsHelper.reportEvent("view_create_order", mapOf())
    }

    fun refreshPage(){
        deliveryCardsViewModel.refreshCards()
        getOffers(basketItem.second.map { it.offerId })
        getAdditionalFields(
            basketItem.first,
            basketItem.second.map { it.offerId },
            basketItem.second.map { it.selectedQuantity }
        ){
            component.onBackClicked()
        }
        refresh()
    }

    fun changeDeliveryMethod(deliveryMethod: Int){
        _selectDeliveryMethod.value = deliveryMethod
    }

    fun changeDealType(dealType: Int){
        _selectDealType.value = dealType
    }

    fun changePaymentType(paymentType: Int){
        _selectPaymentType.value = paymentType
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
                            _responseGetOffers.value += response.success!!.parseToOfferItem()
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

    fun getAdditionalFields(sellerId: Long, lotIds: List<Long>?, lotCounts: List<Int>?, goBack: () -> Unit) {
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
                            _selectDealType.value = data.dealTypes.firstOrNull()?.code ?: 0
                            _selectPaymentType.value = data.paymentMethods.firstOrNull()?.code ?: 0
                            _selectDeliveryMethod.value = data.deliveryMethods.firstOrNull()?.code ?: 0
                        }
                    }
                }else{
                    throw error ?: ServerErrorException(errorCode = "Error", humanMessage = "")
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
                goBack()
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
                goBack()
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
                JsonPrimitive(_selectDeliveryMethod.value)
            )

            put(
                "deal_type",
                JsonPrimitive(_selectDealType.value)
            )

            put(
                "payment_method",
                JsonPrimitive(_selectPaymentType.value)
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
                   apiService.postOperation(UserData.login,"create_new_order", "users", jsonBody)
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

                            component.goToMyOrders()
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
                        }
                    }catch (_: Exception){
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

    fun acceptButton(basketItem: Pair<Long, List<SelectedBasketItem>>){
        val fields = deliveryCardsViewModel.deliveryCardsState.value.deliveryFields

        if(fields.isEmpty()){
            deliveryCardsViewModel.saveDeliveryCard(
                deliveryCardsViewModel.deliveryCardsState.value.deliveryCards.firstOrNull()?.id ?: 1L
            )
        }else{
            postPage(fields, basketItem)
        }
    }
}
