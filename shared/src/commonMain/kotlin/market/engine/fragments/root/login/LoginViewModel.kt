package market.engine.fragments.root.login

import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.UserPayload
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class LoginViewModel : BaseViewModel() {

//    var postChangeGoogleAuth = MutableLiveData<GoogleAuthResponse?>()
    fun postAuth(
        email : String,
        password : String,
        captchaResponse : String?,
        captcha : String?,
        onSuccess : () -> Unit,
        onError : (String?, String?) -> Unit
    ) {
        val body = HashMap<String, String>()
        body["identity"] = email
        body["password"] = password
        body["workstation_data"] = SAPI.workstationData
        if (captcha?.isNotBlank() == true && captchaResponse?.isNotBlank() == true ) {
            body["captcha_key"] = captcha
            body["captcha_response"] = captchaResponse
        }

        setLoading(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.postAuth(body = body)
                }
                withContext(Dispatchers.Main) {
                    try {
                        val serializer = UserPayload.serializer()
                        val payload : UserPayload = deserializePayload(response.payload, serializer)
                        if (payload.result == "SUCCESS") {
                            setLoading(false)
                            userRepository.setToken(payload.user, payload.token ?: "")
                            updateUserInfo()
                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )

                            val events = mapOf(
                                "login_type" to "email",
                                "login_result" to "success",
                                "login_email" to body["identity"]
                            )
                            analyticsHelper.reportEvent("login_success",events)

                            delay(2000)

                            onSuccess()
                        } else {
                            val events = mapOf(
                                "login_type" to "email",
                                "login_result" to "fail",
                                "login_email" to body["identity"]
                            )

                            onError(payload.captchaImage, payload.captchaKey)

                            analyticsHelper.reportEvent("login_fail",events)
                            if(response.humanMessage != "") {
                                showToast(
                                    errorToastItem.copy(
                                        message = response.humanMessage ?: getString(strings.errorLogin)
                                    )
                                )
                            }
                        }
                    }catch (e : Exception){
                        throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
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

    fun postAuthExternal(
        body: HashMap<String, String>,
        onSuccess: () -> Unit,
    ) {
        setLoading(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.postAuthExternal(body = body)
                }
                withContext(Dispatchers.Main) {
                    try {
                        val serializer = UserPayload.serializer()
                        val payload = deserializePayload(response.payload, serializer)
                        if (payload.result == "SUCCESS") {
                            userRepository.setToken(payload.user, payload.token ?: "")

                            updateUserInfo()

                            setLoading(false)
                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )

                            val events = mapOf(
                                "login_type" to "email",
                                "login_result" to "success",
                                "login_email" to body["identity"]
                            )
                            analyticsHelper.reportEvent("login_success",events)

                            delay(2000)

                            onSuccess()
                        } else {
                            val events = mapOf(
                                "login_type" to "email",
                                "login_result" to "fail",
                                "login_email" to body["identity"]
                            )

                            analyticsHelper.reportEvent("login_fail",events)

                            if(response.humanMessage != "") {
                                showToast(
                                    errorToastItem.copy(
                                        message = response.humanMessage ?: getString(strings.errorLogin)
                                    )
                                )
                            }
                        }
                    }catch (e : Exception){
                        throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
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
