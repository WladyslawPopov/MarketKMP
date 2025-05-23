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
import kotlinx.serialization.json.jsonPrimitive
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
                    "set_watermark","set_block_rating" -> {
                        null
                    }
                    "forgot_password","reset_password" -> {
                        userOperations.getUsersOperationsResetPassword()
                    }
                    "set_address_cards" -> {
                        viewModelScope.launch {
                            responseGetLoadCards.value = getDeliveryCards() ?: emptyList()
                            deliveryFields.value = getDeliveryFields() ?: emptyList()
                        }
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
                            "request_phone_change",
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
                            "request_phone_change",
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
                            UserData.login,
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
}
