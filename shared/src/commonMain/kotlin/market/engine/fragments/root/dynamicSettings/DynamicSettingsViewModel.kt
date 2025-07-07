package market.engine.fragments.root.dynamicSettings

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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.ListItem
import market.engine.core.network.networkObjects.OperationResult
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class DynamicSettingsViewModel : CoreViewModel() {

    private val _builderDescription = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val builderDescription : StateFlow<DynamicPayload<OperationResult>?> = _builderDescription.asStateFlow()

    private val _errorSettings = MutableStateFlow<Pair<AnnotatedString, String>?>(null)
    val errorSettings : StateFlow<Pair<AnnotatedString, String>?> = _errorSettings.asStateFlow()

    private val userOperations : UserOperations by lazy { getKoin().get() }

    val deliveryCardsViewModel = DeliveryCardsViewModel()

    fun init(settingsType : String, owner : Long?) {
        viewModelScope.launch {
            setLoading(true)
            val buffer = withContext(Dispatchers.IO) {
                when(settingsType){
                    "set_watermark","set_block_rating","app_settings" -> {
                        null
                    }
                    "forgot_password","reset_password" -> {
                        userOperations.getUsersOperationsResetPassword()
                    }
                    "set_address_cards" -> {
                        null
                    }
                    "remove_bids_of_users" -> {
                        if (owner != null) {
                            operationsMethods.getOperationFields(
                                owner,
                                settingsType,
                                "offers",
                            )
                        }else{
                            null
                        }
                    }
                    "set_email" -> {
                        operationsMethods.getOperationFields(
                            owner ?: UserData.login,
                            "request_email_change",
                            "users"
                        )
                    }
                    "set_outgoing_address" -> {
                        operationsMethods.getOperationFields(
                            owner ?: UserData.login,
                            "save_outgoing_address",
                            "users"
                        )
                    }
                    "set_message_to_buyer" -> {
                        operationsMethods.getOperationFields(
                            owner ?: UserData.login,
                            "set_message_to_buyers",
                            "users"
                        )
                    }
                    "set_about_me" -> {
                        operationsMethods.getOperationFields(
                            owner ?: UserData.login,
                            "edit_about_me",
                            "users"
                        )
                    }
                    "set_phone" ->{
                        operationsMethods.getOperationFields(
                            owner ?: UserData.login,
                            "verify_phone",
                            "users"
                        )
                    }
                    else -> {
                        operationsMethods.getOperationFields(
                            owner ?: UserData.login,
                            settingsType,
                            "users"
                        )
                    }
                }
            }

            val payload = buffer?.success
            val resErr = buffer?.error

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
            builderDescription.value?.fields?.forEach { field ->
                if (field.data != null && field.key != "verifiedbycaptcha" && field.key != "captcha_image" && field.data?.jsonPrimitive?.content?.isNotBlank() == true) {
                    body[field.key ?: ""] = field.data!!
                }
            }

            if (body.isEmpty()){
                setLoading(false)
                showToast(
                    errorToastItem.copy(
                        message = getString(
                            strings.operationFailed
                        )
                    )
                )
                return@launch
            }

            val buf = withContext(Dispatchers.IO) {
                when (settingsType) {
                    "forgot_password", "reset_password" -> userOperations.postUsersOperationsResetPassword(
                        body
                    )
                    "remove_bids_of_users" -> owner?.let {
                        operationsMethods.postOperationFields(
                            it,
                            "remove_bids_of_users",
                            "offers",
                        )
                    }
                    "set_email" -> {
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
                            "request_email_change",
                            "users",
                            body
                        )
                    }
                    "set_about_me" -> {
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
                            "edit_about_me",
                            "users",
                            body
                        )
                    }
                    "set_phone" ->{
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
                            "verify_phone",
                            "users",
                            body
                        )
                    }
                    "set_outgoing_address" -> {
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
                            "save_outgoing_address",
                            "users",
                            body
                        )
                    }
                    "set_message_to_buyer" -> {
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
                            "set_message_to_buyers",
                            "users",
                            body
                        )
                    }
                    else -> {
                        operationsMethods.postOperationFields(
                            owner ?: UserData.login,
                            settingsType,
                            "users",
                            body
                        )
                    }
                }
            }
            val payload = buf?.success
            val resErr = buf?.error

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

                                    else -> payload.operationResult?.message ?: getString(strings.operationSuccess)
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
                                message = getString(
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

    fun getBlocList(type : String, onSuccess: (ArrayList<ListItem>) -> Unit) {
        viewModelScope.launch {
            val res =  withContext(Dispatchers.IO){
                val body = HashMap<String, JsonElement>()
                when(type){
                    "add_to_seller_blacklist" -> {
                        body["list_type"] = JsonPrimitive("blacklist_sellers")
                    }
                    "add_to_buyer_blacklist" -> {
                        body["list_type"] = JsonPrimitive("blacklist_buyers")
                    }
                    "add_to_whitelist" -> {
                        body["list_type"] = JsonPrimitive("whitelist_buyers")
                    }
                }
                userOperations.getUsersOperationsGetUserList(UserData.login, body)
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val resErr = res.error

                if (buffer != null) {
                    if(!buffer.body?.data.isNullOrEmpty()) {
                        onSuccess(buffer.body.data)
                    }
                }else{
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun deleteFromBlocList(type : String, id : Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val list = when(type){
                "add_to_seller_blacklist" -> {
                    "seller_blacklist"
                }
                "add_to_buyer_blacklist" -> {
                    "buyer_blacklist"
                }
                "add_to_whitelist" -> {
                    "whitelist"
                }
                else -> {
                    ""
                }
            }
            val body = HashMap<String, JsonElement>()
            body["identity"] = JsonPrimitive(id)

            val res = withContext(Dispatchers.IO){
                operationsMethods.postOperationFields(
                    UserData.login,
                    "remove_from_$list",
                    "users",
                    body
                )
            }

            withContext(Dispatchers.Main) {
                if (res.success != null){
                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )
                    onSuccess()
                }else{
                    if (res.error != null) {
                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun cancelAllBids(offerId: Long, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val body = HashMap<String, JsonElement>()
            body["comment"] = JsonPrimitive(comment)

            val eventParameters = mapOf(
                "user_id" to UserData.login,
                "profile_source" to "settings",
                "body" to body
            )
            analyticsHelper.reportEvent(
                "set_cancel_all_bids",
                eventParameters
            )

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    offerId,
                    "set_cancel_all_bids",
                    "offers",
                    body
                )
            }

            val payload = res.success
            val resErr = res.error

            withContext(Dispatchers.Main) {
                if (payload != null) {
                    if (payload.operationResult?.result == "ok") {
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        delay(2000)
                        onSuccess()
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun disabledWatermark(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "disable_watermark",
                    "users"
                )
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

    fun enabledBlockRating(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "enable_block_rating",
                    "users"
                )
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("enabled_block_rating_success", eventParameters)

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
                        analyticsHelper.reportEvent("enabled_block_rating_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun disabledBlockRating(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "disable_block_rating",
                    "users"
                )
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("disabled_block_rating_success", eventParameters)

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
                        analyticsHelper.reportEvent("disabled_block_rating_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun enabledWatermark(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "enable_watermark",
                    "users"
                )
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
}
