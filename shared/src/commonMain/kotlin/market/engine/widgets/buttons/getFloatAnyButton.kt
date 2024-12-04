package market.engine.widgets.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun getFloatAnyButton(
    isVisible : Boolean = true,
    drawable: DrawableResource,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    AnimatedVisibility (isVisible,modifier = modifier, enter = fadeIn(), exit = fadeOut()) {
        FloatingActionButton(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            containerColor = colors.grayLayout,
            contentColor = colors.grayText,
            onClick = { onClick() },
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
                hoveredElevation = 2.dp,
                focusedElevation = 2.dp
            )
        ) {
            Icon(
                tint = colors.black,
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
