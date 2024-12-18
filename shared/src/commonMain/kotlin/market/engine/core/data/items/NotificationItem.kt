package market.engine.core.data.items

data class NotificationItem (
    var id: Long,
    var title: String,
    var body: String,
    var data: String,
    var type: String,
    var timeCreated: Long,
    var isRead: Boolean
    )
