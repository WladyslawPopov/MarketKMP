package market.engine.fragments.verifyPage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.common.AnalyticsFactory
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.APIService
import market.engine.core.network.functions.UserOperations
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class VerificationViewModel(
    val apiService: APIService,
    private val userOperations: UserOperations,
    val userRepository: UserRepository,
) : BaseViewModel() {

    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()


    fun init(settingsType: String) {
        when (settingsType) {
            "set_email" -> {

            }

            "set_password" -> {
                getSetPassword()
            }

            else -> {}
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



