package market.engine.common


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual class ScrollBarsProvider {

    @Composable
    actual fun getVerticalScrollbar(
        scrollState: Any,
        modifier: Modifier,
        isReversed: Boolean
    ) {
    }

    @Composable
    actual fun getHorizontalScrollbar(
        scrollState: Any,
        modifier: Modifier,
        isReversed: Boolean
    ) {
    }
}
