package market.engine.fragments.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.items.DeepLink
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.root.contactUs.ContactUsComponent
import market.engine.fragments.root.contactUs.DefaultContactUsComponent
import market.engine.fragments.root.dynamicSettings.DynamicSettingsComponent
import market.engine.fragments.root.dynamicSettings.dynamicSettingsFactory
import market.engine.fragments.root.login.DefaultLoginComponent
import market.engine.fragments.root.login.LoginComponent
import market.engine.fragments.root.main.DefaultMainComponent
import market.engine.fragments.root.main.MainComponent
import market.engine.fragments.root.registration.DefaultRegistrationComponent
import market.engine.fragments.root.registration.RegistrationComponent
import market.engine.fragments.root.verifyPage.VerificationComponent
import market.engine.fragments.root.verifyPage.verificationFactory
import org.koin.mp.KoinPlatform.getKoin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class MainChild(val component: MainComponent) : Child()
        data class LoginChild(val component: LoginComponent) : Child()
        data class RegistrationChild(val component: RegistrationComponent) : Child()
        data class ContactUsChild(val component: ContactUsComponent) : Child()
        data class VerificationChildMain(val component: VerificationComponent) : Child()
        data class DynamicSettingsChild(val component: DynamicSettingsComponent) : Child()
    }

    fun backToMain()
    fun navigateToLogin()
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val deepLink: DeepLink?
) : RootComponent, ComponentContext by componentContext {

    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()
    private val settingsHelper : SettingsRepository = getKoin().get()

    private val navigation = StackNavigation<RootConfig>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> by lazy {
        childStack(
            source = navigation,
            serializer = RootConfig.serializer(),
            initialConfiguration = RootConfig.Main,
            handleBackButton = true,
            childFactory = ::createChild
        )
    }

    override fun backToMain() {
        navigation.pop()
    }

    override fun navigateToLogin() {
        navigation.pushNew(RootConfig.Login)
    }

    val userRepository = getKoin().get<UserRepository>()

    init {
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


        val isShowReview = settingsHelper.getSettingValue("isShowReview", false) ?: false
        if (countLaunch > 10 && !isShowReview){
            //check review
        }
    }

    private fun createChild(rootConfig: RootConfig, componentContext: ComponentContext): RootComponent.Child =
        when (rootConfig) {
            RootConfig.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext,
                    deepLink = deepLink,
                    goToLoginSelected =::navigateToLogin,
                    contactUsSelected = {
                        navigation.pushNew(RootConfig.ContactUs)
                    },
                    navigateToDynamicSettings = { settingsType, ownerId, code ->
                       navigation.pushNew(RootConfig.DynamicSettingsScreen(settingsType, ownerId, code))
                    },
                    navigateToVerification = { settingsType, ownerId, code ->
                        navigation.pushNew(RootConfig.Verification(settingsType, ownerId, code))
                    }
                )
            )
            RootConfig.Login -> RootComponent.Child.LoginChild(
                DefaultLoginComponent(
                    componentContext,
                    navigateToRegistration = {
                        navigation.pushNew(RootConfig.Registration)
                    },
                    navigateToForgotPassword = {
                        navigation.pushNew(RootConfig.DynamicSettingsScreen("forgot_password"))
                    },
                    ::backToMain
                )
            )

            RootConfig.Registration -> RootComponent.Child.RegistrationChild(
                DefaultRegistrationComponent(
                    componentContext = componentContext,
                    onBackSelected = ::backToMain
                )
            )

            RootConfig.ContactUs -> RootComponent.Child.ContactUsChild(
                DefaultContactUsComponent(
                    componentContext = componentContext,
                    onBackSelected = ::backToMain
                )
            )

            is RootConfig.Verification -> RootComponent.Child.VerificationChildMain(
                verificationFactory(
                    componentContext = componentContext,
                    owner = rootConfig.ownerId,
                    code = rootConfig.code,
                    settingsType = rootConfig.settingsType,
                    navigateBack = ::backToMain,
                    navigateLogin = ::navigateToLogin
                )
            )

            is RootConfig.DynamicSettingsScreen ->RootComponent.Child.DynamicSettingsChild(
                component = dynamicSettingsFactory(
                    componentContext,
                    owner = rootConfig.ownerId,
                    code = rootConfig.code,
                    settingsType = rootConfig.settingsType,
                    navigateBack = ::backToMain,
                    navigateToVerification = {
                       navigation.pushNew(RootConfig.Verification(it))
                    }
                )
            )
        }
}


