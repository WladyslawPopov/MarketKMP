package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import market.engine.core.data.constants.DATABASE_NAME
import market.engine.shared.AuctionMarketDb

actual fun createSqlDriver(): SqlDriver {
   return NativeSqliteDriver(AuctionMarketDb.Schema, DATABASE_NAME, maxReaderConnections = 3)
}
