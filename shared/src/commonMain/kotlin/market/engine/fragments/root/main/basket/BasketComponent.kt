package market.engine.fragments.root.main.basket

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.items.SelectedBasketItem

interface BasketComponent {
    val model : Value<Model>

    data class Model(
        val basketViewModel: BasketViewModel,
        val backHandler: BackHandler,
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

    private val basketViewModel : BasketViewModel = BasketViewModel(this)

    private val _model = MutableValue(
        BasketComponent.Model(
            backHandler = backHandler,
            basketViewModel = basketViewModel,
        )
    )
    override val model = _model

    private val analyticsHelper = basketViewModel.analyticsHelper

    init {
        analyticsHelper.reportEvent("view_cart", mapOf())

        lifecycle.doOnResume {
            basketViewModel.updateUserInfo()
            basketViewModel.getUserCart()
        }
    }

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
        val eventParameters = mapOf(
            "seller_id" to basketItem.first.toString(),
            "lot_count" to basketItem.second.size.toString()
        )
        analyticsHelper.reportEvent("click_checkout", eventParameters)
        navigateToCreateOrder(basketItem)
    }
}
