package market.engine.core.utils

import market.engine.common.Platform
import market.engine.core.data.types.PlatformType
import market.engine.core.data.types.WindowType

fun getWindowType(): WindowType {
    val platform = Platform().getPlatform()

    return when(platform) {
        PlatformType.DESKTOP -> WindowType.Big
        else -> WindowType.Compact
    }
}
