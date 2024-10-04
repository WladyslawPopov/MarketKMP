package market.engine.core.util

import androidx.compose.runtime.Composable
import market.engine.core.types.WindowSizeClass
import market.engine.common.ScreenSizeProvider

@Composable
fun getWindowSizeClass(): WindowSizeClass {
    val width = ScreenSizeProvider().getScreenWidthDp()
    val height = ScreenSizeProvider().getScreenHeightDp()

    return when {
        width < 600 || height < 600 -> WindowSizeClass.Compact
        width >= 600 && width < 840 && height >= 600 -> WindowSizeClass.Compact
        width >= 840 && height >= 600 -> WindowSizeClass.Big
        else -> WindowSizeClass.Compact
    }
}
