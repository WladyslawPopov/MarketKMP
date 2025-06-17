package market.engine.fragments.root.main.favPages

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Operations
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString


class FavPagesViewModel() : BaseViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val _favoritesTabList = MutableStateFlow(emptyList<FavoriteListItem>())
    val favoritesTabList = _favoritesTabList.asStateFlow()

    val initPosition = mutableStateOf(0)

    val isDragMode = mutableStateOf(false)

    fun getList(id: Long, onSuccess: (FavoriteListItem) -> Unit) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOffersListItem(id) }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (res != null) {
                    onSuccess(res)
                }
            }
        }
    }

    fun getFavTabList(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val newList = arrayListOf(
                FavoriteListItem(
                    id = 111,
                    title = getString(strings.myFavoritesTitle),
                    owner = UserData.login,
                    position = 0
                ),
                FavoriteListItem(
                    id = 222,
                    title = getString(strings.mySubscribedTitle),
                    owner = UserData.login,
                    position = 1
                ),
                FavoriteListItem(
                    id = 333,
                    title = getString(strings.myNotesTitle),
                    owner = UserData.login,
                    position = 2
                )
            )

            val data = withContext(Dispatchers.IO) {
                offersListOperations.getOffersList()
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                val buf = arrayListOf<FavoriteListItem>()
                buf.addAll(res ?: emptyList())

                newList.addAll(buf)

                val listPosition = db.favoritesTabListItemQueries
                val lh = listPosition.selectAll(UserData.login).executeAsList()
                lh.forEach { favoritesTabListItem ->
                    newList.find { it.id == favoritesTabListItem.itemId }?.position =
                        favoritesTabListItem.position.toInt()
                }

                newList.sortBy { it.position }
                newList.sortBy { !it.markedAsPrimary }

                if (newList != _favoritesTabList.value) {
                    _favoritesTabList.value = newList
                }

                onSuccess()
            }
        }
    }

    fun updateFavTabList(list: List<FavoriteListItem>){
        viewModelScope.launch {
            try {
                list.forEachIndexed { index, it ->
                    db.favoritesTabListItemQueries.insertEntry(
                        itemId = it.id,
                        owner = UserData.login,
                        position = index.toLong()
                    )
                }
                analyticsHelper.reportEvent(
                    "update_offers_list", mapOf()
                )

                _favoritesTabList.value = list
            }  catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    fun getOperationFavTab(id: Long, onSuccess: (List<Operations>) -> Unit){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOperations(id) }

            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                if (!res.isNullOrEmpty()){
                    onSuccess(res)
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }
}
