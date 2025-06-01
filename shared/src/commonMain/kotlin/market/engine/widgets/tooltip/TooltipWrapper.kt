package market.engine.widgets.tooltip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun TooltipWrapper(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(tooltipState: TooltipState) -> Unit,
) {
    val tooltipState = rememberTooltipState()

    Box(
        modifier = modifier
            .clipToBounds()
            // отправляем информацию о лейауте в стейт
            .onGloballyPositioned { tooltipState.changeTooltipWrapperLayoutCoordinates(it) },
    ) {
        content(tooltipState)

        Tooltip(state = tooltipState)
    }
}
