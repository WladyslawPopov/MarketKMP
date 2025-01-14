package market.engine.fragments.createOrder

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
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.DynamicPayload
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

    private var _responseGetLoadCards = MutableStateFlow<List<DeliveryAddress>>(emptyList())
    val responseGetLoadCards : StateFlow<List<DeliveryAddress>> = _responseGetLoadCards.asStateFlow()

    private var _responseGetAdditionalData = MutableStateFlow<AdditionalDataForNewOrder?>(null)
    val responseGetAdditionalData  : StateFlow<AdditionalDataForNewOrder?> = _responseGetAdditionalData.asStateFlow()

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
                        _responseGetLoadCards.value = payload.body.addressCards
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


    fun postPage(idUser: Long, body: JsonObject) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = apiService.postCrateOrder(idUser, body)
                    withContext(Dispatchers.Main) {
                        setLoading(false)
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
            }
        }
    }
}
