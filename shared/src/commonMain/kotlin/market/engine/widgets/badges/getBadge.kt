package market.engine.widgets.badges

import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens


@Composable
fun getBadge(badgeCount: Int?, hasNews: Boolean, color: Color = colors.negativeRed) {
    if (badgeCount != null) {
        Badge(
            containerColor = color,
            contentColor = colors.alwaysWhite
        ){
            Text(
                text = badgeCount.toString(),
                fontSize = dimens.smallText,
                color = colors.alwaysWhite,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        if (hasNews) {
            Badge(
                containerColor = color
            )
        }
    }
}
