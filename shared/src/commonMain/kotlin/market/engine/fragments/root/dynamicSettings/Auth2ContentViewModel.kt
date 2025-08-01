package market.engine.fragments.root.dynamicSettings

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.Auth2ContentData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.UserPayload
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString


class Auth2ContentViewModel(
    initData: Auth2ContentData,
    val onBack: () -> Unit,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle)
{
    private val _leftTimerState = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "leftTimerState",
        0,
        Int.serializer()
    )
    val leftTimerState = _leftTimerState.state

    private val _codeState = MutableStateFlow(TextFieldValue())
    val codeState = _codeState.asStateFlow()

    private val _auth2ContentState = MutableStateFlow(initData)
    val auth2ContentState = _auth2ContentState.asStateFlow()

    init {
        if (initData.lastRequestByIdentity != null) {
            startTimer(initData.lastRequestByIdentity)
        }
    }

    fun startTimer(leftTimer : Int){
        _leftTimerState.value = leftTimer
        viewModelScope.launch {
            while (_leftTimerState.value > 0){
                delay(1000)
                _leftTimerState.value--

                if(_leftTimerState.value == 0){
                    changeO2AuthState(auth2ContentState.value.humanMessage, null)
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
    
    fun changeO2AuthState(humanMessage: String?, lastRequestByIdentity: Int?){
        _auth2ContentState.update {
            it.copy(
                humanMessage = humanMessage,
                lastRequestByIdentity = lastRequestByIdentity
            )
        }
    }

    fun onCodeSubmit() {
        setLoading(true)
        viewModelScope.launch {
            try {
                val body = HashMap<String, String>()
                body["user_id"] = auth2ContentState.value.user.toString()
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
                        onBack()
                    } else {
                        if (auth2ContentState.value.lastRequestByIdentity != null) {
                            showToast(
                                errorToastItem.copy(
                                    message = res.humanMessage ?: getString(strings.errorLogin)
                                )
                            )
                        }
                        changeO2AuthState(res.humanMessage, payload.lastRequestByIdentity)
                    }
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(
                    ServerErrorException(
                        errorCode = e.message.toString(),
                        humanMessage = e.message.toString()
                    )
                )
            } finally {
                setLoading(false)
            }
        }
    }

    fun updateAuthData(data: Auth2ContentData){
        _auth2ContentState.update {
            it.copy(
                user = data.user,
                obfuscatedIdentity = data.obfuscatedIdentity,
                lastRequestByIdentity = data.lastRequestByIdentity,
                humanMessage = data.humanMessage,
            )
        }
    }
}
