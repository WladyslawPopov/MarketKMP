package market.engine.fragments.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.common.showReviewManager
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.items.DeepLink
import market.engine.core.repositories.SettingsRepository
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.contactUs.ContactUsComponent
import market.engine.fragments.root.contactUs.DefaultContactUsComponent
import market.engine.fragments.root.dynamicSettings.DefaultDynamicSettingsComponent
import market.engine.fragments.root.dynamicSettings.DynamicSettingsComponent
import market.engine.fragments.root.login.DefaultLoginComponent
import market.engine.fragments.root.login.LoginComponent
import market.engine.fragments.root.main.DefaultMainComponent
import market.engine.fragments.root.main.MainComponent
import market.engine.fragments.root.registration.DefaultRegistrationComponent
import market.engine.fragments.root.registration.RegistrationComponent
import market.engine.fragments.root.verifyPage.DefaultVerificationComponent
import market.engine.fragments.root.verifyPage.VerificationComponent
import org.koin.mp.KoinPlatform.getKoin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    val model : Value<Model>
    data class Model(
        val backHandler: BackHandler
    )

    sealed class Child {
        data class MainChild(val component: MainComponent) : Child()
        data class LoginChild(val component: LoginComponent) : Child()
        data class RegistrationChild(val component: RegistrationComponent) : Child()
        data class ContactUsChild(val component: ContactUsComponent) : Child()
        data class VerificationChildMain(val component: VerificationComponent) : Child()
        data class DynamicSettingsChild(val component: DynamicSettingsComponent) : Child()
    }

    fun updateURL (url : DeepLink)

    fun updateOrientation (orientation : Int)
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    var deepLink: DeepLink?,
) : RootComponent, ComponentContext by componentContext {

    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    private val settingsHelper : SettingsRepository = getKoin().get()

    val viewModel = CoreViewModel()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> by lazy {
        childStack(
            source = navigation,
            serializer = RootConfig.serializer(),
            initialConfiguration = RootConfig.Main,
            handleBackButton = true,
            childFactory = ::createChild
        )
    }

    private val _model = MutableValue(
        RootComponent.Model(
            backHandler = backHandler
        )
    )
    override val model = _model

    override fun updateURL(url: DeepLink) {
        viewModel.viewModelScope.launch {
            delay(300)
            deepLink = url
            when {
                childStack.active.instance is RootComponent.Child.MainChild -> {
                    (childStack.active.instance as? RootComponent.Child.MainChild)?.component?.handleDeepLink(
                        url
                    )
                }

                else -> {
                    navigation.replaceAll(RootConfig.Main)
                    (childStack.active.instance as? RootComponent.Child.MainChild)?.component?.handleDeepLink(
                        url
                    )
                }
            }
        }
    }

    override fun updateOrientation(orientation: Int) {
        try {
            val activeChild = childStack.value.active.instance
            if (activeChild is RootComponent.Child.MainChild) {
                activeChild.component.updateOrientation(orientation)
            }
        } catch (e: Exception) {
            println("Ignoring orientation update during navigation: ${e.message}")
        }

        println("upd orientation: $orientation")
    }

    init {

        if(deepLink != null){
            updateURL(deepLink!!)
        }

        val isFirstLaunch = settingsHelper.getSettingValue("isFirstLaunch", true)
        if (isFirstLaunch == true) {
            settingsHelper.setSettingValue("isFirstLaunch", false)
            analyticsHelper.reportEvent("launch_first_time", mapOf())
        }

        analyticsHelper.reportEvent("start_session", mapOf("traffic_source" to "direct"))

        val appAttributes = mapOf("app_version" to SAPI.version)
        analyticsHelper.updateUserProfile(appAttributes)

        var countLaunch = settingsHelper.getSettingValue("count_launch", 0) ?: 0
        settingsHelper.setSettingValue("count_launch", ++countLaunch)


        showReviewManager()
    }

    private fun createChild(rootConfig: RootConfig, componentContext: ComponentContext): RootComponent.Child =
        when (rootConfig) {
            is RootConfig.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext
                )
            )
            is RootConfig.Login -> RootComponent.Child.LoginChild(
                DefaultLoginComponent(
                    componentContext,
                    isReset = rootConfig.reset,
                    navigateToRegistration = {
                        navigation.pushNew(RootConfig.Registration)
                    },
                    navigateToForgotPassword = {
                        navigation.pushNew(RootConfig.DynamicSettingsScreen("forgot_password"))
                    },
                )
            )

            RootConfig.Registration -> RootComponent.Child.RegistrationChild(
                DefaultRegistrationComponent(
                    componentContext = componentContext,
                )
            )

            is RootConfig.ContactUs -> RootComponent.Child.ContactUsChild(
                DefaultContactUsComponent(
                    selectedType = rootConfig.selectedType,
                    componentContext = componentContext,
                )
            )

            is RootConfig.Verification -> RootComponent.Child.VerificationChildMain(
                DefaultVerificationComponent(
                    settingsType = rootConfig.settingsType,
                    componentContext = componentContext,
                    owner = rootConfig.ownerId,
                    code = rootConfig.code,
                )
            )

            is RootConfig.DynamicSettingsScreen ->RootComponent.Child.DynamicSettingsChild(
                component = DefaultDynamicSettingsComponent(
                    owner = rootConfig.ownerId,
                    code = rootConfig.code,
                    settingsType = rootConfig.settingsType,
                    componentContext = componentContext,
                )
            )
        }

    companion object {
        private val navigation = StackNavigation<RootConfig>()

        val goToLogin: (reset : Boolean) -> Unit = { isReset ->
            if (!isReset) {
                navigation.pushNew(RootConfig.Login())
            }else{
                navigation.replaceAll(RootConfig.Login(true))
            }
        }

        val goToContactUs : (selectedType: String?) -> Unit = {
            navigation.pushNew(RootConfig.ContactUs(selectedType = it))
        }

        val goToVerification: (settingsType: String, ownerId: Long?,code: String?) -> Unit = { settingsType, ownerId, code ->
            navigation.pushNew(RootConfig.Verification(settingsType, ownerId, code))
        }

        val goToDynamicSettings : (settingsType: String, ownerId: Long?, code: String?) -> Unit = { settingsType, ownerId, code ->
            navigation.pushNew(RootConfig.DynamicSettingsScreen(settingsType, ownerId, code))
        }

        val goBack: () -> Unit = {
            navigation.pop()
        }

        val goToMain : () -> Unit = {
            navigation.replaceAll(RootConfig.Main)
        }
    }
}


