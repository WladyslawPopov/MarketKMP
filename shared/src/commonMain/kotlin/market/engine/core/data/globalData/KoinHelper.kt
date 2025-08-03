package market.engine.core.data.globalData

import market.engine.shared.AuctionMarketDb
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object KoinHelper : KoinComponent {
    val marketDb: AuctionMarketDb by inject()
}
