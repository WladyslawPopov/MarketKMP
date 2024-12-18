package market.engine.core.utils

import androidx.compose.runtime.Composable
import market.engine.common.Platform
import market.engine.core.data.types.PlatformType
import market.engine.core.data.types.WindowType

@Composable
fun getWindowType(): WindowType {

    val platform = Platform().getPlatform()

    return when(platform) {
        PlatformType.DESKTOP -> WindowType.Big
        else -> WindowType.Compact
    }
}
