package market.engine.fragments.root.main.profile.profileSettings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.fragments.root.main.profile.navigation.ProfileConfig

interface ProfileSettingsComponent {
    val model : Value<Model>

    data class Model(
        var type : ProfileSettingsTypes,
        val profileSettingsViewModel: ProfileSettingsViewModel,
        val backHandler: BackHandler
    )

    fun selectProfileSettingsPage(type: ProfileSettingsTypes)

    fun navigateToDynamicSettings(settingsType : String)

    fun goToBack()
}

class DefaultProfileSettingsComponent(
    val type : ProfileSettingsTypes,
    val selectedPage : (ProfileSettingsTypes) -> Unit,
    val profileNavigation: StackNavigation<ProfileConfig>,
    val goToDynamicSettings : (String) -> Unit,
    componentContext: ComponentContext,
) : ProfileSettingsComponent, ComponentContext by componentContext
{

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private  val profileSettingsViewModel = ProfileSettingsViewModel()

    private val _model = MutableValue(
        ProfileSettingsComponent.Model(
            type = type,
            profileSettingsViewModel = profileSettingsViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        lifecycle.doOnResume {
            profileSettingsViewModel.updateUserInfo()
            if (UserData.token == ""){
                goToBack()
            }
        }

        when(type){
            ProfileSettingsTypes.GLOBAL_SETTINGS -> {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings"
                )
                analyticsHelper.reportEvent("view_settings_profile_general", eventParameters)
            }

            ProfileSettingsTypes.SELLER_SETTINGS -> {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings"
                )
                analyticsHelper.reportEvent("view_settings_profile_seller", eventParameters)
            }
            ProfileSettingsTypes.ADDITIONAL_SETTINGS -> {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings"
                )
                analyticsHelper.reportEvent("view_settings_profile_other", eventParameters)
            }
        }
    }

    override fun selectProfileSettingsPage(type: ProfileSettingsTypes) {
        selectedPage(type)
    }

    override fun navigateToDynamicSettings(settingsType: String) {
        goToDynamicSettings(settingsType)
    }

    override fun goToBack() {
        profileNavigation.replaceCurrent(ProfileConfig.ProfileScreen())
    }
}
