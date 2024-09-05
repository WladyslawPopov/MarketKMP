package market.engine.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.business.constants.ThemeResources.dimens
import market.engine.root.NavigationItem
import org.jetbrains.compose.resources.painterResource

@Composable
fun getBadgedBox(
    item: NavigationItem
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
        Icon(
            painter = painterResource(item.icon),
            contentDescription = item.title,
            tint = item.tint,
            modifier = Modifier.size(dimens.smallIconSize)
        )
    }
}
