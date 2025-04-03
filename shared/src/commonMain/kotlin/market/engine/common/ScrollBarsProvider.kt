package market.engine.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class ScrollBarsProvider() {
    @Composable
    fun getVerticalScrollbar(scrollState : Any, modifier : Modifier, isReversed : Boolean = false)
    @Composable
    fun getHorizontalScrollbar(scrollState : Any, modifier : Modifier, isReversed : Boolean = false)
}
