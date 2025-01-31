package market.engine.fragments.root.registration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.common.AnalyticsFactory
import org.koin.mp.KoinPlatform.getKoin

interface RegistrationComponent {
    val model: Value<Model>

    data class Model(
        val regViewModel: RegViewModel,
    )

    fun onBack()
}

class DefaultRegistrationComponent(
    componentContext: ComponentContext,
    private val onBackSelected: () -> Unit
) : RegistrationComponent, ComponentContext by componentContext {

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private val regViewModel = getKoin().get<RegViewModel>()

    private val _model = MutableValue(
        RegistrationComponent.Model(
            regViewModel = regViewModel
        )
    )

    override val model = _model

    init {
        regViewModel.getRegFields()
        analyticsHelper.reportEvent("view_register_account", mapOf())
    }

    override fun onBack() {
        onBackSelected()
    }
}
