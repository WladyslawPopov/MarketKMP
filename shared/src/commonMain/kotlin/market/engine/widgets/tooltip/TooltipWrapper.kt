package market.engine.widgets.tooltip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun TooltipWrapper(
    modifier: Modifier = Modifier,
    tooltipState: TooltipState = rememberTooltipState(),
    content: @Composable BoxScope.(tooltipState: TooltipState) -> Unit,
    onClick: MutableState<() -> Unit>
) {
    Box(
        modifier = modifier
            .clipToBounds()
            .onGloballyPositioned { tooltipState.changeTooltipWrapperLayoutCoordinates(it) },
    ) {
        content(tooltipState)

        Tooltip(state = tooltipState, onClick = onClick)
    }
}
