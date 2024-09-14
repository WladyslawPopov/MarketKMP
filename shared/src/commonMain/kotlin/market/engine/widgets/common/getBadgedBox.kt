package market.engine.widgets.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.items.NavigationItem
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
                Badge(
                    containerColor = colors.badgeColor,
                    contentColor = colors.accentColor
                ) {
                    val dynamicFontSize = (10 - (item.badgeCount / 10)).coerceAtLeast(7).sp
                    Text(text = item.badgeCount.toString(), fontSize = dynamicFontSize)
                }
            } else {
                if (item.hasNews) {
                    Badge()
                }
            }
        }
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = item.title,
            tint = if (!selected) item.tint else item.tintSelected,
            modifier = Modifier.size(dimens.smallIconSize)
        )
    }
}
