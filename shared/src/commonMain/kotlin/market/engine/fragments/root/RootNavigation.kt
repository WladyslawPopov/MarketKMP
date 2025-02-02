package market.engine.fragments.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import market.engine.fragments.root.contactUs.ContactUsContent
import market.engine.fragments.root.dynamicSettings.DynamicSettingsContent
import market.engine.fragments.root.login.LoginContent
import market.engine.fragments.root.main.MainNavigation
import market.engine.fragments.root.registration.RegistrationContent
import market.engine.fragments.root.verifyPage.VerificationContent
import market.engine.widgets.exceptions.BackHandler

@Composable
fun RootNavigation(
    component: RootComponent,
    modifier: Modifier = Modifier
) {
    val childStack by component.childStack.subscribeAsState()

    BackHandler(component.model.value.backHandler){

    }

    Surface(
        modifier = modifier.fillMaxSize().imePadding(),
    ) {
        Children(
            stack = childStack,
            modifier = modifier.fillMaxSize()
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.MainChild -> {
                    MainNavigation(instance.component)
                }

                is RootComponent.Child.LoginChild -> {
                    LoginContent(instance.component)
                }

                is RootComponent.Child.RegistrationChild -> {
                    RegistrationContent(instance.component)
                }

                is RootComponent.Child.ContactUsChild -> {
                    ContactUsContent(instance.component)
                }
                is RootComponent.Child.VerificationChildMain -> {
                    VerificationContent(instance.component)
                }

                is RootComponent.Child.DynamicSettingsChild -> {
                    DynamicSettingsContent(instance.component)
                }
            }
        }
    }
}

@Serializable
sealed class RootConfig {
    @Serializable
    data object Main : RootConfig()

    @Serializable
    data object Login : RootConfig()

    @Serializable
    data object Registration : RootConfig()

    @Serializable
    data object ContactUs : RootConfig()

    @Serializable
    data class Verification(val settingsType : String, val ownerId: Long? = null, val code: String? = null) : RootConfig()

    @Serializable
    data class DynamicSettingsScreen(val settingsType : String, val ownerId: Long? = null, val code: String? = null) : RootConfig()
}
