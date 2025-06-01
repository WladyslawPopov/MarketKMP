package market.engine.widgets.tooltip

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned

@Stable
fun Modifier.tooltip(
   state: TooltipState,
   data: TooltipData?,
   initialVisibility : Boolean = true
) : Modifier = composed {

    LaunchedEffect(Unit) {
        if (data != null)
        state.initialize(
            data = data,
            initialVisibility = initialVisibility,
        )
    }

    this.onGloballyPositioned {
        state.changeAnchorLayoutCoordinates(layoutCoordinates = it)
    }
}
