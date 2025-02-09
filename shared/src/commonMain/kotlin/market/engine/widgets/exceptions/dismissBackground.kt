package market.engine.widgets.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource


@Composable
fun dismissBackground() {
    Row(
        modifier = Modifier
            .background(colors.errorLayoutBackground, shape = MaterialTheme.shapes.small)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(drawables.deleteIcon),
            contentDescription = null,
            tint = colors.inactiveBottomNavIconColor,
            modifier = Modifier.size(dimens.smallIconSize)
        )

        Spacer(Modifier.width(dimens.largeSpacer))
    }
}
