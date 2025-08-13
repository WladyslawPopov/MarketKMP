package market.engine.common

import market.engine.shared.AuctionMarketDb

expect fun syncNotificationsFromUserDefaults(db : AuctionMarketDb) : Unit
