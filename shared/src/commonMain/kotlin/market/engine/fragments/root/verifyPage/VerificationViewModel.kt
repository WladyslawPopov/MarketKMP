package market.engine.fragments.root.verifyPage

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class VerificationViewModel : BaseViewModel() {
    val action = mutableStateOf("")

    fun init(settingsType: String, owner: Long?, code: String?) {
        when (settingsType) {
            "set_password" -> {
                getSetPassword(owner)
            }

            else -> {
                if (code != null && owner != null) {
                    getSetEmail(owner, code)
                }
            }
        }
    }

    private fun getSetEmail(owner: Long, code: String) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val body = HashMap<String, String>()
                body["code"] = code
                val buffer = withContext(Dispatchers.IO) { userOperations.postUsersOperationsConfirmEmail(owner, body) }
                val res = buffer.success
                val resErr = buffer.error

                if (res != null) {
                    if (res.status == "ok") {
                        if (res.body != null) {
                            val bodySMS = HashMap<String, JsonElement>()
                            bodySMS["action"] = JsonPrimitive("change_email")

                            val buf = withContext(Dispatchers.IO) {
                                operationsMethods.postOperation(
                                    owner,
                                    "request_additional_confirmation",
                                    "users",
                                    bodySMS
                                )
                            }

                            val resSMS = buf.success
                            val resSmsErr = buf.error
                            withContext(Dispatchers.Main) {
                                if (resSMS != null) {
                                    showToast(
                                        successToastItem.copy(
                                            message = resSMS.operationResult?.message ?: getString(strings.operationSuccess)
                                        )
                                    )
                                    action.value = "change_email"
                                } else {
                                    if (resSmsErr != null) {
                                        onError(
                                            resSmsErr
                                        )
                                    }
                                    action.value = "close"
                                }
                            }
                        } else {
                            onError(ServerErrorException(res.errorCode ?:"", res.humanMessage ?: ""))
                            action.value = "close"
                        }
                    } else {
                        if (res.status == "operation_success") {
                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )
                            action.value = "login"
                        } else {
                            showToast(
                                errorToastItem.copy(
                                    message = res.humanMessage ?: ""
                                )
                            )
                            action.value = "close"
                        }
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                    action.value = "close"
                }
            } catch (e : ServerErrorException){
                onError(e)
            } catch (e : Exception){
                onError(ServerErrorException(e.message ?: ""))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun getSetPassword(owner: Long?) {
        viewModelScope.launch {
            val body = HashMap<String, JsonElement>()
            body["action"] = JsonPrimitive("change_password")

            val buffer = withContext(Dispatchers.IO) {
                operationsMethods.postOperation(
                    owner ?: UserData.login,
                    "request_additional_confirmation",
                    "users",
                    body
                )
            }
            val res = buffer.success
            val resErr = buffer.error
            withContext(Dispatchers.Main) {
                if (res != null) {
                    showToast(
                        successToastItem.copy(
                            message = res.operationResult?.message ?: getString(strings.operationSuccess)
                        )
                    )
                }else{
                    if (resErr != null) {
                        onError(
                            resErr
                        )
                    }
                }
            }
        }
    }

    fun postSetEmail(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            setLoading(true)

            val bodySMS = HashMap<String, JsonElement>()
            bodySMS["action"] = JsonPrimitive("change_email")
            bodySMS["code"] = JsonPrimitive(code)

            val buf = withContext(Dispatchers.IO) {
                operationsMethods.postOperation(
                    UserData.login,
                    "enter_additional_confirmation_code",
                    "users",
                    bodySMS
                )
            }
            val resE = buf.success
            val resEerr = buf.error

            withContext(Dispatchers.Main) {
                setLoading(false)
                if (resE != null) {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings"
                    )
                    analyticsHelper.reportEvent(
                        "change_email_success",
                        eventParameters
                    )
                    showToast(
                        successToastItem.copy(
                            message = resE.operationResult?.message ?: getString(strings.operationSuccess)
                        )
                    )
                    delay(2000)
                    onSuccess()
                } else {
                    if (resEerr != null) {
                        onError(
                            resEerr
                        )
                    }
                }
            }
        }
    }

    fun postSetPassword(owner: Long?,code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            setLoading(true)

            val bodySMS = HashMap<String, JsonElement>()
            bodySMS["action"] = JsonPrimitive("change_password")
            bodySMS["code"] = JsonPrimitive(code)

            val buf = withContext(Dispatchers.IO) {
                operationsMethods.postOperation(
                    owner ?: UserData.login,
                    "enter_additional_confirmation_code",
                    "users",
                    bodySMS
                )
            }
            val resE = buf.success
            val resEerr = buf.error
            withContext(Dispatchers.Main) {
                setLoading(false)
                if (resE != null) {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings"
                    )
                    analyticsHelper.reportEvent(
                        "change_password_success",
                        eventParameters
                    )
                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )
                    delay(2000)
                    onSuccess()
                } else {
                    if (resEerr != null) {
                        onError(
                            resEerr
                        )
                    }
                }
            }
        }
    }

    fun postSetPhone(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            setLoading(true)
            val bodySMS = HashMap<String, JsonElement>()
            bodySMS["code"] = JsonPrimitive(code)

            val buf = withContext(Dispatchers.IO) {
                operationsMethods.postOperation(
                    UserData.login,
                    "verify_phone",
                    "users",
                    bodySMS
                )
            }
            val resE = buf.success
            val resEerr = buf.error

            withContext(Dispatchers.Main) {
                setLoading(false)
                if (resE != null) {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings"
                    )
                    analyticsHelper.reportEvent(
                        "set_phone_success",
                        eventParameters
                    )

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    delay(2000)

                    onSuccess()
                } else {
                    if (resEerr != null) {
                        onError(resEerr)
                    }
                }
            }
        }
    }
}



