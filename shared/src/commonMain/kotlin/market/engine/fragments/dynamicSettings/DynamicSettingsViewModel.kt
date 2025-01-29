package market.engine.fragments.dynamicSettings

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
import market.engine.common.AnalyticsFactory
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

    fun init(settingsType : String) {
        when(settingsType){
            "set_login" ->{
                getSetLogin()
            }
        }
    }

    private fun getSetLogin() {
        setLoading(true)
        userRepository.updateToken()
        if (UserData.token != "") {

            val eventParameters = mapOf(
                "user_id" to UserData.login,
                "profile_source" to "settings",
            )
            analyticsHelper.reportEvent("view_set_login", eventParameters)

            viewModelScope.launch(Dispatchers.IO) {
                val buffer =
                    userOperations.getUsersOperationsSetLogin(UserData.login)
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
                                        withStyle(SpanStyle(
                                            color = colors.titleTextColor,
                                            fontWeight = FontWeight.Bold
                                        )) {
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
}
