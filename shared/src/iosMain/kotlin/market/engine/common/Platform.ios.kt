package market.engine.common

import market.engine.core.data.types.PlatformWindowType
import platform.Foundation.NSThread
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPhone
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIApplication
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown
import platform.UIKit.UIWindowScene
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_sync


actual class Platform {
    actual fun getPlatform(): PlatformWindowType {

        fun <T> runOnMainThread(block: () -> T): T {
            if (NSThread.isMainThread()) {
                return block()
            }

            var result: T? = null
            dispatch_sync(dispatch_get_main_queue()) {
                result = block()
            }
            return result!!
        }

        // Determine device type
        val idiom = UIDevice.currentDevice.userInterfaceIdiom
        val deviceType = when (idiom) {
            UIUserInterfaceIdiomPhone -> PlatformWindowType.MOBILE
            UIUserInterfaceIdiomPad -> PlatformWindowType.TABLET
            else -> PlatformWindowType.TABLET
        }

        // Determine interface orientation
        val interfaceOrientation = runOnMainThread {
            val windowScene = UIApplication.sharedApplication.connectedScenes
                .firstOrNull { it is UIWindowScene } as? UIWindowScene
            windowScene?.interfaceOrientation ?: UIInterfaceOrientationPortrait
        }

        val isPortrait = when (interfaceOrientation) {
            UIInterfaceOrientationPortrait, UIInterfaceOrientationPortraitUpsideDown -> true
            else -> false // LandscapeLeft, LandscapeRight, Unknown
        }

        // Debug log
        println("Platform Type: $deviceType, isPortrait: $isPortrait, orientation: $interfaceOrientation")

        // Combine device type and orientation
        return when (deviceType) {
            PlatformWindowType.TABLET -> if (isPortrait) PlatformWindowType.TABLET_PORTRAIT else PlatformWindowType.TABLET
            PlatformWindowType.MOBILE -> if (isPortrait) PlatformWindowType.MOBILE_PORTRAIT else PlatformWindowType.MOBILE
            else -> deviceType
        }
    }
}
