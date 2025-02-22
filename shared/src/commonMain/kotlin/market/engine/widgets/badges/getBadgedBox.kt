package market.engine.widgets.badges

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun getBadgedBox(
    modifier: Modifier = Modifier,
    item: NavigationItem,
    selected: Boolean = false
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            getBadge(item.badgeCount, item.hasNews)
        }
    ) {
        if (item.imageString != null){
            Box(
                modifier = Modifier.clip(CircleShape).wrapContentSize(),
            ){
                LoadImage(
                    url = item.imageString,
                    isShowLoading = false,
                    isShowEmpty = false,
                    size = dimens.mediumIconSize,
                    contentScale = ContentScale.FillBounds
                )
            }
        }else {
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
