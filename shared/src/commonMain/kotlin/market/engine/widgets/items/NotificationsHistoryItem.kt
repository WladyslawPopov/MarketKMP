package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NotificationItem
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getIconByType
import market.engine.widgets.badges.getBadge
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotificationsHistoryItem(
    item: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(item.getIconByType()),
                contentDescription = null,
                tint = colors.steelBlue,
                modifier = Modifier.size(dimens.smallIconSize)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.title,
                    color = colors.black,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    item.body,
                    color = colors.black,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    item.timeCreated.toString().convertDateWithMinutes(),
                    color = colors.steelBlue,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            if (item.unreadCount > 0) {
                getBadge(item.unreadCount, false)
            }
        }
    }
}
