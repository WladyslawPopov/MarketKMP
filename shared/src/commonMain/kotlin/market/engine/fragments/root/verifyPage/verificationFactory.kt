package market.engine.fragments.root.verifyPage

import com.arkivanov.decompose.ComponentContext

fun verificationFactory(
    componentContext: ComponentContext,
    settingsType : String,
    owner : Long?,
    code : String?,
    navigateBack: () -> Unit,
    navigateLogin: () -> Unit,
    ): VerificationComponent {
        return DefaultVerificationComponent(
            settingsType = settingsType,
            componentContext = componentContext,
            navigateBack = {
                navigateBack()
            },
            owner = owner,
            code = code,
            navigateLogin = {
                navigateLogin()
            }
        )
    }
