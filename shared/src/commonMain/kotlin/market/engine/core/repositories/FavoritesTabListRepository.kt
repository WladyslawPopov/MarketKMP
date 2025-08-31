package market.engine.core.repositories

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.shared.AuctionMarketDb
import market.engine.shared.FavoritesTabListItem

class FavoritesTabListRepository(private val db: AuctionMarketDb, private val mutex: Mutex) {

    suspend fun getFavoritesTabList(): List<FavoritesTabListItem> {
        return mutex.withLock {
            try {
                db.favoritesTabListItemQueries.selectAll(UserData.login).executeAsList()
            } catch (_ : Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveFavoritesTabList(list: List<FavoriteListItem>) {
        mutex.withLock {
            db.transaction {
                list.forEachIndexed { index, it ->
                    db.favoritesTabListItemQueries.insertEntry(
                        itemId = it.id,
                        owner = UserData.login,
                        position = index.toLong()
                    )
                }
            }
        }
    }

    suspend fun deleteFavoritesTabListById(id: Long) {
        mutex.withLock {
            db.favoritesTabListItemQueries.deleteById(
                itemId = id,
                owner = UserData.login
            )
        }
    }

}
