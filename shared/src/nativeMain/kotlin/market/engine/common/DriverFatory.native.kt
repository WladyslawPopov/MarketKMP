package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import market.engine.shared.MarketDB

actual fun createSqlDriver(): SqlDriver {
   return NativeSqliteDriver(MarketDB.Schema, "MarketDB.db")
}
