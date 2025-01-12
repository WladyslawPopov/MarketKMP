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

    fun goToOffer(offerId: Long)

    fun goToUser(userId: Long)
}

class DefaultBasketComponent(
    componentContext: ComponentContext,
    val navigateToListing: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit
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

    override fun goToOffer(offerId: Long) {
        navigateToOffer(offerId)
    }

    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        analyticsHelper.reportEvent("view_cart", mapOf())

        basketViewModel.getUserCart()
    }
}
