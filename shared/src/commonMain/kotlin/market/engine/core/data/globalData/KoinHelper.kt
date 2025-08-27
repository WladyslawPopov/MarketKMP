package market.engine.core.data.globalData

import kotlinx.coroutines.sync.Mutex
import market.engine.shared.AuctionMarketDb
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object KoinHelper : KoinComponent {
    val marketDb: AuctionMarketDb by inject()
    val mutex : Mutex by inject()
}
