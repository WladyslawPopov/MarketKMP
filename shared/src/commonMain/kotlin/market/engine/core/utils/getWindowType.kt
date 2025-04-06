package market.engine.core.utils

import market.engine.common.Platform
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.WindowType

fun getWindowType(): WindowType {
    val platform = Platform().getPlatform()

    return when(platform) {
        PlatformWindowType.DESKTOP -> WindowType.Big
        PlatformWindowType.TABLET -> WindowType.Big
        PlatformWindowType.MOBILE -> WindowType.Compact
        PlatformWindowType.TABLET_PORTRAIT -> WindowType.Big
        PlatformWindowType.MOBILE_PORTRAIT -> WindowType.Compact
    }
}
