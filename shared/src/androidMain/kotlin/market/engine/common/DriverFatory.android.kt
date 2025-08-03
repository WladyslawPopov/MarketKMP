package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import market.engine.core.data.constants.DATABASE_NAME
import market.engine.shared.AuctionMarketDb

actual fun createSqlDriver(): SqlDriver {
    val d = AndroidSqliteDriver(AuctionMarketDb.Schema, appContext!!, DATABASE_NAME)
    return d
}
