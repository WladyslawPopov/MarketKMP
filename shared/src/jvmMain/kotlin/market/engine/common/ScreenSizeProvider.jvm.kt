package market.engine.common

import androidx.compose.runtime.Composable
import java.awt.Toolkit

actual class ScreenSizeProvider actual constructor() {
    @Composable
    actual fun getScreenWidthDp(): Float {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val density = 1.0
        return (screenSize.width / density).toFloat()
    }
    @Composable
    actual fun getScreenHeightDp(): Float {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val density = 1.0
        return (screenSize.height / density).toFloat()
    }
}
