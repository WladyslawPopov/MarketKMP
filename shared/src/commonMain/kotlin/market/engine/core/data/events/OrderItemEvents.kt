package market.engine.core.data.events

import market.engine.core.network.networkObjects.Offer

interface OrderItemEvents {
    fun onGoToUser(id : Long)
    fun onGoToOffer(offer: Offer)
    fun goToDialog(dialogId: Long?)
}
