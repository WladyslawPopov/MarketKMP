package market.engine.common

import market.engine.core.data.types.PlatformWindowType
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPhone
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIApplication
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight


actual class Platform {
    actual fun getPlatform(): PlatformWindowType {
        // Determine device type
        val idiom = UIDevice.currentDevice.userInterfaceIdiom
        val deviceType = when (idiom) {
            UIUserInterfaceIdiomPhone -> PlatformWindowType.MOBILE
            UIUserInterfaceIdiomPad -> PlatformWindowType.TABLET
            else -> PlatformWindowType.TABLET
        }

        // Determine interface orientation
        val interfaceOrientation = UIApplication.sharedApplication.statusBarOrientation()
        val isPortrait = when (interfaceOrientation) {
            UIInterfaceOrientationPortrait -> true
            UIInterfaceOrientationLandscapeLeft, UIInterfaceOrientationLandscapeRight -> false
            else -> true
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
