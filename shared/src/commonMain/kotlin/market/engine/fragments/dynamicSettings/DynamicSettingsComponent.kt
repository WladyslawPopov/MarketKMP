package market.engine.fragments.dynamicSettings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import org.koin.mp.KoinPlatform.getKoin

interface DynamicSettingsComponent {
    val model : Value<Model>

    data class Model(
        var settingsType : String,
        val dynamicSettingsViewModel: DynamicSettingsViewModel,
    )

    fun onBack()
    fun goToVerificationPage(method : String)
}

class DefaultDynamicSettingsComponent(
    val navigateBack : () -> Unit,
    val navigateToVerification: (String) -> Unit,
    settingsType : String,
    componentContext: ComponentContext,
) : DynamicSettingsComponent, ComponentContext by componentContext
{

    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private  val dynamicSettingsViewModel : DynamicSettingsViewModel = getKoin().get()

    private val _model = MutableValue(
        DynamicSettingsComponent.Model(
            settingsType = settingsType,
            dynamicSettingsViewModel = dynamicSettingsViewModel
        )
    )

    override val model = _model

    init {
        dynamicSettingsViewModel.init(settingsType)
    }

    override fun onBack() {
        navigateBack()
    }

    override fun goToVerificationPage(method: String) {
        navigateToVerification(method)
        lifecycle.doOnResume {
            navigateBack()
        }
    }
}
