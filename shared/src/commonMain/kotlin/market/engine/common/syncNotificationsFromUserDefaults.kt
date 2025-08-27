package market.engine.common

import kotlinx.coroutines.sync.Mutex
import market.engine.shared.AuctionMarketDb

expect fun syncNotificationsFromUserDefaults(db : AuctionMarketDb, mutex: Mutex) : Unit
