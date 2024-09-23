package application.market.agora.business.items

import application.market.agora.business.networkObjects.MesImage
import application.market.data.types.MessageType


data class MessageItem (
    var id: Long,
    var message: String,
    var dateTime: Long,
    var user: String,
    var messageType: MessageType,
    var images: ArrayList<MesImage>?,
    var readByReceiver: Boolean,
)
