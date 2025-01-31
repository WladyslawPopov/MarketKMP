package market.engine.fragments.root.dynamicSettings

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.common.AnalyticsFactory
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.APIService
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class DynamicSettingsViewModel(
    val apiService: APIService,
    private val userOperations: UserOperations,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private val _builderDescription = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val builderDescription : StateFlow<DynamicPayload<OperationResult>?> = _builderDescription.asStateFlow()

    private val _errorSettings = MutableStateFlow<Pair<AnnotatedString, String>?>(null)
    val errorSettings : StateFlow<Pair<AnnotatedString, String>?> = _errorSettings.asStateFlow()

    fun init(settingsType : String, owner : Long?, code : String?) {
        when(settingsType){
            "set_login" ->{
                getSetLogin()
            }
            "set_email" ->{
                getSetEmail()
            }
            "set_password","forgot_password","reset_password" -> {
                getSetPassword(settingsType, owner)
            }
            "set_phone" -> {
                getSetPhone()
            }
            "set_about_me" -> {
                getSetAboutMe()
            }
        }
    }

    private fun getSetLogin() {
        setLoading(true)
        userRepository.updateToken()
        if (UserData.token != "") {
            viewModelScope.launch {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings",
                )
                analyticsHelper.reportEvent("view_set_login", eventParameters)

                val buffer = withContext(Dispatchers.IO) {
                    userOperations.getUsersOperationsSetLogin(UserData.login)
                }
                val res = buffer.success
                val resErr = buffer.error

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (res != null) {
                        _builderDescription.value = res
                    } else {
                        if (resErr != null) {
                            if (resErr.humanMessage.isNotEmpty()) {
                                _errorSettings.value = Pair(
                                    buildAnnotatedString {
                                        append(getString(strings.yourCurrentLogin))
                                        append("  ")
                                        withStyle(
                                            SpanStyle(
                                                color = colors.titleTextColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append(UserData.userInfo?.login.toString())
                                        }
                                    },
                                    resErr.humanMessage
                                )
                            } else {
                                onError(resErr)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getSetAboutMe() {
        setLoading(true)
        userRepository.updateToken()
        if (UserData.token != "") {
            viewModelScope.launch {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings",
                )
                analyticsHelper.reportEvent("view_set_about_me", eventParameters)

                val buffer = withContext(Dispatchers.IO) {
                    userOperations.getUsersOperationsSetAboutMe(UserData.login)
                }
                val res = buffer.success
                val resErr = buffer.error

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (res != null) {
                        _builderDescription.value = res
                    } else {
                        if (resErr != null) {
                            onError(resErr)
                        }
                    }
                }
            }
        }
    }

    private fun getSetEmail() {
        setLoading(true)
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsSetEmail(UserData.login)
            }
            val payload = buffer.success
            val resErr = buffer.error

            withContext(Dispatchers.Main) {
                setLoading(false)
                if (payload != null) {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("view_set_email", eventParameters)
                    _builderDescription.value = payload

                } else {
                    if (resErr != null) {
                       onError(resErr)
                    }
                }
            }
        }
    }

    private fun getSetPassword(settingsType: String, owner: Long?) {
        setLoading(true)
        viewModelScope.launch {
            val buffer = withContext(Dispatchers.IO) {
                when(settingsType){
                    "set_password" ->{
                        userOperations.getUsersOperationsSetPassword(owner ?: UserData.login)
                    }
                    else -> {
                        userOperations.getUsersOperationsResetPassword()
                    }
                }
            }

            val payload = buffer.success
            val resErr = buffer.error

            withContext(Dispatchers.Main) {
                setLoading(false)
                if (payload != null) {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("view_$settingsType", eventParameters)
                    _builderDescription.value = payload

                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    private fun getSetPhone(){
        userRepository.updateToken()
        if (UserData.token != "") {
            setLoading(true)
            viewModelScope.launch {
                val buffer = withContext(Dispatchers.IO) {
                    userOperations.getUsersOperationsSetPhone(UserData.login)
                }
                val payload = buffer.success
                val resErr = buffer.error

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (payload != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                        )
                        analyticsHelper.reportEvent("view_set_phone", eventParameters)
                        _builderDescription.value = payload

                    } else {
                        if (resErr != null) {
                            onError(resErr)
                        }
                    }
                }
            }
        }
    }

    suspend fun postSubmit(settingsType: String, owner: Long?) : Boolean {
        setLoading(true)
        userRepository.updateToken()
        val body = HashMap<String, JsonElement>()
        builderDescription.value?.fields?.forEach {
            if (it.data != null && it.key != "verifiedbycaptcha" && it.key != "captcha_image")
                body[it.key ?: ""] = it.data!!
        }
        if (body.isNotEmpty()) {
            val buf = withContext(Dispatchers.IO){
                when(settingsType){
                    "set_login" -> userOperations.postUsersOperationsSetLogin(UserData.login, body)
                    "set_about_me" -> userOperations.postUsersOperationsSetAboutMe(UserData.login, body)
                    "set_email" -> userOperations.postUsersOperationsSetEmail(UserData.login, body)
                    "set_password" -> userOperations.postUsersOperationsSetPassword(owner ?: UserData.login, body)
                    "set_phone" -> userOperations.postUsersOperationsSetPhone(UserData.login, body)
                    "forgot_password", "reset_password" -> userOperations.postUsersOperationsResetPassword(body)
                    else -> {
                        userOperations.postUsersOperationsSetLogin(UserData.login, body)
                    }
                }
            }
            val payload = buf.success
            val resErr = buf.error

            return withContext(Dispatchers.Main) {
                setLoading(false)
                if (payload != null) {
                    if (payload.status == "operation_success") {

                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to body
                        )
                        analyticsHelper.reportEvent("${settingsType}_success", eventParameters)

                        setLoading(false)
                        showToast(
                            successToastItem.copy(
                                message = when(settingsType){
                                    "set_email","forgot_password","reset_password" -> getString(strings.checkOutEmailToast)
                                    else -> getString(strings.operationSuccess)
                                }
                            )
                        )
                        _builderDescription.value = _builderDescription.value?.copy(
                            body = payload.body
                        )
                        return@withContext true
                    } else {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to body
                        )
                        analyticsHelper.reportEvent("${settingsType}_failed", eventParameters)

                        _builderDescription.value = _builderDescription.value?.copy(
                            fields = payload.recipe?.fields ?: payload.fields
                        )

                        showToast(
                            errorToastItem.copy(
                                message = payload.recipe?.operationResult?.message ?: getString(strings.operationFailed)
                            )
                        )
                        return@withContext false
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                    return@withContext false
                }
            }
        } else {
            showToast(
                errorToastItem.copy(
                    message = getString(strings.operationFailed)
                )
            )
            return false
        }
    }
}
