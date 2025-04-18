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
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.Operations
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString

class FavPagesViewModel(private val db : MarketDB) : BaseViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    private val _favoritesTabList = MutableStateFlow(emptyList<FavoriteListItem>())
    val favoritesTabList = _favoritesTabList.asStateFlow()

    val isDragMode = mutableStateOf(false)

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

            val data = withContext(Dispatchers.IO) { offersListOperations.getOffersList() }

            withContext(Dispatchers.Main) {
                val res = data.success
                val buf = arrayListOf<FavoriteListItem>()
                buf.addAll(res ?: emptyList())
                buf.sortBy { !it.markedAsPrimary }


                newList.addAll(buf)

                val listPosition = db.favoritesTabListItemQueries
                val lh = listPosition.selectAll(UserData.login).executeAsList()
                lh.forEach { favoritesTabListItem ->
                    newList.find { it.id == favoritesTabListItem.itemId }?.position =
                        favoritesTabListItem.position.toInt()
                }

                newList.sortBy { it.position }

                _favoritesTabList.value = newList

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

    fun getOperationFields(type: String, id: Long, onSuccess: (title: String, List<Fields>) -> Unit){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                when(type){
                    "create_blank_offer_list" -> userOperations.getCreateBlankOfferList(id)
                    "copy_offers_list" -> offersListOperations.getCopyOffersList(id)
                    "rename_offers_list" -> offersListOperations.getRenameOffersList(id)
                    else -> ServerResponse<DynamicPayload<OperationResult>>(null,ServerErrorException())
                }
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                if (!res?.fields.isNullOrEmpty()){
                    onSuccess(res?.description?:"", res?.fields!!)
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun deleteFavTab(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.postOperationsDelete(id) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error
                if (res?.success == true) {
                    showToast(
                        successToastItem.copy(
                            message = getString(
                                strings.operationSuccess
                            )
                        )
                    )
                    onSuccess()
                } else {
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun pinFavTab(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.postOperationPin(id) }

            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                if (res == true) {
                    showToast(
                        successToastItem.copy(
                            message = getString(
                                strings.operationSuccess
                            )
                        )
                    )
                    onSuccess()
                } else {
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun unpinFavTab(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.postOperationUnpin(id) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error
                if (res == true) {
                    showToast(
                        successToastItem.copy(
                            message = getString(
                                strings.operationSuccess
                            )
                        )
                    )
                    onSuccess()
                } else {
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun postFieldsSend(
        type: String,
        id: Long,
        body: HashMap<String, JsonElement>,
        onSuccess: () -> Unit,
        errorCallback: (List<Fields>) -> Unit
    ){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                when(type){
                    "create_blank_offer_list" -> userOperations.postCreateBlankOfferList(UserData.login, body)
                    "copy_offers_list" -> offersListOperations.postCopyOffersList(id, body)
                    "rename_offers_list" -> offersListOperations.postRenameOffersList(id, body)
                    else -> ServerResponse<DynamicPayload<OperationResult>>(null, ServerErrorException())
                }
            }
            val res = data.success
            val error = data.error
            if (res != null) {
                if (res.operationResult?.result == "ok") {
                    showToast(
                        successToastItem.copy(
                            message = getString(
                                strings.operationSuccess
                            )
                        )
                    )

                    onSuccess()
                } else {
                    showToast(
                        errorToastItem.copy(
                            message = getString(
                                strings.operationFailed
                            )
                        )
                    )

                    errorCallback(res.recipe?.fields ?: res.fields)
                }
            }else{
                if (error != null)
                    onError(error)

                onSuccess()
            }
        }
    }
}
