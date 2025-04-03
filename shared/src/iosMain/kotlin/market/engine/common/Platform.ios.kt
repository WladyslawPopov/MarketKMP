package market.engine.common

import market.engine.core.data.types.PlatformWindowType

import platform.UIKit.UIDevice

actual class Platform {
    actual fun getPlatform(): PlatformWindowType {
        val idiom = UIDevice.currentDevice.name
        println("Platform Type: $idiom")
        return when {
            idiom.contains("iPhone") -> PlatformWindowType.MOBILE
            else -> PlatformWindowType.TABLET
        }
    }
}
