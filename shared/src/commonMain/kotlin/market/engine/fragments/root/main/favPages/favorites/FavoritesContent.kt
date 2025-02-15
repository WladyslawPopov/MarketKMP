package market.engine.fragments.root.main.favPages.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import market.engine.core.network.functions.OfferOperations
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ToastType
import market.engine.core.data.types.WindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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

    val ld = listingData.data.subscribeAsState()
    val sd = listingData.searchData.subscribeAsState()

    val offerOperations : OfferOperations = koinInject()

    val selectedItems = remember { favViewModel.selectItems }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val successToast = stringResource(strings.operationSuccess)

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    BackHandler(model.backHandler){
        when{
            favViewModel.activeFiltersType.value != "" ->{
                favViewModel.activeFiltersType.value = ""
            }
            else -> {

            }
        }
    }

    val updateFilters = remember { mutableStateOf(0) }


    val refresh = {
        favViewModel.onError(ServerErrorException())
        favViewModel.resetScroll()
        favViewModel.refresh()
        updateFilters.value++
    }


    val noFound = @Composable {
        if (ld.value.filters.any {it.interpretation != null && it.interpretation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                OfferFilters.clearTypeFilter(LotsType.FAVORITES)
                listingData.data.value.filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES)
                refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.emptyFavoritesLabel),
                image = drawables.emptyFavoritesImage
            ) {
                refresh()
            }
        }
    }


    //update item when we back
    LaunchedEffect(favViewModel.updateItem.value) {
        if (favViewModel.updateItem.value != null) {
            val offer = withContext(Dispatchers.IO) {
                    favViewModel.getOfferById(favViewModel.updateItem.value!!)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    data.itemSnapshotList.items.find { it.id == offer.id }?.isWatchedByMe =
                        offer.isWatchedByMe
                }
                favViewModel.updateItemTrigger.value++
                favViewModel.updateItem.value = null
            }
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            refresh()
        },
        error = null,
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
                favViewModel.resetScroll()
                data.refresh()
            },
            filtersContent = { isRefreshingFromFilters, onClose ->
                when (favViewModel.activeFiltersType.value){
                    "filters" -> OfferFilterContent(
                        isRefreshingFromFilters,
                        ld.value.filters,
                        favViewModel,
                        LotsType.FAVORITES,
                        onClose
                    )
                    "sorting" -> SortingOffersContent(
                        isRefreshingFromFilters,
                        ld.value,
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
                                offerOperations.postOfferOperationUnwatch(item)
                            }

                            withContext(Dispatchers.Main) {
                                favViewModel.selectItems.clear()
                                data.refresh()
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
                        refresh()
                        updateFilters.value++
                    }
                )
            },
            item = { offer ->
                val isSelect = rememberUpdatedState(selectedItems.contains(offer.id))
                var fav = if(favViewModel.updateItemTrigger.value>=0) offer.isWatchedByMe else offer.isWatchedByMe

                AnimatedVisibility(fav, enter = fadeIn(), exit = fadeOut()) {
                    OfferItem(
                        offer,
                        isGrid = (columns.value > 1),
                        baseViewModel = favViewModel,
                        updateTrigger = favViewModel.updateItemTrigger.value,
                        isSelection = isSelect.value,
                        onSelectionChange = { select ->
                            if (select) {
                                favViewModel.selectItems.add(offer.id)
                            } else {
                                favViewModel.selectItems.remove(offer.id)
                            }
                        },
                        onUpdateOfferItem = { selectedOffer ->
                            data.itemSnapshotList.find { it?.id == selectedOffer.id }?.isWatchedByMe =
                                false
                            fav = false
                            favViewModel.updateItemTrigger.value++

                            favViewModel.updateUserInfo()

                            favViewModel.showToast(
                                ToastItem(
                                    isVisible = true,
                                    type = ToastType.SUCCESS,
                                    message = successToast
                                )
                            )
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
                        }
                    )
                }
            }
        )
    }
}
