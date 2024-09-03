package market.engine.common

import androidx.compose.runtime.Composable

expect class ScreenSizeProvider() {
    @Composable
    fun getScreenWidthDp(): Float
    @Composable
    fun getScreenHeightDp(): Float
}
