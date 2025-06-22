package market.engine.core.data.states

import market.engine.core.data.items.PhotoTemp

data class MessengerBarState(
    val messageTextState: String = "",
    val imagesUpload: List<PhotoTemp> = emptyList(),
    val isDisabledSendMes: Boolean = false,
    val isDisabledAddPhotos: Boolean = false,
)
