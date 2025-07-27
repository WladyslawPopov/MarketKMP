package market.engine.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import market.engine.core.data.constants.DATABASE_NAME
import market.engine.shared.marketDb
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun createSqlDriver(): SqlDriver {
   return try {
      NativeSqliteDriver(marketDb.Schema, DATABASE_NAME)
   } catch (e: Exception) {
      println("!!! DB open failed. Attempting recovery. Error: ${e.message}")
      try {
         deleteDatabaseFromCorrectPath(DATABASE_NAME)
         println("--- DB file deleted. Recreating...")
         NativeSqliteDriver(marketDb.Schema, DATABASE_NAME)
      } catch (recoveryException: Exception) {
         println("!!! FATAL: Recovery failed. Error: ${recoveryException.message}")
         throw recoveryException
      }
   }
}

@OptIn(ExperimentalForeignApi::class)
private fun deleteDatabaseFromCorrectPath(dbName: String) {
   val fileManager = NSFileManager.defaultManager
   // 1. Получаем путь к папке Application Support
   val supportPath = NSSearchPathForDirectoriesInDomains(
      NSApplicationSupportDirectory, // <- ПРАВИЛЬНЫЙ КАТАЛОГ
      NSUserDomainMask,
      true
   ).firstOrNull() as? String ?: return // Если путь не найден, выходим

   val dbFilePath = "$supportPath/$dbName"

   // 3. Проверяем и удаляем
   if (fileManager.fileExistsAtPath(dbFilePath)) {
      try {
         // Удаляем файл по строковому пути
         fileManager.removeItemAtPath(dbFilePath, null)
         println("--- Successfully deleted corrupted database file at path: $dbFilePath")
      } catch (e: Exception) {
         println("--- Error while trying to delete database file: ${e.message}")
      }
   } else {
      println("--- Database file not found at path: $dbFilePath (this is normal if the DB was never created)")
   }
}
