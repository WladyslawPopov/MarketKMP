package market.engine.common

import market.engine.core.data.types.PlatformType
import platform.UIKit.UIDevice

actual class Platform {
    actual fun getPlatform(): PlatformType {
        val idiom = UIDevice.currentDevice.name
        println("Platform Type: $idiom")
        return when {
            idiom.contains("iPhone") -> PlatformType.IOS
            else -> PlatformType.DESKTOP
        }
    }
}
