package market.engine.widgets.badges

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun BadgedButton(
    item: NavigationItem,
    selected: Boolean = false
) {
    BadgedBox(
        badge = {
            getBadge(item.badgeCount, item.hasNews)
        }
    ) {
        if (item.imageString != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { item.onClick() },
                contentAlignment = Alignment.Center
            ) {
                LoadImage(
                    url = item.imageString,
                    isShowLoading = false,
                    isShowEmpty = false,
                    size = dimens.mediumIconSize,
                    contentScale = ContentScale.FillBounds
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { item.onClick() }
                    .size(dimens.mediumIconSize + dimens.smallSpacer),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        painter = painterResource(item.icon!!),
                        contentDescription = item.title,
                        tint = if (!selected) item.tint else item.tintSelected,
                        modifier = Modifier.size(dimens.smallIconSize)
                    )
                }
                if (item.image != null) {
                    Image(
                        painter = painterResource(item.image!!),
                        contentDescription = item.title,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(dimens.mediumIconSize)
                    )
                }
            }
        }
    }
}
