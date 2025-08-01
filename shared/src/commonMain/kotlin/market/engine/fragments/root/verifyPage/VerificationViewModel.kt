package market.engine.fragments.root.verifyPage

import androidx.lifecycle.SavedStateHandle
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
import market.engine.core.network.functions.UserOperations
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class VerificationViewModel(
    val settingsType: String,
    val owner: Long?,
    val code: String?,
    val component: VerificationComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    val userOperations : UserOperations by lazy { getKoin().get() }

    init {
        refresh()
        setPage()
        val eventParameters = mapOf("settings_type" to settingsType)
        analyticsHelper.reportEvent("view_verification_page", eventParameters)
    }

    fun setPage(type: String = settingsType){
        if(type != "") {
            viewModelScope.launch {
                try {
                    setLoading(true)
                    val bodySMS = HashMap<String, JsonElement>()
                    bodySMS["action"] = JsonPrimitive(type)

                    val buf = withContext(Dispatchers.IO) {
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
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
                        } else {
                            if (resSmsErr != null) {
                                onError(
                                    resSmsErr
                                )
                            }
                            goBack()
                        }
                    }
                } catch (e : ServerErrorException){
                    onError(e)
                } catch (e : Exception){
                    onError(ServerErrorException(e.message ?: ""))
                } finally {
                    setLoading(false)
                }
            }
        }else{
            if (code != null && owner != null) {
                getSetEmail(owner, code)
            }
        }
    }

    fun postCode(code: String) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val bodySMS = HashMap<String, JsonElement>()
                bodySMS["action"] = JsonPrimitive(settingsType)
                bodySMS["code"] = JsonPrimitive(code)

                val buf = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        UserData.login,
                        "enter_additional_confirmation_code",
                        "users",
                        bodySMS
                    )
                }
                val resE = buf.success
                val resEerr = buf.error

                withContext(Dispatchers.Main) {
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
                                message = resE.operationResult?.message
                                    ?: getString(strings.operationSuccess)
                            )
                        )
                        delay(2000)
                        goBack()
                    } else {
                        if (resEerr != null) {
                            onError(
                                resEerr
                            )
                        }
                    }
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: ""))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun getSetEmail(owner: Long, code: String) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val body = HashMap<String, JsonElement>()
                body["code"] = JsonPrimitive(code)
                val buffer = withContext(Dispatchers.IO) { userOperations.postUsersOperationsConfirmEmail(owner, body) }
                val res = buffer.success
                val resErr = buffer.error

                if (res != null) {
                    if (res.status == "ok") {
                        val body = res.body?.action
                        if (body != null) {
                            setPage(body)
                        } else {
                            onError(ServerErrorException(res.errorCode ?:"", res.humanMessage ?: ""))
                            goBack()
                        }
                    } else {
                        if (res.status == "operation_success") {
                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )
                            delay(2000)
                            goBack()
                        } else {
                            showToast(
                                errorToastItem.copy(
                                    message = res.humanMessage ?: ""
                                )
                            )
                            delay(2000)
                            goBack()
                        }
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
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
}



