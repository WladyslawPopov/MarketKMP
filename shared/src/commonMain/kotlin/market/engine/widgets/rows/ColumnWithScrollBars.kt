package market.engine.widgets.rows

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.ScrollBarsProvider

@Composable
fun ColumnWithScrollBars(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = ScrollState(0),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            content = content
        )

        ScrollBarsProvider().getVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd),
            isReversed = false
        )
    }
}
