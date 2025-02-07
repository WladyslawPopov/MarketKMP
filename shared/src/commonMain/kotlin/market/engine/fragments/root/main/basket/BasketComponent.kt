package market.engine.fragments.root.main.basket

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.items.SelectedBasketItem
import org.koin.mp.KoinPlatform.getKoin


interface BasketComponent {
    val model : Value<Model>

    data class Model(
        val basketViewModel: BasketViewModel
    )

    fun goToListing()

    fun goToOffer(offerId: Long)

    fun goToUser(userId: Long)

    fun goToCreateOrder(basketItem : Pair<Long, List<SelectedBasketItem>>)
}

class DefaultBasketComponent(
    componentContext: ComponentContext,
    val navigateToListing: () -> Unit,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToCreateOrder: (Pair<Long, List<SelectedBasketItem>>) -> Unit,
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

    override fun goToCreateOrder(basketItem : Pair<Long, List<SelectedBasketItem>>) {
        navigateToCreateOrder(basketItem)
    }

    private val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    init {
        analyticsHelper.reportEvent("view_cart", mapOf())

        lifecycle.doOnResume {
            basketViewModel.updateUserInfo()
        }

        basketViewModel.getUserCart()
    }
}
