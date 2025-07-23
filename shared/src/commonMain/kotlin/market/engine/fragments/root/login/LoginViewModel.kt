package market.engine.fragments.root.login

import androidx.compose.ui.text.input.TextFieldValue
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.network.networkObjects.UserPayload
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

data class LoginContentState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val email : TextFieldValue = TextFieldValue(),
    val password : TextFieldValue = TextFieldValue(),
    val captcha : TextFieldValue = TextFieldValue(),
    val captchaImage : String? = null,
    val captchaKey : String? = null,

    val auth2ContentState: Auth2ContentState = Auth2ContentState(),
)

data class CaptchaState(
    val captchaImage : String? = null,
    val captchaKey : String? = null,
    val captcha : TextFieldValue = TextFieldValue(),
)

data class Auth2ContentState(
    val user: Long = 1L,
    val obfuscatedIdentity: String?= null,
    val lastRequestByIdentity: Int?= null,
    val humanMessage: String? = null,
    private val viewModel: LoginViewModel? = null
)
{
    private val _leftTimerState = MutableStateFlow(0)
    val leftTimerState = _leftTimerState.asStateFlow()

    private val _codeState = MutableStateFlow(TextFieldValue())
    val codeState = _codeState.asStateFlow()

    init {
        if (lastRequestByIdentity != null) {
            startTimer(lastRequestByIdentity)
        }
    }

    fun startTimer(leftTimer : Int){
        _leftTimerState.value = leftTimer
        viewModel?.viewModelScope?.launch {
            while (_leftTimerState.value > 0){
                delay(1000)
                _leftTimerState.value--

                if(_leftTimerState.value == 0){
                    viewModel.changeO2AuthState(humanMessage, null)
                }
            }
        }
    }

    fun onCodeChange(value : TextFieldValue){
        _codeState.value = value
        if(value.text.length == 4){
            onCodeSubmit()
        }
    }

    fun onCodeSubmit(){
        viewModel?.run {
            setLoading(true)
            viewModelScope.launch {
                try {
                    val body = HashMap<String, String>()
                    body["user_id"] = user.toString()
                    if (codeState.value.text.isNotBlank()) {
                        body["code"] = codeState.value.text
                    }
                    val res = withContext(Dispatchers.IO) {
                        apiService.postAuthByCode(body)
                    }
                    val serializer = UserPayload.serializer()
                    val payload: UserPayload =
                        deserializePayload(res.payload, serializer)

                    withContext(Dispatchers.Main) {
                        if (payload.result == "SUCCESS") {
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
                            analyticsHelper.reportEvent("login_success", events)
                            delay(1000)
                            component.onBack()
                        } else {
                            if (lastRequestByIdentity != null) {
                                showToast(
                                    errorToastItem.copy(
                                        message = res.humanMessage ?: getString(strings.errorLogin)
                                    )
                                )
                            }
                            changeO2AuthState(res.humanMessage, payload.lastRequestByIdentity)
                        }
                    }
                }catch (e : ServerErrorException){
                    onError(e)
                }
                catch (e : Exception){
                    onError(ServerErrorException(errorCode = e.message.toString(), humanMessage = e.message.toString()))
                }finally {
                    setLoading(false)
                }
            }
        }
    }
}

class LoginViewModel(val component: LoginComponent) : CoreViewModel() {

    private val emailTextValue = MutableStateFlow(TextFieldValue())
    private val passwordTextValue = MutableStateFlow(TextFieldValue())
    private val captchaState = MutableStateFlow(CaptchaState())

    private val auth2ContentState = MutableStateFlow(Auth2ContentState())

    private val _openContent = MutableStateFlow(false)
    val openContent = _openContent.asStateFlow()

    val loginContentState: StateFlow<LoginContentState> = combine(
        emailTextValue,
        passwordTextValue,
        captchaState,
        auth2ContentState
    ){ email, password, captcha, auth2ContentState ->
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
            auth2ContentState = auth2ContentState
        )
    }.stateIn(
        viewModelScope,
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
        viewModelScope.launch {
            setLoading(true)
            refresh()
            delay(2000)
            setLoading(false)
        }
    }

    fun setCaptchaTextValue(value : TextFieldValue) {
        captchaState.update {
            it.copy(
                captcha = value
            )
        }
    }

    fun setEmailTextValue(value : TextFieldValue) {
        auth2ContentState.update {
            it.copy(
                user = 1
            )
        }
        emailTextValue.value = value
    }

    fun setPasswordTextValue(value : TextFieldValue) {
        passwordTextValue.value = value
    }

    fun changeO2AuthState(humanMessage: String?, lastRequestByIdentity: Int?){
        auth2ContentState.update {
            it.copy(
                humanMessage = humanMessage,
                lastRequestByIdentity = lastRequestByIdentity
            )
        }
    }

    fun postAuth() {
        if (auth2ContentState.value.user == 1L) {
            val email = emailTextValue.value.text
            val password = passwordTextValue.value.text
            val captcha = captchaState.value.captcha.text
            val captKey = captchaState.value.captchaKey

            val body = HashMap<String, String>()
            body["identity"] = email
            body["password"] = password
            body["workstation_data"] = SAPI.workstationData
           // body["test"] = "true"

            if (captcha.isNotBlank() && captKey?.isNotBlank() == true) {
                body["captcha_key"] = captKey
                body["captcha_response"] = captcha
            }

            setLoading(true)
            viewModelScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apiService.postAuth(body = body)
                    }

                    withContext(Dispatchers.Main) {
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
                                            message = getString(strings.operationSuccess)
                                        )
                                    )

                                    val events = mapOf(
                                        "login_type" to "email",
                                        "login_result" to "success",
                                        "login_email" to body["identity"]
                                    )
                                    analyticsHelper.reportEvent("login_success", events)
                                    delay(1000)
                                    component.onBack()
                                }

                                "needs_code" -> {
                                    val events = mapOf(
                                        "login_type" to "email",
                                        "login_result" to "fail",
                                        "login_email" to body["identity"]
                                    )
                                    analyticsHelper.reportEvent("login_fail_need_code", events)

                                    auth2ContentState.update {
                                        it.copy(
                                            user = payload.user,
                                            obfuscatedIdentity = payload.obfuscatedIdentity,
                                            lastRequestByIdentity = payload.lastRequestByIdentity,
                                            humanMessage = response.humanMessage,
                                            viewModel = this@LoginViewModel
                                        )
                                    }

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
                                                    ?: getString(strings.errorLogin)
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
                            delay(1000)
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
                                        message = response.humanMessage ?: getString(strings.errorLogin)
                                    )
                                )
                            }
                        }
                    }catch (_ : Exception){
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

    fun closeAuth2Content(){
        _openContent.value = false
    }

//    var postChangeGoogleAuth = MutableLiveData<GoogleAuthResponse?>()
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
