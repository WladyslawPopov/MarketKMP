package market.engine.fragments.root.dynamicSettings

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.Platform
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.ListItem
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.filterContents.deliveryCardsContents.DeliveryCardsViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

data class DynamicSettingsState(
    val appBarState: SimpleAppBarData = SimpleAppBarData(),
    val titleText: String = "",
    val fields: List<Fields> = emptyList(),
    val errorMessage: Pair<AnnotatedString, String>? = null,
)

class DynamicSettingsViewModel(
    val settingsType: String,
    val owner : Long? = null,
    val code : String? = null,
    val component: DynamicSettingsComponent
) : CoreViewModel() {

    private val userOperations : UserOperations by lazy { getKoin().get() }

    val deliveryCardsViewModel = DeliveryCardsViewModel()

    private val _dynamicSettingsState = MutableStateFlow(DynamicSettingsState())
    val dynamicSettingsState = _dynamicSettingsState.asStateFlow()

    private val _blocList = MutableStateFlow<List<ListItem>>(emptyList())
    val blocList = _blocList.asStateFlow()

    init {
        setUpPage()

        val eventParameters = mapOf(
            "user_id" to UserData.login,
            "profile_source" to "settings"
        )
        analyticsHelper.reportEvent("view_$settingsType", eventParameters)
    }

    fun setUpPage(){
        refresh()
        viewModelScope.launch {
            try {
                setLoading(true)

                val buffer = withContext(Dispatchers.IO) {
                    when(settingsType){
                        "set_watermark","set_block_rating","app_settings","set_address_cards" -> {
                            null
                        }
                        "forgot_password","reset_password" -> {
                            userOperations.getUsersOperationsResetPassword()
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
                                "request_phone_verification",
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

                val title = when (settingsType) {
                    "app_settings" -> {
                        getString(strings.settingsTitleApp)
                    }

                    "set_about_me" -> {
                        payload?.title ?: payload?.description ?: ""
                    }

                    "set_vacation" -> {
                        getString(strings.vacationTitle)
                    }

                    "set_bidding_step" -> {
                        getString(strings.settingsBiddingStepsLabel)
                    }

                    "set_auto_feedback" -> {
                        getString(strings.settingsAutoFeedbacksLabel)
                    }

                    "set_watermark" -> {
                        getString(strings.settingsWatermarkLabel)
                    }

                    "set_address_cards" -> {
                        getString(strings.addressCardsTitle)
                    }

                    "set_block_rating" ->{
                        getString(strings.settingsBlockRatingLabel)
                    }

                    "cancel_all_bids" ->{
                        getString(strings.cancelAllBidsTitle)
                    }

                    "remove_bids_of_users" -> {
                        getString(strings.cancelAllBidsTitle)
                    }

                    "set_phone" -> {
                        getString(strings.htmlVerifyLabel)
                    }

                    "set_message_to_buyer" -> {
                        getString(strings.headerMessageToBuyersLabel)
                    }

                    "set_outgoing_address" -> {
                        getString(strings.outgoingAddressHeaderLabel)
                    }

                    "add_to_seller_blacklist", "add_to_buyer_blacklist", "add_to_whitelist" ->{
                        getBlocList()
                        payload?.description ?: payload?.title ?: ""
                    }

                    else -> {
                        payload?.description ?: payload?.title ?: ""
                    }
                }

                _dynamicSettingsState.value = DynamicSettingsState(
                    titleText = title,
                    fields = payload?.fields ?: emptyList(),
                    errorMessage = if (resErr?.humanMessage?.isNotEmpty() == true) {
                        Pair(
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
                    }else{
                        null
                    },
                    appBarState = SimpleAppBarData(
                        onBackClick = {
                            component.onBack()
                        },
                        listItems = listOf(
                            NavigationItem(
                                title = "",
                                icon = drawables.recycleIcon,
                                tint = colors.inactiveBottomNavIconColor,
                                hasNews = false,
                                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                                badgeCount = null,
                                onClick = {
                                    setUpPage()
                                }
                            ),
                        )
                    )
                )
            }catch (e : ServerErrorException){
                onError(e)
            } catch (e : Exception){
                onError(ServerErrorException(
                    e.message ?: "",
                    e.message ?: ""
                ))
            } finally {
                setLoading(false)
            }
        }
    }

    fun postSubmit() {
        viewModelScope.launch {
            setLoading(true)
            userRepository.updateToken()

            val body = HashMap<String, JsonElement>()
            dynamicSettingsState.value.fields.forEach { field ->
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
                            "request_phone_verification",
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

                        delay(1500)

                        val body = payload.body?.jsonObject["action"]?.jsonPrimitive?.content

                        if (body?.isNotBlank() == true) {
                            component.goToVerificationPage(body,owner, code)
                        } else {
                            component.onBack()
                        }

                    } else {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to body
                        )
                        analyticsHelper.reportEvent("${settingsType}_failed", eventParameters)

                        _dynamicSettingsState.update {
                            it.copy(
                                fields = payload.recipe?.fields ?: payload.fields
                            )
                        }

                        showToast(
                            errorToastItem.copy(
                                message = payload.recipe?.globalErrorMessage ?: getString(strings.operationFailed)
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

    fun removeBidsOfUser(){
        val field = dynamicSettingsState.value.fields.find { it.key == "bidders" } ?: return
        val data = field.data?.jsonArray
        field.data = buildJsonArray {
            field.choices?.forEachIndexed { index, choices ->
                if(data?.get(index) == choices.code){
                    val exData = choices.extendedFields?.find { it.data != null }?.data
                    if (exData != null) {
                        add(
                            buildJsonObject {
                                choices.code?.jsonPrimitive?.let { code ->
                                    put(
                                        "code",
                                        code
                                    )
                                }
                                put("comment", exData)
                            }
                        )
                    }else{
                        choices.code?.let { code -> add(code) }
                    }
                }
            }
        }

        postSubmit()
    }

    fun getBlocList() {
        viewModelScope.launch {
            val res =  withContext(Dispatchers.IO){
                val body = HashMap<String, JsonElement>()
                when(settingsType){
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
                    _blocList.value = buffer.body?.data ?: emptyList()
                }else{
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun deleteFromBlocList(id : Long) {
        viewModelScope.launch {
            val list = when(settingsType){
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
                    getBlocList()
                }else{
                    if (res.error != null) {
                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun cancelAllBids(field : Fields) {
        viewModelScope.launch {
            val body = HashMap<String, JsonElement>()
            body["comment"] = field.data ?: JsonPrimitive("")

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
                    owner ?: 1,
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
                        component.onBack()
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun disabledWatermark() {
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

                    updateUserInfo()
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

    fun enabledWatermark() {
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

                    updateUserInfo()
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

    fun changeTheme(value : Boolean) {
        settings.updateThemeMode(if (value) "day" else "night")

        val eventParameters =
            mapOf("mode_theme" to if (value) "day" else "night")
        analyticsHelper.reportEvent(
            "change_theme",
            eventParameters
        )

        updateUserInfo()

        setUpPage()
    }

    fun enabledBlockRating() {
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

                    updateUserInfo()
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

    fun setDescription(description: String) {
        val text = KsoupEntities.decodeHtml(description)
        _dynamicSettingsState.update { page ->
            val date = page.fields.map {
                if(it.widgetType == "text_area"){
                    it.copy(data = JsonPrimitive(text) )
                } else it.copy()
            }

            page.copy(
                fields = date
            )
        }
    }

    fun disabledBlockRating() {
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

                    updateUserInfo()
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
}
