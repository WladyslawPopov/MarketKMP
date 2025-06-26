package market.engine.core.data.states

import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.Fields

data class DeliveryCardsState(
    val deliveryCards: List<DeliveryAddress> = emptyList(),
    val deliveryFields: List<Fields> = emptyList(),
    val showFields: Boolean = false,
    val selectedCard: Long? = null,
    val selectedCountry: Int = 0
)
