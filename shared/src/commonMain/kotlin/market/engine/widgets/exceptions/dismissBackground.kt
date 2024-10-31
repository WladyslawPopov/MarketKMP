package market.engine.widgets.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource


@Composable
fun dismissBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.errorLayoutBackground)
            .padding(start = dimens.largePadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            painterResource(drawables.cancelIcon),
            contentDescription = null,
            tint = colors.inactiveBottomNavIconColor,
        )
    }

}
