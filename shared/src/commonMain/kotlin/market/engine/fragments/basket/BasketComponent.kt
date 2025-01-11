package market.engine.fragments.basket

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.common.AnalyticsFactory
import org.koin.mp.KoinPlatform.getKoin


interface BasketComponent {
    val model : Value<Model>

    data class Model(
        val basketViewModel: BasketViewModel
    )
}

class DefaultBasketComponent(
    componentContext: ComponentContext,
) : BasketComponent, ComponentContext by componentContext {

    private val basketViewModel : BasketViewModel = getKoin().get()

    private val _model = MutableValue(
        BasketComponent.Model(
            basketViewModel = basketViewModel
        )
    )
    override val model = _model

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        analyticsHelper.reportEvent("view_cart", mapOf())

        basketViewModel.getUserCart()
    }
}
