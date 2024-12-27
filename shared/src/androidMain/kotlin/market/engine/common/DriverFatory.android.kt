package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import market.engine.shared.MarketDB

actual fun createSqlDriver(): SqlDriver {
    val d = AndroidSqliteDriver(MarketDB.Schema, appContext!!, "MarketDB.db")

//    MarketDB.Schema.migrate(
//        driver = d,
//        oldVersion = 1,
//        newVersion = MarketDB.Schema.version,
//        AfterVersion(2) { driver ->
//            driver.execute(null, "CREATE TABLE IF NOT EXISTS offerVisitedHistory (id INTEGER PRIMARY KEY, owner INTEGER NOT NULL)", 0)
//        },
//    )
    return d
}
