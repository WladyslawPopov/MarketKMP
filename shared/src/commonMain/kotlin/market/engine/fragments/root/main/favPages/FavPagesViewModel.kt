package market.engine.fragments.root.main.favPages

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.map
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Operations
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class FavPagesViewModel : BaseViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    private val _favoritesTabList = MutableStateFlow(emptyList<FavoriteListItem>())
    val favoritesTabList = _favoritesTabList.asStateFlow()

    val updateFilters = mutableStateOf(0)

    val initPosition = mutableStateOf(0)

    val isDragMode = mutableStateOf(false)

    fun init(type: FavScreenType, listId: Long?= null): Flow<PagingData<OfferItem>> {
        when(type){
            FavScreenType.FAVORITES -> {
                listingData.value.data.value.filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES)
                listingData.value.data.value.methodServer = "get_cabinet_listing_watched_by_me"
                listingData.value.data.value.objServer = "offers"
            }
            FavScreenType.NOTES ->{
                listingData.value.data.value.methodServer = "get_cabinet_listing_my_notes"
                listingData.value.data.value.objServer = "offers"
            }
            FavScreenType.FAV_LIST ->{
                listingData.value.data.value.filters.add(
                    Filter("list_id", "$listId", "", null),
                )
                listingData.value.data.value.methodServer = "get_cabinet_listing_in_list"
                listingData.value.data.value.objServer = "offers"
            }
            else -> {}
        }

        return pagingRepository.getListing(listingData.value, apiService, Offer.serializer()).map {
            it.map { offer ->
                offer.parseToOfferItem()
            }
        }.cachedIn(viewModelScope)
    }

    fun refresh(){
        updateUserInfo()
        pagingRepository.refresh()
    }

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
