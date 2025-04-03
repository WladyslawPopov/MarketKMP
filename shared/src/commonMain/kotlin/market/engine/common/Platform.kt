package market.engine.common

import market.engine.core.data.types.PlatformWindowType


expect class Platform() {
    fun getPlatform(): PlatformWindowType
}
