package market.engine.core.data.items

data class NotificationItem (
    var id: String,
    var title: String,
    var body: String,
    var data: String,
    var type: String,
    var timeCreated: Long,
    var unreadCount: Int,
    var unreadIds: List<String>,
    var isRead: Boolean,
)
