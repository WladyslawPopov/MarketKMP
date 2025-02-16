package market.engine.widgets.badges

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
        if (item.image != null){
            Box(
                modifier = Modifier.clip(CircleShape).wrapContentSize(),
            ){
                LoadImage(
                    url = item.image ?: "",
                    isShowLoading = false,
                    isShowEmpty = false,
                    size = dimens.mediumIconSize
                )
            }
        }else {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = stringResource(item.title),
                tint = if (!selected) item.tint else item.tintSelected,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
