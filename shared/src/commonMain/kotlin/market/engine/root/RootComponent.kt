package market.engine.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import market.engine.core.navigation.configs.RootConfig
import market.engine.presentation.login.DefaultLoginComponent
import market.engine.presentation.login.LoginComponent
import market.engine.presentation.main.DefaultMainComponent
import market.engine.presentation.main.MainComponent

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
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

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
        navigation.push(RootConfig.Login)
    }

    private fun createChild(rootConfig: RootConfig, componentContext: ComponentContext): RootComponent.Child =
        when (rootConfig) {
            RootConfig.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext,
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


