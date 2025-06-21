package market.engine.core.data.events

import market.engine.core.data.items.MenuItem
import market.engine.core.network.networkObjects.Offer

interface OrderItemEvents {
    fun onUpdateItem()
    fun onGoToUser(id : Long)
    fun onGoToOffer(offer: Offer)
    fun sendMessage()
    fun openOrderDetails()
    fun getOperations(onGetOperations: (List<MenuItem>) -> Unit)
    fun copyTrackId()
    fun copyOrderId()
}
