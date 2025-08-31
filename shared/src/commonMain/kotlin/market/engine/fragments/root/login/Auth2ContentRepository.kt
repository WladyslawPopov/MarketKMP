package market.engine.fragments.root.login

import androidx.lifecycle.SavedStateHandle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources
import market.engine.core.data.states.Auth2ContentData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.UserPayload
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

class Auth2ContentRepository(
    initData: Auth2ContentData,
    savedStateHandle: SavedStateHandle,
    val core: CoreViewModel,
    val onBack: () -> Unit,
)
{
    private val _leftTimerState = savedStateHandle.getSavedStateFlow(
        core.scope,
        "leftTimerState",
        0,
        Int.serializer()
    )
    val leftTimerState = _leftTimerState.state

    private val _codeState = savedStateHandle.getSavedStateFlow(
        core.scope,
        "codeState",
        "",
        String.serializer()
    )
    val codeState = _codeState.state

    private val _auth2ContentState = savedStateHandle.getSavedStateFlow(
        core.scope,
        "auth2ContentState",
        initData,
        Auth2ContentData.serializer()
    )
    val auth2ContentState = _auth2ContentState.state

    init {
        core.scope.launch {
            _auth2ContentState.state.collectLatest {
                if (it.lastRequestByIdentity != null) {
                    startTimer(it.lastRequestByIdentity)
                }
            }
        }
    }

    fun startTimer(leftTimer : Int){
        _leftTimerState.value = leftTimer
        core.scope.launch {
            while (_leftTimerState.value > 0){
                delay(1000)
                _leftTimerState.value--

                if(_leftTimerState.value == 0){
                    changeO2AuthState(auth2ContentState.value.humanMessage, null)
                }
            }
        }
    }

    fun onCodeChange(value : String){
        _codeState.value = value
        if(value.length == 4){
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
        core.setLoading(true)
        core.scope.launch {
            try {
                val body = HashMap<String, String>()
                body["user_id"] = auth2ContentState.value.user.toString()
                if (codeState.value.isNotBlank()) {
                    body["code"] = codeState.value
                }
                val res = withContext(Dispatchers.IO) {
                    core.apiService.postAuthByCode(body)
                }
                val serializer = UserPayload.serializer()
                val payload: UserPayload =
                    deserializePayload(res.payload, serializer)

                withContext(Dispatchers.Main) {
                    if (payload.result == "SUCCESS") {
                        core.userRepository.setToken(payload.user, payload.token ?: "")
                        core.updateUserInfo()
                        core.showToast(
                            successToastItem.copy(
                                message = getString(ThemeResources.strings.operationSuccess)
                            )
                        )

                        val events = mapOf(
                            "login_type" to "email",
                            "login_result" to "success",
                            "login_email" to body["identity"]
                        )
                        core.analyticsHelper.reportEvent("login_success", events)
                        delay(1000)
                        onBack()
                    } else {
                        if (auth2ContentState.value.lastRequestByIdentity != null) {
                            core.showToast(
                                errorToastItem.copy(
                                    message = res.humanMessage
                                        ?: getString(ThemeResources.strings.errorLogin)
                                )
                            )
                        }
                        changeO2AuthState(res.humanMessage, payload.lastRequestByIdentity)
                    }
                }
            } catch (e: ServerErrorException) {
                core.onError(e)
            } catch (e: Exception) {
                core.onError(
                    ServerErrorException(
                        errorCode = e.message.toString(),
                        humanMessage = e.message.toString()
                    )
                )
            } finally {
                core.setLoading(false)
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
