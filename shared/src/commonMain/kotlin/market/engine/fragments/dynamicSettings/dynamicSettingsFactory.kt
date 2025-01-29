package market.engine.fragments.dynamicSettings

import com.arkivanov.decompose.ComponentContext

fun dynamicSettingsFactory(
    componentContext: ComponentContext,
    settingsType : String,
    navigateBack: () -> Unit,
    ): DynamicSettingsComponent {
        return DefaultDynamicSettingsComponent(
            settingsType = settingsType,
            componentContext = componentContext,
            navigateBack = {
                navigateBack()
            },
        )
    }
