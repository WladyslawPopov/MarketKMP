package market.engine.fragments.verifyPage

import com.arkivanov.decompose.ComponentContext

fun verificationFactory(
    componentContext: ComponentContext,
    settingsType : String,
    navigateBack: () -> Unit,
    ): VerificationComponent {
        return DefaultVerificationComponent(
            settingsType = settingsType,
            componentContext = componentContext,
            navigateBack = {
                navigateBack()
            },
        )
    }
