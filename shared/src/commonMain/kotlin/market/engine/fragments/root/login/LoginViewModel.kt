package market.engine.fragments.root.login

import androidx.lifecycle.SavedStateHandle

import com.arkivanov.decompose.ExperimentalDecomposeApi
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.states.Auth2ContentData
import market.engine.core.network.networkObjects.UserPayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

data class LoginContentState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val email : String = "",
    val password : String = "",
    val captcha : String = "",
    val captchaImage : String? = null,
    val captchaKey : String? = null,
)

@Serializable
data class CaptchaState(
    val captchaImage : String? = null,
    val captchaKey : String? = null,
    val captcha : String = "",
)

class LoginViewModel(val component: LoginComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    private val emailTextValue = savedStateHandle.getSavedStateFlow(
        scope,
        "emailTextValue",
        "",
        String.serializer()
    )
    private val passwordTextValue = savedStateHandle.getSavedStateFlow(
        scope,
        "passwordTextValue",
        "",
        String.serializer()
    )
    private val captchaState = savedStateHandle.getSavedStateFlow(
        scope,
        "captchaState",
        CaptchaState(),
        CaptchaState.serializer()
    )

    private val _openContent = savedStateHandle.getSavedStateFlow(
        scope,
        "openContent",
        false,
        Boolean.serializer()
    )

    @OptIn(ExperimentalDecomposeApi::class)
    val auth2ContentRepository = Auth2ContentRepository(
        Auth2ContentData(),
        savedStateHandle,
        this@LoginViewModel

    ) { component.onBack() }

    val openContent = _openContent.state

    val loginContentState: StateFlow<LoginContentState> = combine(
        emailTextValue.state,
        passwordTextValue.state,
        captchaState.state
    ){ email, password, captcha ->
        LoginContentState(
            appBarData = SimpleAppBarData(
                onBackClick = {
                    onBack()
                }
            ),
            email = email,
            password = password,
            captcha = captcha.captcha,
            captchaImage = captcha.captchaImage,
            captchaKey = captcha.captchaKey,
        )
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        LoginContentState()
    )

    fun onBack(){
        if(openContent.value){
            _openContent.value = false
        }else{
            component.onBack()
        }
    }

    fun refreshPage(){
        scope.launch(Dispatchers.IO) {
            setLoading(true)
            refresh()
            delay(2000)
            setLoading(false)
        }
    }

    fun postAuth() {
        if (auth2ContentRepository.auth2ContentState.value.user == 1L) {
            val email = emailTextValue.value
            val password = passwordTextValue.value
            val captcha = captchaState.value.captcha
            val captKey = captchaState.value.captchaKey

            val body = HashMap<String, String>()
            body["identity"] = email
            body["password"] = password
            body["workstation_data"] = SAPI.workstationData
            body["test"] = "true"

            if (captcha.isNotBlank() && captKey?.isNotBlank() == true) {
                body["captcha_key"] = captKey
                body["captcha_response"] = captcha
            }

            setLoading(true)
            scope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apiService.postAuth(body = body)
                    }

                    setLoading(false)

                    try {
                        val serializer = UserPayload.serializer()
                        val payload: UserPayload =
                            deserializePayload(response.payload, serializer)
                        when (payload.result) {
                            "SUCCESS" -> {
                                userRepository.setToken(payload.user, payload.token ?: "")
                                updateUserInfo()
                                showToast(
                                    successToastItem.copy(
                                        message = withContext(Dispatchers.IO){
                                            getString(strings.operationSuccess)
                                        }
                                    )
                                )

                                val events = mapOf(
                                    "login_type" to "email",
                                    "login_result" to "success",
                                    "login_email" to body["identity"]
                                )
                                analyticsHelper.reportEvent("login_success", events)
                                withContext(Dispatchers.IO){
                                    delay(1000)
                                }
                                component.onBack()
                            }

                            "needs_code" -> {
                                val events = mapOf(
                                    "login_type" to "email",
                                    "login_result" to "fail",
                                    "login_email" to body["identity"]
                                )
                                analyticsHelper.reportEvent("login_fail_need_code", events)

                                auth2ContentRepository.updateAuthData(
                                    auth2ContentRepository.auth2ContentState.value.copy(
                                        user = payload.user,
                                        obfuscatedIdentity = payload.obfuscatedIdentity,
                                        lastRequestByIdentity = payload.lastRequestByIdentity,
                                        humanMessage = response.humanMessage,
                                    )
                                )

                                _openContent.value = true
                            }

                            else -> {
                                val events = mapOf(
                                    "login_type" to "email",
                                    "login_result" to "fail",
                                    "login_email" to body["identity"]
                                )

                                if (payload.captchaImage != null && payload.captchaKey != null) {
                                    if(captchaState.value.captchaKey == payload.captchaKey){
                                        postAuth()
                                    }else {
                                        captchaState.update {
                                            it.copy(
                                                captchaImage = payload.captchaImage,
                                                captchaKey = payload.captchaKey
                                            )
                                        }
                                    }
                                }

                                analyticsHelper.reportEvent("login_fail", events)
                                if (response.humanMessage != "") {
                                    showToast(
                                        errorToastItem.copy(
                                            message = response.humanMessage
                                                ?: withContext(Dispatchers.IO){
                                                    getString(strings.errorLogin)
                                                }
                                        )
                                    )
                                }

                            }
                        }
                    } catch (_: Exception) {
                        throw ServerErrorException(
                            response.errorCode.toString(),
                            response.humanMessage.toString()
                        )
                    }
                } catch (exception: ServerErrorException) {
                    onError(exception)
                } catch (exception: Exception) {
                    onError(
                        ServerErrorException(
                            errorCode = exception.message.toString(),
                            humanMessage = exception.message.toString()
                        )
                    )
                }finally {
                    setLoading(false)
                }
            }
        }else {
            _openContent.value = true
        }
    }

    fun postAuthExternal(body: HashMap<String, String>) {
        setLoading(true)
        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.postAuthExternal(body = body)
                }
                try {
                    val serializer = UserPayload.serializer()
                    val payload = deserializePayload(response.payload, serializer)
                    if (payload.result == "SUCCESS") {
                        userRepository.setToken(payload.user, payload.token ?: "")

                        updateUserInfo()

                        setLoading(false)
                        showToast(
                            successToastItem.copy(
                                message = withContext(Dispatchers.IO){
                                    getString(strings.operationSuccess)
                                }
                            )
                        )

                        val events = mapOf(
                            "login_type" to "email",
                            "login_result" to "success",
                            "login_email" to body["identity"]
                        )
                        analyticsHelper.reportEvent("login_success",events)
                        withContext(Dispatchers.IO){
                            delay(500)
                        }
                        component.onBack()
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
                                    message = response.humanMessage ?: withContext(Dispatchers.IO){
                                        getString(strings.errorLogin)
                                    }
                                )
                            )
                        }
                    }
                }catch (_ : Exception){
                    throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
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

    fun closeAuth2Content(){
        _openContent.value = false
    }

    fun setCaptchaTextValue(value : String) {
        captchaState.update {
            it.copy(
                captcha = value
            )
        }
    }

    fun setEmailTextValue(value : String) {
        auth2ContentRepository.updateAuthData(
            auth2ContentRepository.auth2ContentState.value.copy(
                user = 1
            )
        )
        emailTextValue.value = value
    }

    fun setPasswordTextValue(value : String) {
        passwordTextValue.value = value
    }

//    var postChangeGoogleAuth = MutableLiveData<GoogleAuthResponse?>()
//    fun changeTokenGoogleAuth(body: HashMap<String, String>) {
//        jobPostChangeGoogleAuth = scope.launch {
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
