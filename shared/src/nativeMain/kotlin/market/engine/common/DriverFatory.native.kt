package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import market.engine.core.data.constants.DATABASE_NAME
import market.engine.shared.MarketDB
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun createSqlDriver(): SqlDriver {
   try {
      return NativeSqliteDriver(MarketDB.Schema, DATABASE_NAME)
   } catch (e: Exception) {
      println("!!! Initial database opening failed. Will try to recover by deleting and recreating the database. Error: ${e.message}")
      e.printStackTrace()

      try {
         deleteDatabase(DATABASE_NAME)
         return NativeSqliteDriver(MarketDB.Schema, DATABASE_NAME)
      } catch (finalException: Exception) {
         println("!!! FATAL: Failed to recreate database after deletion. Crashing is now unavoidable. Final Error: ${finalException.message}")
         throw finalException
      }
   }
}

@OptIn(ExperimentalForeignApi::class)
private fun deleteDatabase(dbName: String) {
   val fileManager = NSFileManager.defaultManager
   val documentsPath = NSSearchPathForDirectoriesInDomains(
      NSDocumentDirectory,
      NSUserDomainMask,
      true
   ).first() as String

   val dbPath = "$documentsPath/$dbName"

   if (fileManager.fileExistsAtPath(dbPath)) {
      try {
         fileManager.removeItemAtPath(dbPath, null)
         println("--- Deleted corrupted database file at path: $dbPath")
      } catch (e: Exception) {
         println("--- Error while trying to delete database file: ${e.message}")
      }
   }
}
