package market.engine.widgets.badges

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.items.NavigationItem
import market.engine.widgets.exceptions.LoadImage
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
            if (item.badgeCount != null) {
                Badge{
                    Text(text = item.badgeCount.toString(), fontSize = 9.sp)
                }
            } else {
                if (item.hasNews) {
                    Badge()
                }
            }
        }
    ) {
        if (item.image != null){
            Box(
                modifier = Modifier.clip(CircleShape).wrapContentSize(),
            ){
                LoadImage(
                    url = item.image ?: "",
                    size = dimens.mediumIconSize
                )
            }
        }else {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = item.title,
                tint = if (!selected) item.tint else item.tintSelected,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
