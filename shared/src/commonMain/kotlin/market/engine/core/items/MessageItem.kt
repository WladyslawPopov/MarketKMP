package market.engine.core.items

import market.engine.core.networkObjects.MesImage
import market.engine.core.types.MessageType


data class MessageItem (
    var id: Long,
    var message: String,
    var dateTime: Long,
    var user: String,
    var messageType: MessageType,
    var images: ArrayList<MesImage>?,
    var readByReceiver: Boolean,
)
