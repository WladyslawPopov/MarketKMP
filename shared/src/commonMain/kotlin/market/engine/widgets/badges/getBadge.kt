package market.engine.widgets.badges

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens


@Composable
fun getBadge(badgeCount: Int?, hasNews: Boolean, color: Color = colors.negativeRed) {
    if (badgeCount != null) {
        Badge(
            modifier = Modifier.padding(dimens.extraSmallPadding),
            containerColor = color,
            contentColor = colors.alwaysWhite
        ){
            Text(
                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                fontSize = dimens.smallText,
                color = colors.alwaysWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
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
