package market.engine.fragments.root.login

import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.network.networkObjects.UserPayload
import market.engine.fragments.base.BaseViewModel

class LoginViewModel : BaseViewModel() {

//    var postChangeGoogleAuth = MutableLiveData<GoogleAuthResponse?>()

    private val _responseAuth = MutableStateFlow<UserPayload?>(null)
    val responseAuth: StateFlow<UserPayload?> = _responseAuth.asStateFlow()

    fun postAuth(body: HashMap<String, String>) {
        setLoading(true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = apiService.postAuth(body = body)

                    withContext(Dispatchers.Main) {
                        try {
                            val serializer = UserPayload.serializer()
                            val payload : UserPayload = deserializePayload(response.payload, serializer)

                            if (payload.result == "success") {
                                _responseAuth.value = payload
                            }else{
                                _responseAuth.value = payload
                                throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                            }
                        }catch (e : Exception){
                            throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                        }

                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
            finally {
                setLoading(false)
            }
        }
    }

//    fun postAuthExternal(body: HashMap<String, String>) {
//        jobPostAuthExternal = viewModelScope.launch {
//            try {
//                withContext(Dispatchers.IO) {
//
//                    isShowProgress.postValue(true)
//                    val response = apiService.postAuthExternal(body = body)
//
//                    withContext(Dispatchers.Main) {
//                        try {
//                            isShowProgress.postValue(false)
//                            val payload = deserializePayload<UserPayload>(response.payload)
//                            postResponseAuth.postValue(payload)
//                        }catch (e : Exception){
//                            throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
//                        }
//                    }
//                }
//            } catch (exception: ServerErrorException) {
//                onError(exception)
//            } catch (exception: Exception) {
//                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
//            }
//        }
//    }
//
//    fun changeTokenGoogleAuth(body: HashMap<String, String>) {
//        jobPostChangeGoogleAuth = viewModelScope.launch {
//            try {
//                withContext(Dispatchers.IO) {
//                    isShowProgress.postValue(true)
//                    val response = apiService.postChangeTokenGoogleAuth(body = body)
//                    withContext(Dispatchers.Main) {
//                        postChangeGoogleAuth.postValue(response)
//                        isShowProgress.postValue(false)
//                    }
//                }
//            } catch (exception: ServerErrorException) {
//                onError(exception)
//            } catch (exception: Exception) {
//                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
//            }
//        }
//    }
}
