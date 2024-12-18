package market.engine.common

import market.engine.core.data.types.PlatformType

actual class Platform {
    actual fun getPlatform(): PlatformType {
        return PlatformType.ANDROID
    }
}
