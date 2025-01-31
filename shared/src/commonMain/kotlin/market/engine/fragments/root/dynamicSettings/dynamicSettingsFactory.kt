package market.engine.fragments.root.dynamicSettings

import com.arkivanov.decompose.ComponentContext

fun dynamicSettingsFactory(
    componentContext: ComponentContext,
    owner : Long?,
    code : String?,
    settingsType : String,
    navigateBack: () -> Unit,
    navigateToVerification: (String) -> Unit,
    ): DynamicSettingsComponent {
        return DefaultDynamicSettingsComponent(
            owner = owner,
            code = code,
            settingsType = settingsType,
            componentContext = componentContext,
            navigateBack = {
                navigateBack()
            },
            navigateToVerification = {
                navigateToVerification(it)
            }
        )
    }
