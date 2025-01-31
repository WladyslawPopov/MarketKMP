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
import market.engine.common.AnalyticsFactory
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString


class CreateOrderViewModel(
    private val apiService: APIService,
    private val offerOperations: OfferOperations,
    private val userOperations: UserOperations
) : BaseViewModel() {

    private var _responsePostPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseCreateOrder : StateFlow<DynamicPayload<OperationResult>?> = _responsePostPage.asStateFlow()

    private var _responseGetOffers = MutableStateFlow<List<Offer>>(emptyList())
    val responseGetOffers : StateFlow<List<Offer>> = _responseGetOffers.asStateFlow()

    val responseGetLoadCards = mutableStateOf(emptyList<DeliveryAddress>())

    private var _responseGetAdditionalData = MutableStateFlow<AdditionalDataForNewOrder?>(null)
    val responseGetAdditionalData  : StateFlow<AdditionalDataForNewOrder?> = _responseGetAdditionalData.asStateFlow()

    val deliveryFields = mutableStateOf<List<Fields>>(emptyList())

    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    val selectDeliveryMethod = mutableStateOf(0)
    val selectDealType = mutableStateOf(0)
    val selectPaymentType = mutableStateOf(0)

    init {
        loadFields()
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

    fun loadDeliveryCards(){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = userOperations.getUsersOperationsAddressCards(UserData.login)

                    val payload = response.success
                    val err = response.error

                    if (payload?.body?.addressCards != null) {
                        responseGetLoadCards.value = payload.body.addressCards
                    }else{
                        throw err ?: ServerErrorException(errorCode = "Error", humanMessage = "")
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
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
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

                    val buf = userOperations.postUserOperationsGetAdditionalDataBeforeCreateOrder(
                        UserData.login,
                        additionalBody
                    )
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

    private fun loadFields() {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsSetAddressCards(UserData.login)
            }
            withContext(Dispatchers.Main){
                val payload = res.success
                val err = res.error

                if (payload != null) {
                    deliveryFields.value = payload.fields
                } else {
                    if (err != null)
                        onError(err)
                }
            }
        }
    }

    fun updateDefaultCard(card: DeliveryAddress) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                val b = HashMap<String, Long>()
                b["id_as_ts"] = card.id
                userOperations.postUsersOperationsSetAddressCardsDefault(UserData.login, b)
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    if (buffer.success) {
                        responseGetLoadCards.value = buildList {
                            responseGetLoadCards.value.forEach {
                                if (it.id != card.id && !it.isDefault){
                                    add(it)
                                }else{
                                    if (it.isDefault){
                                        add(it.copy(isDefault = false))
                                    }else{
                                        add(it.copy(isDefault = true))
                                    }
                                }
                            }
                        }

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = buffer.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                    }
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    fun updateDeleteCard(card: DeliveryAddress) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                val b = HashMap<String, Long>()
                b["id_as_ts"] = card.id
                userOperations.postUsersOperationsDeleteAddressCards(UserData.login, b)
            }
            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    if (buffer.success) {
                        responseGetLoadCards.value = buildList {
                            addAll(responseGetLoadCards.value)
                            remove(card)
                        }

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = buffer.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                    }
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    suspend fun saveDeliveryCard(cardId: Long?) : Boolean {
        val jsonBody = buildJsonObject {
            deliveryFields.value.forEach { field ->
                when (field.widgetType) {
                    "input" -> {
                        if(field.data != null) {
                            put(field.key.toString(), field.data!!)
                        }
                    }
                    "hidden" -> {
                        if (cardId != null) {
                            put(field.key.toString(), JsonPrimitive(cardId))
                        }
                    }
                    else -> {}
                }
            }
        }

        val res =  withContext(Dispatchers.IO) {
            userOperations.postUsersOperationsSetAddressCards(UserData.login, jsonBody)
        }

        return withContext(Dispatchers.Main) {
            val payload = res.success
            val err = res.error
            if (payload != null) {
                if (payload.status == "operation_success") {

                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                        "body" to jsonBody
                    )
                    analyticsHelper.reportEvent(
                        "save_address_cards_success",
                        eventParameters
                    )

                    loadFields()
                    loadDeliveryCards()

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    return@withContext true
                } else {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                        "body" to jsonBody
                    )
                    analyticsHelper.reportEvent(
                        "save_address_cards_failed",
                        eventParameters
                    )
                    payload.recipe?.fields?.let { deliveryFields.value = it }

                    showToast(errorToastItem.copy(
                        message = getString(strings.operationFailed)
                    ))
                    return@withContext false
                }
            } else {
                err?.let { onError(it) }
                return@withContext false
            }
        }
    }

    fun postPage(basketItem:  Pair<Long, List<SelectedBasketItem>>) {
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

            deliveryFields.value.forEach { field ->
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
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = apiService.postCrateOrder(UserData.login, jsonBody)
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
                                _responsePostPage.value = payload
                            }
                        }catch (e: Exception){
                            throw ServerErrorException(errorCode = response.errorCode.toString(), humanMessage = response.errorCode.toString())
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
}
