package market.engine.core.items

import market.engine.core.types.ToastType

data class ToastItem(
    var isVisible: Boolean,
    val message: String,
    val type: ToastType,
)
