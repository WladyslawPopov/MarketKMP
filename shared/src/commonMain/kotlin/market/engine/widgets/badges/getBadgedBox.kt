package market.engine.widgets.badges

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
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
            Card{
                LoadImage(
                    url = item.image ?: "",
                    size = 30.dp
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
