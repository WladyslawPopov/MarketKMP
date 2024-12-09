package market.engine.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.globalData.SAPI
import market.engine.core.items.DeepLink
import market.engine.core.repositories.SettingsRepository
import market.engine.presentation.login.DefaultLoginComponent
import market.engine.presentation.login.LoginComponent
import market.engine.core.navigation.main.DefaultMainComponent
import market.engine.core.navigation.main.MainComponent
import org.koin.mp.KoinPlatform.getKoin

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class MainChild(val component: MainComponent) : Child()
        class LoginChild(val component: LoginComponent) : Child()
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

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = RootConfig.serializer(),
            initialConfiguration = RootConfig.Main,
            handleBackButton = true,
            childFactory = ::createChild
        )

    override fun backToMain() {
        navigation.pop()
    }

    override fun navigateToLogin() {
        navigation.pushNew(RootConfig.Login)
    }

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
    }

    private fun createChild(rootConfig: RootConfig, componentContext: ComponentContext): RootComponent.Child =
        when (rootConfig) {
            RootConfig.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext,
                    deepLink = deepLink,
                    ::navigateToLogin
                )
            )
            RootConfig.Login -> RootComponent.Child.LoginChild(
                DefaultLoginComponent(
                    componentContext,
                    ::backToMain
                )
            )
        }
}


