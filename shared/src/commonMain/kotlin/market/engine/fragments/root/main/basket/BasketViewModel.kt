package market.engine.fragments.root.main.basket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.User
import market.engine.core.network.networkObjects.UserBody
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel

class BasketViewModel: BaseViewModel() {

    private var _responseGetUserCart = MutableStateFlow<
            List<Pair<User?, List<Offer?>>>>(emptyList())
    val responseGetUserCart : StateFlow<
            List<Pair<User?, List<Offer?>>>> = _responseGetUserCart.asStateFlow()


    val firstVisibleItem = MutableStateFlow(0)

    fun getUserCart(){
        viewModelScope.launch {
            try {
                setLoading(true)
                updateUserInfo()
                val response = withContext(Dispatchers.IO) {
                    apiService.postUserOperationsGetCartItems(UserData.login)
                }

                try {
                    val serializer = BodyListPayload.serializer(UserBody.serializer())
                    val payload : BodyListPayload<UserBody> = deserializePayload(response.payload, serializer)

                    val groupedBySeller = payload.bodyList.groupBy { it.sellerId }
                        val result = groupedBySeller.map { (sellerId, items) ->
                            val sellerUser: User? = getUser(sellerId)
                            val basketItems: List<Offer?> = items.map { item ->
                                Offer(
                                    id = item.offerId,
                                    title = item.offerTitle,
                                    currentPricePerItem = item.offerPrice,
                                    currentQuantity = item.availableQuantity,
                                    quantity = item.quantity,
                                    sellerData = User(id = item.sellerId),
                                    freeLocation = item.freeLocation,
                                    externalUrl = item.offerImage,
                                    safeDeal = item.isBuyable ?: false
                                )
                            }
                            sellerUser to basketItems
                        }
                    _responseGetUserCart.value = result
                }catch (e : Exception){
                    throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
            } finally {
                setLoading(false)
            }
        }
    }

    suspend fun clearBasket() : Boolean? {
        try {
            return withContext(Dispatchers.IO) {
                setLoading(true)
                if(UserData.token != "") {
                    val resObj = userOperations.postUsersOperationDeleteCart(
                        UserData.login)
                    val res = resObj.success
                    val resErr = resObj.error

                    if (res == true) {
                        updateUserInfo()
                        return@withContext true
                    }else {
                        if (resErr != null) {
                            onError(resErr)
                            return@withContext null
                        }else{
                            return@withContext null
                        }
                    }
                }else{
                    return@withContext null
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            return null
        } finally {
            setLoading(false)
        }
    }

    private suspend fun getUser(id : Long) : User? {
        try {
            val res = withContext(Dispatchers.IO){
                userOperations.getUsers(id)
            }

            return withContext(Dispatchers.Main){
                val user = res.success?.firstOrNull()
                val error = res.error
                if (user != null){
                    return@withContext user
                }else{
                    error?.let { throw it }
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            return null
        }
    }

    suspend fun addOfferToBasket(body : HashMap<String, String>, offerId : Long) : Boolean? {
        try {
            val res = withContext(Dispatchers.Default) {
                userOperations.postUsersOperationsAddItemToCart(UserData.login, body)
            }
            return withContext(Dispatchers.Main) {
                val buffer = res.success
                val error = res.error
                if (buffer != null) {
                    responseGetUserCart.value.find { pair ->
                        pair.second.find { it?.id == offerId } != null
                    }?.second?.find { it?.id == offerId }
                        ?.quantity = body["quantity"]?.toInt() ?: 0
                    updateUserInfo()
                    return@withContext true
                } else {
                    if (error != null) {
                        onError(error)
                    }
                    return@withContext null
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            return null
        }
    }

    suspend fun deleteItem(bodyUIR : JsonObject, lotData: Offer?, idSeller: Long) : Boolean? {
        try {
            val res = withContext(Dispatchers.IO) {
                userOperations.postUsersOperationsRemoveManyItemsFromCart(
                    UserData.login,
                    bodyUIR
                )
            }

            val buffer = res.success
            val error = res.error
            return withContext(Dispatchers.Main) {
                if (buffer != null) {
                    updateUserInfo()

                    val eventParameters = mapOf(
                        "lot_id" to lotData?.id,
                        "lot_name" to lotData?.title,
                        "lot_city" to lotData?.freeLocation,
                        "auc_delivery" to "false",
                        "lot_category" to "-",
                        "seller_id" to idSeller,
                        "lot_price_start" to lotData?.currentPricePerItem,
                        "cart_id" to idSeller
                    )
                    analyticsHelper.reportEvent(
                        "click_del_item",
                        eventParameters
                    )
                    return@withContext true
                } else {
                    if (error != null) {
                        onError(error)
                    }
                    return@withContext null
                }
            }
        }catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            return null
        }
    }
}
