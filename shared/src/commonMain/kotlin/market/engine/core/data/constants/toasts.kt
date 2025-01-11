package market.engine.core.data.constants

import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType

val successToastItem = ToastItem(
    isVisible = true,
    type = ToastType.SUCCESS,
    message = ""
)

val errorToastItem = ToastItem(
    isVisible = true,
    type = ToastType.ERROR,
    message = ""
)

val infoToastItem = ToastItem(
    isVisible = true,
    type = ToastType.WARNING,
    message = ""
)
