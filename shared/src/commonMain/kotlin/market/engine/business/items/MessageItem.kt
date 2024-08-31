package application.market.auction_mobile.business.items

import application.market.auction_mobile.business.networkObjects.MesImage
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
