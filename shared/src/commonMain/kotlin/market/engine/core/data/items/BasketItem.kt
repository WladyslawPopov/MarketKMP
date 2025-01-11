package market.engine.core.data.items

import market.engine.core.network.networkObjects.UserBody

data class BasketItem(
    var isChecked: Boolean,
    var offerId: Long?,
    val data: UserBody
)
