package market.engine.widgets.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dismissBackground(
    dismissState : SwipeToDismissBoxState
) {
    val direction = dismissState.dismissDirection

    val color = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> colors.transparent
        SwipeToDismissBoxValue.Settled -> colors.transparent
        SwipeToDismissBoxValue.EndToStart -> colors.errorLayoutBackground
        else -> colors.transparent
    }
    if (direction == SwipeToDismissBoxValue.EndToStart) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
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
}
