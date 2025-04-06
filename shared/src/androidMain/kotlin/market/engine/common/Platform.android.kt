package market.engine.common

import market.engine.core.data.types.PlatformWindowType
import kotlin.math.sqrt

actual class Platform {
    actual fun getPlatform(): PlatformWindowType {
        if (appContext != null) {
            val metrics = appContext?.resources?.displayMetrics
            if (metrics != null) {
                val widthInches = metrics.widthPixels / metrics.xdpi
                val heightInches = metrics.heightPixels / metrics.ydpi
                val diagonalInches =
                    sqrt((widthInches * widthInches + heightInches * heightInches).toDouble())
                val isPortrait = metrics.heightPixels > metrics.widthPixels
                return if (diagonalInches >= 7.0) {
                    if(isPortrait) PlatformWindowType.TABLET_PORTRAIT else PlatformWindowType.TABLET
                } else {
                    if(isPortrait) PlatformWindowType.MOBILE_PORTRAIT else PlatformWindowType.MOBILE
                }
            } else {
                return PlatformWindowType.MOBILE
            }
        }else{
            return PlatformWindowType.MOBILE
        }
    }
}
