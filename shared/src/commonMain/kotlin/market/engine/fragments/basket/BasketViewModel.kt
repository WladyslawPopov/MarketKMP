package market.engine.fragments.basket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.UserData
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.BodyListPayload
import market.engine.core.network.networkObjects.UserBody
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class BasketViewModel(
    private val apiService: APIService,
    private val userRepository : UserRepository,
    private val userOperations : UserOperations,
) : BaseViewModel() {

    private var _responseGetUserCart = MutableStateFlow<BodyListPayload<UserBody>?>(null)
    val responseGetUserCart : StateFlow<BodyListPayload<UserBody>?> = _responseGetUserCart.asStateFlow()

    fun getUserCart(){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    setLoading(true)
                    userRepository.updateToken()
                    val response = apiService.postUserOperationsGetCartItems(UserData.login)
                    withContext(Dispatchers.Main){
                        try {
                            val serializer = BodyListPayload.serializer(UserBody.serializer())
                            val payload : BodyListPayload<UserBody> = deserializePayload(response.payload, serializer)
                            _responseGetUserCart.value = payload
                            userRepository.updateUserInfo()
                        }catch (e : Exception){
                            throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                        }
                    }
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
            finally {
                setLoading(false)
            }
        }
    }

    suspend fun clearBasket() : Boolean? {
        try {
            return withContext(Dispatchers.IO) {
                setLoading(true)
                userRepository.updateToken()
                if(UserData.token != "") {
                    val resObj = userOperations.postUsersOperationDeleteCart(
                        UserData.login)
                    val res = resObj.success
                    val resErr = resObj.error

                    if (res == true) {
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
        }
        finally {
            setLoading(false)
        }
    }
}
