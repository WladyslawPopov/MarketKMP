package market.engine.core.data.states

import market.engine.core.data.items.PhotoSave

data class MessengerBarState(
    val messageTextState: String = "",
    val imagesUpload: List<PhotoSave> = emptyList(),
    val isDisabledSendMes: Boolean = false,
    val isDisabledAddPhotos: Boolean = false,
)
