package market.engine.core.data.items

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem (
    val id: String,
    val title: String,
    val body: String,
    val data: String,
    val type: String,
    val timeCreated: Long,
    val unreadCount: Int,
    val unreadIds: List<String>,
    val isRead: Boolean,
)
