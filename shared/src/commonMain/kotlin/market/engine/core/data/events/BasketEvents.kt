package market.engine.core.data.events

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
