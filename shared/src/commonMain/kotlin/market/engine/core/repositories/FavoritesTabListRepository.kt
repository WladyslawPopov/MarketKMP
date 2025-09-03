package market.engine.core.repositories

import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.shared.AuctionMarketDb
import market.engine.shared.FavoritesTabListItem

class FavoritesTabListRepository(private val db: AuctionMarketDb) {

    fun getFavoritesTabList(): List<FavoritesTabListItem> = try {
            db.favoritesTabListItemQueries.selectAll(UserData.login).executeAsList()
        } catch (_ : Exception) {
            emptyList()
        }

    fun saveFavoritesTabList(list: List<FavoriteListItem>) = db.transaction {
            list.forEachIndexed { index, it ->
                db.favoritesTabListItemQueries.insertEntry(
                    itemId = it.id,
                    owner = UserData.login,
                    position = index.toLong()
                )
            }
        }

    fun deleteFavoritesTabListById(id: Long) = db.favoritesTabListItemQueries.deleteById(
            itemId = id,
            owner = UserData.login
        )
}
