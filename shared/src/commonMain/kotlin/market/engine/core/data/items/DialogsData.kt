package market.engine.core.data.items

import market.engine.core.data.events.DialogItemEvents
import market.engine.core.network.networkObjects.MesImage
import market.engine.core.data.types.MessageType

sealed class DialogsData {
    data class MessageItem (
        var id: Long,
        var message: String,
        var dateTime: Long,
        var user: String,
        var messageType: MessageType,
        var images: ArrayList<MesImage>?,
        var readByReceiver: Boolean,
        val options: List<MenuItem> = emptyList(),
        val events : DialogItemEvents
    ) : DialogsData()

    data class SeparatorItem (
        var dateTime: String,
    ) : DialogsData()
}


