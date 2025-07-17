package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import market.engine.core.data.constants.DATABASE_NAME
import market.engine.shared.marketDb

actual fun createSqlDriver(): SqlDriver {
    val d = AndroidSqliteDriver(marketDb.Schema, appContext!!, DATABASE_NAME)
    return d
}
