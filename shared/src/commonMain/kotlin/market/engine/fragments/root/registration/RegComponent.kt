package market.engine.fragments.root.registration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.common.AnalyticsFactory
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack

interface RegistrationComponent {
    val model: Value<Model>

    data class Model(
        val regViewModel: RegViewModel,
        val backHandler: BackHandler
    )

    fun onBack()
}

class DefaultRegistrationComponent(
    componentContext: ComponentContext
) : RegistrationComponent, ComponentContext by componentContext {

    private val regViewModel = RegViewModel(this)

    private val _model = MutableValue(
        RegistrationComponent.Model(
            regViewModel = regViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    override fun onBack() {
        goBack()
    }
}
