package market.engine.presentation.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
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

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Main,
            handleBackButton = true,
            childFactory = ::createChild
        )

    override fun backToMain() {
        navigation.pop()
    }

    override fun navigateToLogin() {
        navigation.push(Config.Login)
    }

    private fun createChild(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            Config.Main -> RootComponent.Child.MainChild(
                DefaultMainComponent(
                    componentContext,
                    ::navigateToLogin
                )
            )
            Config.Login -> RootComponent.Child.LoginChild(
                DefaultLoginComponent(
                    componentContext,
                    ::backToMain
                )
            )
        }
}

@Serializable
sealed class Config {
    @Serializable
    data object Main : Config()

    @Serializable
    data object Login : Config()
}
