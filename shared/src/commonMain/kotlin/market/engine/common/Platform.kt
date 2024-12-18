package market.engine.common

import market.engine.core.data.types.PlatformType

expect class Platform() {
    fun getPlatform(): PlatformType
}
