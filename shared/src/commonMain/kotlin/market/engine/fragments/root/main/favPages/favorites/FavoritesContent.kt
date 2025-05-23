package market.engine.fragments.root.main.favPages.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItemList
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoritesContent(
    component: FavoritesComponent,
    modifier: Modifier,
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val favViewModel = model.favViewModel
    val listingData = favViewModel.listingData.value
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val ld = listingData.data
    val sd = listingData.searchData

    val selectedItems = remember { favViewModel.selectItems }

    val columns = remember { mutableStateOf(if (isBigScreen.value) 2 else 1) }
    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    BackHandler(model.backHandler){
        when{
            favViewModel.activeFiltersType.value != "" ->{
                if (favViewModel.openFiltersCat.value){
                    favViewModel.catBack.value = true
                }else {
                    favViewModel.activeFiltersType.value = ""
                }
            }
            else -> {

            }
        }
    }

    val updateFilters = remember { favViewModel.updateFilters }

    val noFound = @Composable {
        if (ld.value.filters.any {it.interpretation != null && it.interpretation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                OfferFilters.clearTypeFilter(LotsType.FAVORITES)
                listingData.data.value.filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES)
                component.onRefresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.emptyFavoritesLabel),
                image = drawables.emptyFavoritesImage
            ) {
                component.onRefresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(favViewModel.updateItem.value) {
        if (favViewModel.updateItem.value != null) {
            component.model.value.listId?.let { id ->
                favViewModel.getList(id) { item ->
                    val offer =
                        data.itemSnapshotList.items.find { it.id == favViewModel.updateItem.value }
                    if (!item.offers.contains(offer?.id)) {
                        offer?.session = null
                    }
                }
            }

            val offer = withContext(Dispatchers.IO) {
                favViewModel.getOfferById(favViewModel.updateItem.value!!)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    val item = data.itemSnapshotList.items.find { it.id == offer.id }
                    item?.setNewParams(offer)
                }

                if (model.favType != FavScreenType.FAV_LIST) {
                    val isEmpty = data.itemSnapshotList.items.none { item ->
                        when (model.favType) {
                            FavScreenType.NOTES -> {
                                item.note != null && item.note != ""
                            }

                            FavScreenType.FAVORITES -> {
                                item.isWatchedByMe
                            }

                            else -> {
                                item.session != null
                            }
                        }
                    }
                    if (isEmpty) {
                        component.onRefresh()
                    }
                } else {
                    favViewModel.getList(component.model.value.listId ?: 1L) {
                        val isEmpty = data.itemSnapshotList.items.none { item ->
                            it.offers.contains(item.id)
                        }
                        if (isEmpty) {
                            component.onRefresh()
                        }
                    }
                }

                favViewModel.updateItemTrigger.value++
                favViewModel.updateItem.value = null
            }
        }
    }

    val err = favViewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err) { component.onRefresh() } }
    }else{
        null
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = favViewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            columns = columns,
            listingData = ld.value,
            data = data,
            searchData = sd.value,
            baseViewModel = favViewModel,
            noFound = noFound,
            onRefresh = {
                favViewModel.onError(ServerErrorException())
                favViewModel.resetScroll()
                favViewModel.refresh()
                data.refresh()
                updateFilters.value++
            },
            filtersContent = { isRefreshingFromFilters, onClose ->
                when (favViewModel.activeFiltersType.value) {
                    "filters" -> OfferFilterContent(
                        favViewModel.openFiltersCat,
                        favViewModel.catBack,
                        isRefreshingFromFilters,
                        ld.value.filters,
                        favViewModel,
                        LotsType.FAVORITES,
                        onClose
                    )

                    "sorting" -> SortingOffersContent(
                        isRefreshingFromFilters,
                        ld.value,
                        isCabinet = true,
                        onClose
                    )
                }
            },
            additionalBar = {
                DeletePanel(
                    selectedItems.size,
                    onCancel = {
                        favViewModel.selectItems.clear()
                    },
                    onDelete = {
                        favViewModel.viewModelScope.launch(Dispatchers.IO) {
                            selectedItems.forEach { item ->
                                val type = when(model.favType){
                                    FavScreenType.NOTES -> {
                                        "delete_note"
                                    }
                                    FavScreenType.FAVORITES -> {
                                        "unwatch"
                                    }
                                    FavScreenType.FAV_LIST -> {
                                        "remove_from_list"
                                    }
                                    else -> {
                                        ""
                                    }
                                }

                                val body = HashMap<String, JsonElement>()
                                if (model.favType == FavScreenType.FAV_LIST) {
                                    body["offers_list_id"] = JsonPrimitive(model.listId)
                                }
                                favViewModel.postOperationFields(
                                    item,
                                    type,
                                    "offers",
                                    body,
                                    onSuccess = {
                                        favViewModel.selectItems.clear()
                                        data.refresh()
                                    },
                                    errorCallback = {

                                    }
                                )
                            }
                        }
                    }
                )

                FiltersBar(
                    sd.value,
                    ld.value,
                    updateFilters.value,
                    isShowGrid = false,
                    onFilterClick = {
                        favViewModel.activeFiltersType.value = "filters"
                    },
                    onSortClick = {
                        favViewModel.activeFiltersType.value = "sorting"
                    },
                    onRefresh = {
                        component.onRefresh()
                    }
                )
            },
            item = { offer ->
                val isSelect = rememberUpdatedState(selectedItems.contains(offer.id))
                val fav =
                    mutableStateOf(
                        when (model.favType) {
                            FavScreenType.NOTES -> {
                                offer.note != null && offer.note != ""
                            }

                            FavScreenType.FAVORITES -> {
                                offer.isWatchedByMe
                            }

                            else -> {
                                offer.session != null
                            }
                        }
                    )

                AnimatedVisibility(fav.value, enter = fadeIn(), exit = fadeOut()) {
                    CabinetOfferItemList(
                        offer,
                        baseViewModel = favViewModel,
                        updateTrigger = favViewModel.updateItemTrigger.value,
                        isSelection = isSelect.value,
                        goToProposal = {
                            component.goToProposal(it, offer.id)
                        },
                        onSelectionChange = { select ->
                            if (select) {
                                favViewModel.selectItems.add(offer.id)
                            } else {
                                favViewModel.selectItems.remove(offer.id)

                            }
                        },
                        onUpdateOfferItem = { id ->
                            favViewModel.updateItem.value = id
                            favViewModel.updateItemTrigger.value++

                            favViewModel.updateUserInfo()
                        },
                        refreshPage = {
                            component.refreshTabs()
                        },
                        onItemClick = {
                            if (favViewModel.selectItems.isNotEmpty()) {
                                if (isSelect.value) {
                                    favViewModel.selectItems.remove(offer.id)
                                } else {
                                    favViewModel.selectItems.add(offer.id)
                                }
                            } else {
                                component.goToOffer(offer)
                                // set item for update
                                favViewModel.updateItem.value = offer.id
                            }
                        },
                    )
                }
            },
            modifier = modifier
        )
    }
}
