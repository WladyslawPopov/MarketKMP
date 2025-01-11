package market.engine.fragments.basket

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import org.koin.mp.KoinPlatform.getKoin


interface BasketComponent {
    val model : Value<Model>

    data class Model(
        val basketViewModel: BasketViewModel
    )

    fun goToListing()
}

class DefaultBasketComponent(
    componentContext: ComponentContext,
    val navigateToListing: () -> Unit
) : BasketComponent, ComponentContext by componentContext {

    private val basketViewModel : BasketViewModel = getKoin().get()

    private val _model = MutableValue(
        BasketComponent.Model(
            basketViewModel = basketViewModel
        )
    )
    override val model = _model

    override fun goToListing() {
        navigateToListing()
    }

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            analyticsHelper.reportEvent("view_cart", mapOf())

            basketViewModel.getUserCart()
        }
    }
}
