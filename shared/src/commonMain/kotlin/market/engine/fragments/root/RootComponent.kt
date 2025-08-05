package market.engine.fragments.root

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
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
import market.engine.core.data.items.DeepLink
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

@OptIn(ExperimentalDecomposeApi::class)
class DefaultRootComponent(
    componentContext: JetpackComponentContext,
) : RootComponent, JetpackComponentContext by componentContext {

    val viewModel =  viewModel("rootViewModel") {
        RootVewModel(this@DefaultRootComponent, createSavedStateHandle())
    }

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
        viewModel.goToDeepLink(url)
    }

    override fun updateOrientation(orientation: Int) {
        try {
            val component = (childStack.active.instance as? RootComponent.Child.MainChild)?.component
            component?.model?.value?.viewModel?.updateOrientation(orientation)
        } catch (e: Exception) {
            println("Ignoring orientation update during navigation: ${e.message}")
        }

        println("upd orientation: $orientation")
    }

    private fun createChild(rootConfig: RootConfig, componentContext: JetpackComponentContext): RootComponent.Child =
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

        val goToLogin: () -> Unit = {
            navigation.pushNew(RootConfig.Login())
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


