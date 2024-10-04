package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import market.engine.shared.MarketDB

actual fun createSqlDriver(): SqlDriver {
    return AndroidSqliteDriver(MarketDB.Schema, appContext, "MarketDB.db")
}
