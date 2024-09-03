package market.engine.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity

actual class ScreenSizeProvider actual constructor() {
    @Composable
    actual fun getScreenWidthDp(): Float {
        val context = LocalContext.current
        val metrics = context.resources.displayMetrics
        val density = LocalDensity.current

        return metrics.widthPixels / density.density
    }

    @Composable
    actual fun getScreenHeightDp(): Float {
        val context = LocalContext.current
        val metrics = context.resources.displayMetrics
        val density = LocalDensity.current

        return metrics.heightPixels / density.density
    }
}
