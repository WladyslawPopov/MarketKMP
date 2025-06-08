package market.engine.fragments.root.main.basket

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem


interface BasketEvents {
    fun onSelectAll(userId: Long, allOffers: List<OfferItem?>, isChecked: Boolean)
    fun onOfferSelected(userId: Long, item: SelectedBasketItem, isChecked: Boolean)
    fun onQuantityChanged(offerId: Long, newQuantity: Int, onResult: (Int) -> Unit)
    fun onAddToFavorites(offer: OfferItem, onFinish: (Boolean) -> Unit)
    fun onDeleteOffersRequest(ids : List<Long>)
    fun onExpandClicked(userId: Long, currentOffersSize: Int)
    fun onCreateOrder(userId: Long, selectedOffers: List<SelectedBasketItem>)
    fun onGoToUser(userId: Long)
    fun onGoToOffer(offerId: Long)
}

interface BasketComponent {
    val model : Value<Model>

    val deleteIds: Value<List<Long>>

    data class Model(
        val basketViewModel: BasketViewModel,
        val backHandler: BackHandler,
        val events: BasketEvents
    )

    fun clearDeleteIds()

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

    private val basketViewModel : BasketViewModel = BasketViewModel()
    override var deleteIds = MutableValue(emptyList<Long>())

    private val _model = MutableValue(
        BasketComponent.Model(
            backHandler = backHandler,
            basketViewModel = basketViewModel,
            events = object : BasketEvents {
                override fun onOfferSelected(userId: Long, item: SelectedBasketItem, isChecked: Boolean) {
                    basketViewModel.checkSelected(userId, item, isChecked)
                }
                override fun onExpandClicked(userId: Long, currentOffersSize: Int) {
                    basketViewModel.clickExpanded(userId, currentOffersSize)
                }
                override fun onSelectAll(userId: Long, allOffers: List<OfferItem?>, isChecked: Boolean) {
                    if (isChecked) {
                        allOffers.filter { it?.safeDeal == true }.mapNotNull { it }.forEach { offer ->
                            basketViewModel.checkSelected(userId, SelectedBasketItem(
                                offerId = offer.id,
                                pricePerItem = offer.price.toDouble(),
                                selectedQuantity = offer.quantity
                            ), true)
                        }
                    } else {
                        basketViewModel.uncheckAll(userId)
                    }
                }
                override fun onQuantityChanged(offerId: Long, newQuantity: Int, onResult: (Int) -> Unit) {
                    val body = HashMap<String, JsonElement>()
                    body["offer_id"] = JsonPrimitive(offerId)
                    body["quantity"] = JsonPrimitive(newQuantity)
                    basketViewModel.addOfferToBasket(body) { onResult(newQuantity) }
                    basketViewModel.updateQuantityInState(offerId, newQuantity)
                }
                override fun onAddToFavorites(offer: OfferItem, onFinish: (Boolean) -> Unit) {
                    basketViewModel.addToFavorites(offer) {
                        offer.isWatchedByMe = it
                        onFinish(it)
                    }
                }
                override fun onDeleteOffersRequest(ids: List<Long>) {
                   deleteIds.value = ids
                }
                override fun onCreateOrder(userId: Long, selectedOffers: List<SelectedBasketItem>) {
                    goToCreateOrder(Pair(userId, selectedOffers))
                }
                override fun onGoToUser(userId: Long) {
                    goToUser(userId)
                }
                override fun onGoToOffer(offerId: Long) {
                    goToOffer(offerId)
                }
            }
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

    override fun clearDeleteIds() {
        deleteIds.value = emptyList()
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
