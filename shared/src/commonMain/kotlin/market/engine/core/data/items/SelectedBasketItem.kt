package market.engine.core.data.items

import kotlinx.serialization.Serializable

@Serializable
data class SelectedBasketItem(
    val offerId: Long,
    val pricePerItem: Double,
    var selectedQuantity: Int = 0
)
