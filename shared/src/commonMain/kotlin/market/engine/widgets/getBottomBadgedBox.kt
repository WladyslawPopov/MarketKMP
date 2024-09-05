package market.engine.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.root.BottomNavigationItem
import org.jetbrains.compose.resources.painterResource

@Composable
fun getBottomBadgedBox(
    selectedItemIndex: Int,
    index: Int,
    item: BottomNavigationItem
) {
    BadgedBox(
        badge = {
            if (item.badgeCount != null){
                Badge {
                    Text(text = item.badgeCount.toString())
                }
            } else {
                if (item.hasNews) {
                    Badge()
                }
            }
        }
    ){
        if (selectedItemIndex == index){
            Icon(
                painter = painterResource(item.selectedIcon),
                contentDescription = item.title,
                tint = colors.inactiveBottomNavIconColor,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        } else {
            Icon(
                painter = painterResource(item.unselectedIcon),
                contentDescription = null,
                tint = colors.black,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
