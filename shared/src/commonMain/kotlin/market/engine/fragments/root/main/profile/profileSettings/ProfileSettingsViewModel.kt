package market.engine.fragments.root.main.profile.profileSettings

import androidx.lifecycle.SavedStateHandle

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.compressImage
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.network.networkObjects.Choices
import market.engine.core.utils.Base64.encodeToBase64
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

class ProfileSettingsViewModel(val component : ProfileSettingsComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    private val _genderSelects = savedStateHandle.getSavedStateFlow(
        scope,
        "genderSelects",
        emptyList(),
        ListSerializer(Choices.serializer())
    )
    val genderSelects : StateFlow<List<Choices>> = _genderSelects.state

    val sellerSettingsItems = mutableListOf<NavigationItem>()
    val addressItems = mutableListOf<NavigationItem>()
    val blackListItems = mutableListOf<NavigationItem>()

    init {
        scope.launch {
            sellerSettingsItems.addAll(listOf(
                NavigationItem(
                    title = getString(strings.pageAboutMeParameterName),
                    icon = drawables.infoIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_about_me")
                    }
                ),
                NavigationItem(
                    title = getString(strings.vacationTitle),
                    icon = drawables.vacationIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_vacation")
                    }
                ),
                NavigationItem(
                    title = getString(strings.messageToBuyersLabel),
                    icon = drawables.dialogIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_message_to_buyer")
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsBiddingStepsLabel),
                    icon = drawables.listIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_bidding_step")
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsAutoFeedbacksLabel),
                    icon = drawables.timerListIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_auto_feedback")
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsWatermarkLabel),
                    icon = drawables.watermarkIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_watermark")
                    }
                )
            ))

            addressItems.addAll(listOf(
                NavigationItem(
                    title = getString(strings.outgoingAddressLabel),
                    icon = drawables.locationIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_outgoing_address")
                    }
                ),
                NavigationItem(
                    title = getString(strings.addressCardsTitle),
                    icon = drawables.emptyOffersIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_address_cards")
                    }
                )
            ))

            blackListItems.addAll(listOf(
                NavigationItem(
                    title = getString(strings.settingsBlackListSellersLabel),
                    icon = drawables.blackSellersIcon,
                    onClick = {
                        component.navigateToDynamicSettings("add_to_seller_blacklist")
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsBlackListBuyersLabel),
                    icon = drawables.blackBuyersIcon,
                    onClick = {
                        component.navigateToDynamicSettings("add_to_buyer_blacklist")
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsWhiteListBuyersLabel),
                    icon = drawables.whiteBuyersIcon,
                    onClick = {
                        component.navigateToDynamicSettings("add_to_whitelist")
                    }
                ),
                NavigationItem(
                    title = getString(strings.settingsBlockRatingLabel),
                    icon = drawables.blockRatingIcon,
                    onClick = {
                        component.navigateToDynamicSettings("set_block_rating")
                    }
                )
            ))
        }
    }

    fun refreshPage(){
        setLoading(true)
        scope.launch {
            updateUserInfo()
            delay(2000)
            setLoading(false)
        }
        refresh()
    }

    fun getGenderSelects(){
        scope.launch {
            val buffer = withContext(Dispatchers.IO) {
                operationsMethods.getOperationFields(
                    UserData.login,
                    "set_gender",
                    "users",
                )
            }
            val payload = buffer.success
            val resErr = buffer.error
            withContext(Dispatchers.Main) {
                if (payload != null) {
                    payload.fields.firstOrNull()?.choices?.let {
                        _genderSelects.value = it
                    }
                } else {
                    resErr?.let { onError(it) }
                }
            }
        }
    }

    fun setGender(gender : String){
        if (UserData.token != ""){
            val body = HashMap<String, JsonElement>()
            val i = genderSelects.value.find { it.name == gender}
            if (i != null){
                body["new_gender"] = i.code ?: JsonPrimitive(0)
            }

            scope.launch {
                val buffer = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        UserData.login,
                        "set_gender",
                        "users",
                        body
                    )
                }

                val res = buffer.success
                val error = buffer.error

                withContext(Dispatchers.Main){
                    if (res != null){
                        updateUserInfo()
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        if (error != null){
                            onError(error)
                        }
                    }
                }
            }
        }
    }

    fun uploadNewAvatar(file : PlatformFile){
        scope.launch {
            if (UserData.token != "") {
                val barr = file.readBytes()
                val resizeImage = compressImage(barr, 60)
                val body = HashMap<String, JsonElement>()
                body["new_avatar"] = JsonPrimitive(resizeImage.encodeToBase64())

                val buffer = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        UserData.login,
                        "set_avatar",
                        "users",
                        body
                    )
                }

                val res = buffer.success
                val error = buffer.error

                withContext(Dispatchers.Main) {
                    if (res != null) {
                        updateUserInfo()
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        if (error != null) {
                            onError(error)
                        }
                    }
                }
            }
        }
    }

    fun deleteAvatar(){
        if(UserData.token != "") {
            scope.launch {
                val buffer = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        UserData.login,
                        "unset_avatar",
                        "users"
                    )
                }
                val res = buffer.success
                val error = buffer.error

                withContext(Dispatchers.Main) {
                    if (res != null) {
                        updateUserInfo()
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                    } else {
                        if (error != null) {
                            onError(error)
                        }
                    }
                }
            }
        }
    }
}
