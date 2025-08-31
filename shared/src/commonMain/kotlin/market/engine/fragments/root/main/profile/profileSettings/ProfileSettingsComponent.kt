package market.engine.fragments.root.main.profile.profileSettings

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.fragments.root.main.profile.ProfileConfig

interface ProfileSettingsComponent {
    val model : Value<Model>

    data class Model(
        var type : ProfileSettingsTypes,
        val profileSettingsViewModel: ProfileSettingsViewModel,
        val backHandler: BackHandler
    )

    fun selectProfileSettingsPage(type: ProfileSettingsTypes)

    fun navigateToDynamicSettings(settingsType : String)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultProfileSettingsComponent(
    val type : ProfileSettingsTypes,
    val selectedPage : (ProfileSettingsTypes) -> Unit,
    val profileNavigation: StackNavigation<ProfileConfig>,
    val goToDynamicSettings : (String) -> Unit,
    componentContext: JetpackComponentContext,
) : ProfileSettingsComponent, JetpackComponentContext by componentContext
{

    private  val profileSettingsViewModel = viewModel("ProfileSettingsViewModel"){
        ProfileSettingsViewModel(this@DefaultProfileSettingsComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        ProfileSettingsComponent.Model(
            type = type,
            profileSettingsViewModel = profileSettingsViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    val backCallback = object : BackCallback(){
        override fun onBack() {
            profileNavigation.pop()
        }
    }

    init {
        backHandler.register(backCallback)

        lifecycle.doOnResume {
            profileSettingsViewModel.updateUserInfo()
            if (UserData.token == ""){
                profileNavigation.pop()
            }
        }

        lifecycle.doOnDestroy {
            profileSettingsViewModel.onClear()
        }

        when(type){
            ProfileSettingsTypes.GLOBAL_SETTINGS -> {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings"
                )
                profileSettingsViewModel.analyticsHelper.reportEvent("view_settings_profile_general", eventParameters)
            }

            ProfileSettingsTypes.SELLER_SETTINGS -> {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings"
                )
                profileSettingsViewModel.analyticsHelper.reportEvent("view_settings_profile_seller", eventParameters)
            }
            ProfileSettingsTypes.ADDITIONAL_SETTINGS -> {
                val eventParameters = mapOf(
                    "user_id" to UserData.login,
                    "profile_source" to "settings"
                )
                profileSettingsViewModel.analyticsHelper.reportEvent("view_settings_profile_other", eventParameters)
            }
        }
    }

    override fun selectProfileSettingsPage(type: ProfileSettingsTypes) {
        selectedPage(type)
    }

    override fun navigateToDynamicSettings(settingsType: String) {
        goToDynamicSettings(settingsType)
    }
}
