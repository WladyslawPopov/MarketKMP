package market.engine.fragments.root.dynamicSettings

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class DynamicSettingsViewModel : BaseViewModel() {

    private val _builderDescription = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val builderDescription : StateFlow<DynamicPayload<OperationResult>?> = _builderDescription.asStateFlow()

    private val _errorSettings = MutableStateFlow<Pair<AnnotatedString, String>?>(null)
    val errorSettings : StateFlow<Pair<AnnotatedString, String>?> = _errorSettings.asStateFlow()

    val responseGetLoadCards = mutableStateOf(emptyList<DeliveryAddress>())
    val deliveryFields = mutableStateOf<List<Fields>>(emptyList())

    fun init(settingsType : String, owner : Long?) {
        viewModelScope.launch {
            setLoading(true)
            val buffer = withContext(Dispatchers.IO) {
                when(settingsType){
                    "set_login" ->{
                        userOperations.getUsersOperationsSetLogin(UserData.login)
                    }
                    "set_email" ->{
                        userOperations.getUsersOperationsSetEmail(UserData.login)
                    }
                    "set_password" -> {
                        userOperations.getUsersOperationsSetPassword(owner ?: UserData.login)
                    }
                    "forgot_password","reset_password" -> {
                        userOperations.getUsersOperationsResetPassword()
                    }
                    "set_phone" -> {
                        userOperations.getUsersOperationsSetPhone(UserData.login)
                    }
                    "set_about_me" -> {
                        userOperations.getUsersOperationsSetAboutMe(UserData.login)
                    }
                    "set_vacation" -> {
                        userOperations.getUsersOperationsSetVacation(UserData.login)
                    }
                    "set_message_to_buyer" -> {
                        userOperations.getUsersOperationsSetMessageToBuyer(UserData.login)
                    }
                    "set_bidding_step" -> {
                        userOperations.getUsersOperationsSetBiddingStep(UserData.login)
                    }
                    "set_auto_feedback" -> {
                        userOperations.getUsersOperationsSetAutoFeedback(UserData.login)
                    }
                    "set_outgoing_address" -> {
                        userOperations.getUsersOperationsSetOutgoingAddress(UserData.login)
                    }
                    "set_address_cards" -> {
                        viewModelScope.launch {
                            responseGetLoadCards.value = getDeliveryCards() ?: emptyList()
                            deliveryFields.value = getDeliveryFields() ?: emptyList()
                        }
                        userOperations.getUsersOperationsSetLogin(UserData.login)
                    }
                    "remove_bids_of_users" -> {
                        offerOperations.getOfferOperationsRemoveBidsOfUsers(UserData.login)
                    }
                    "add_to_seller_blacklist", "add_to_buyer_blacklist", "add_to_whitelist" -> {
                        userOperations.getUsersOperationsGetSettingsList(UserData.login, settingsType)
                    }
                    else -> {
                        userOperations.getUsersOperationsSetLogin(UserData.login)
                    }
                }
            }

            val payload = buffer.success
            val resErr = buffer.error

            withContext(Dispatchers.Main) {
                setLoading(false)
                if (payload != null) {
                    _builderDescription.value = payload
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

    fun postSubmit(settingsType: String, owner: Long?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            setLoading(true)
            userRepository.updateToken()

            val body = HashMap<String, JsonElement>()

            builderDescription.value?.fields?.forEach {
                if (it.data != null && it.key != "verifiedbycaptcha" && it.key != "captcha_image")
                    body[it.key ?: ""] = it.data!!
            }

            val buf = withContext(Dispatchers.IO) {
                when (settingsType) {
                    "set_login" -> userOperations.postUsersOperationsSetLogin(
                        UserData.login,
                        body
                    )

                    "set_about_me" -> userOperations.postUsersOperationsSetAboutMe(
                        UserData.login,
                        body
                    )

                    "set_email" -> userOperations.postUsersOperationsSetEmail(
                        UserData.login,
                        body
                    )

                    "set_password" -> userOperations.postUsersOperationsSetPassword(
                        owner ?: UserData.login, body
                    )

                    "set_phone" -> userOperations.postUsersOperationsSetPhone(
                        UserData.login,
                        body
                    )

                    "forgot_password", "reset_password" -> userOperations.postUsersOperationsResetPassword(
                        body
                    )

                    "set_vacation" -> userOperations.postUsersOperationsSetVacation(
                        UserData.login,
                        body
                    )

                    "set_message_to_buyer" -> userOperations.postUsersOperationsSetMessageToBuyer(
                        UserData.login,
                        body
                    )

                    "set_bidding_step" -> userOperations.postUsersOperationsSetBiddingStep(
                        UserData.login,
                        body
                    )

                    "set_auto_feedback" -> userOperations.postUsersOperationsSetAutoFeedback(
                        UserData.login,
                        body
                    )

                    "set_outgoing_address" -> userOperations.postUsersOperationsSetOutgoingAddress(
                        UserData.login,
                        body
                    )

                    "remove_bids_of_users" -> offerOperations.postOfferOperationsRemoveBidsOfUsers(
                        UserData.login,
                        body
                    )
                    "add_to_seller_blacklist", "add_to_buyer_blacklist", "add_to_whitelist" -> {
                        userOperations.postUsersOperationAddList(
                            UserData.login,
                            body,
                            settingsType
                        )
                    }
                    else -> {
                        userOperations.postUsersOperationsSetLogin(UserData.login, body)
                    }
                }
            }
            val payload = buf.success
            val resErr = buf.error

            withContext(Dispatchers.Main) {
                setLoading(false)

                if (payload != null) {
                    if (payload.status == "operation_success") {

                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to body
                        )
                        analyticsHelper.reportEvent("${settingsType}_success", eventParameters)

                        showToast(
                            successToastItem.copy(
                                message = when (settingsType) {
                                    "set_email", "forgot_password", "reset_password" -> getString(
                                        strings.checkOutEmailToast
                                    )

                                    else -> getString(strings.operationSuccess)
                                }
                            )
                        )
                        _builderDescription.value = _builderDescription.value?.copy(
                            body = payload.body
                        )
                        delay(2000)
                        onSuccess()
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
                                message = payload.recipe?.operationResult?.message ?: getString(
                                    strings.operationFailed
                                )
                            )
                        )
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun getBlocList(onSuccess: () -> Unit) {
        viewModelScope.launch {


        }
    }

    fun enabledWatermark(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                userOperations.postUsersOperationsSetWatermarkEnabled(UserData.login)
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("enabled_watermark_success", eventParameters)

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    onSuccess()
                } else {
                    if (res.error != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "human_message" to res.error?.humanMessage,
                            "error_code" to res.error?.errorCode
                        )
                        analyticsHelper.reportEvent("enabled_watermark_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun disabledWatermark(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                userOperations.postUsersOperationsSetWatermarkDisabled(UserData.login)
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("disabled_watermark_success", eventParameters)

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    onSuccess()
                } else {
                    if (res.error != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "human_message" to res.error?.humanMessage,
                            "error_code" to res.error?.errorCode
                        )
                        analyticsHelper.reportEvent("disabled_watermark_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }
}
