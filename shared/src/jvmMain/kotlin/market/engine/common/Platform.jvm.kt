package market.engine.common

import market.engine.core.data.types.PlatformWindowType


actual class Platform {
    actual fun getPlatform(): PlatformWindowType {
        return PlatformWindowType.DESKTOP
    }
}
