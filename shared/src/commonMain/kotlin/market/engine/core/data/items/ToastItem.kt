package market.engine.core.data.items

import market.engine.core.data.types.ToastType

data class ToastItem(
    var isVisible: Boolean,
    val message: String,
    val type: ToastType,
)
