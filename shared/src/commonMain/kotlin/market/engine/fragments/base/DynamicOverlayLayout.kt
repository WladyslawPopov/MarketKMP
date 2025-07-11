package market.engine.fragments.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private enum class OverlaySlot { TopBar, BottomBar, Content }

@Composable
fun DynamicOverlayLayout(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (topPadding: Dp, bottomPadding: Dp) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val topBarPlaceable = subcompose(OverlaySlot.TopBar, topBar).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val topBarHeight = topBarPlaceable.maxOfOrNull { it.height }?.toDp() ?: 0.dp

        val bottomBarPlaceable = subcompose(OverlaySlot.BottomBar, bottomBar).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }
        val bottomBarHeight = bottomBarPlaceable.maxOfOrNull { it.height }?.toDp() ?: 0.dp

        val contentPlaceable = subcompose(OverlaySlot.Content) {
            content(topBarHeight, bottomBarHeight)
        }.map {
            it.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.forEach { it.placeRelative(0, 0) }

            topBarPlaceable.forEach { it.placeRelative(0, 0) }

            val bottomBarY = constraints.maxHeight - (bottomBarHeight.roundToPx())
            bottomBarPlaceable.forEach { it.placeRelative(0, bottomBarY) }
        }
    }
}
