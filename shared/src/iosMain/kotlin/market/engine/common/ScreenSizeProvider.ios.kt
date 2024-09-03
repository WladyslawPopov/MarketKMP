package market.engine.common

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

actual class ScreenSizeProvider actual constructor() {
    @Composable
    @OptIn(ExperimentalForeignApi::class)
    actual fun getScreenWidthDp(): Float {
        val screenWidth = UIScreen.mainScreen().bounds.useContents { size.width }
        val scale = UIScreen.mainScreen.scale
        return (screenWidth / scale).toFloat()
    }

    @Composable
    @OptIn(ExperimentalForeignApi::class)
    actual fun getScreenHeightDp(): Float {
        val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }
        val scale = UIScreen.mainScreen.scale
        return (screenHeight / scale).toFloat()
    }
}
