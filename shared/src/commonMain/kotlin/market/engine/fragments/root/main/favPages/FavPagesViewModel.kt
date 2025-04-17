package market.engine.fragments.root.main.favPages

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.UrlBuilder
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.deserializePayload
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString

class FavPagesViewModel(private val db : MarketDB) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    private val _favoritesTabList = MutableStateFlow(emptyList<FavoriteListItem>())
    val favoritesTabList = _favoritesTabList.asStateFlow()

    fun init(type: FavScreenType): Flow<PagingData<Offer>> {
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

            }
            else -> {

            }
        }

        return pagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
        updateUserInfo()
        pagingRepository.refresh()
    }

    fun getFavTabList() {
        viewModelScope.launch {
            try {
                val url = UrlBuilder()
                    .addPathSegment("offers_lists")
                    .addPathSegment("get_cabinet_listing")
                    .build()

                val data = withContext(Dispatchers.IO) { apiService.getPage(url) }
                val serializer = Payload.serializer(FavoriteListItem.serializer())
                val value = deserializePayload(data.payload, serializer)

                _favoritesTabList.value = buildList {
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
                    newList.addAll(value.objects)

                    val listPosition = db.favoritesTabListItemQueries
                    val lh = listPosition.selectAll(UserData.login).executeAsList()
                    lh.forEach { favoritesTabListItem ->
                        newList.find { it.id == favoritesTabListItem.id }?.position =
                            favoritesTabListItem.position.toInt()
                    }

                    newList.sortBy { it.position }

                    clear()
                    addAll(newList)
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }
}
