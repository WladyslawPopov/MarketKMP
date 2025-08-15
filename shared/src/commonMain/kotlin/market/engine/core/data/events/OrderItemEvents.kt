package market.engine.core.data.events

interface OrderItemEvents {
    fun onGoToUser(id : Long)
    fun onGoToOffer(id: Long)
    fun goToDialog(dialogId: Long?)
}
