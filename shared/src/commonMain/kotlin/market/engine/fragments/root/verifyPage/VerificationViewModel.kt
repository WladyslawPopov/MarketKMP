package market.engine.fragments.root.verifyPage

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                getSetPassword()
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
                val buffer = userOperations.postUsersOperationsConfirmEmail(owner, body)
                val res = buffer.success
                val resErr = buffer.error

                if (res != null) {
                    if (res.status == "ok") {
                        if (res.body != null) {
                            val bodySMS = HashMap<String, String>()
                            bodySMS["action"] = "change_email"
                            val buf = userOperations.postUsersOperationsRAC(
                                owner,
                                bodySMS
                            )
                            val resSMS = buf.success
                            val resSmsErr = buf.error
                            withContext(Dispatchers.Main) {
                                if (resSMS != null) {
                                    showToast(
                                        successToastItem.copy(
                                            message = resSMS.humanMessage ?: ""
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

    private fun getSetPassword() {
        viewModelScope.launch {
            val body = HashMap<String, String>()
            body["action"] = "change_password"
            val buffer = userOperations.postUsersOperationsRAC(UserData.login, body)
            val res = buffer.success
            val resErr = buffer.error
            withContext(Dispatchers.Main) {
                if (res != null) {
                    showToast(
                        successToastItem.copy(
                            message = res.humanMessage ?: ""
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

    suspend fun postSetEmail(code: String) : Boolean {
        setLoading(true)

        val bodySMS = HashMap<String, String>()
        bodySMS["action"] = "change_email"
        bodySMS["code"] = code

        val buf = withContext(Dispatchers.IO) {

            userOperations.postUsersOperationsEACC(
                UserData.login, bodySMS
            )
        }
        val resE = buf.success
        val resEerr = buf.error
        return withContext(Dispatchers.Main) {
            setLoading(false)
            if (resE != null) {
                if (resE.success) {

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
                            message = getString(strings.operationSuccess)
                        )
                    )
                    return@withContext true
                } else {
                    showToast(
                        errorToastItem.copy(
                            message = getString(strings.operationFailed)
                        )
                    )
                    return@withContext false
                }
            } else {
                if (resEerr != null) {
                    onError(
                        resEerr
                    )
                }
                return@withContext false
            }
        }
    }

    suspend fun postSetPassword(code: String) : Boolean {
        setLoading(true)

        val bodySMS = HashMap<String, String>()
        bodySMS["action"] = "change_password"
        bodySMS["code"] = code

        val buf = withContext(Dispatchers.IO) {

            userOperations.postUsersOperationsEACC(
                UserData.login, bodySMS
            )
        }
        val resE = buf.success
        val resEerr = buf.error
        return withContext(Dispatchers.Main) {
            setLoading(false)
            if (resE != null) {
                if (resE.success) {

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
                    return@withContext true
                } else {
                    showToast(
                        errorToastItem.copy(
                            message = getString(strings.operationFailed)
                        )
                    )
                    return@withContext false
                }
            } else {
                if (resEerr != null) {
                    onError(
                        resEerr
                    )
                }
                return@withContext false
            }
        }
    }

    suspend fun postSetPhone(code: String) : Boolean{
        val bodySMS = HashMap<String, String>()
        bodySMS["code"] = code

        val buf =  withContext(Dispatchers.IO) {
            userOperations.postUsersOperationsVerifyPhone(
                UserData.login, bodySMS
            )
        }
        val resE = buf.success
        val resEerr = buf.error
        return withContext(Dispatchers.Main) {
            setLoading(false)
            if (resE != null) {
                if (resE.success) {

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
                    return@withContext true
                } else {
                    showToast(
                        errorToastItem.copy(
                            message = getString(strings.operationFailed)
                        )
                    )
                    return@withContext false
                }
            } else {
                if (resEerr != null) {
                    onError(resEerr)
                }
                return@withContext false
            }
        }
    }
}



