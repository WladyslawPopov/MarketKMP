package market.engine.core.data.items

data class SelectedBasketItem(
    val offerId: Long,
    val pricePerItem: Double,
    var selectedQuantity: Int = 0
)
