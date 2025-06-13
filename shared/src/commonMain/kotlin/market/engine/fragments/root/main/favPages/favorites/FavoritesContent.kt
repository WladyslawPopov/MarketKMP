package market.engine.fragments.root.main.favPages.favorites

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
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItem
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
        if (ld.filters.any {it.interpretation != null && it.interpretation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                OfferFilters.clearTypeFilter(LotsType.FAVORITES)
                listingData.data.filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES)
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
            val offer =
                data.itemSnapshotList.items.find { it.id == favViewModel.updateItem.value }
            component.updateItem(offer)
        }
    }

    val err = favViewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err.value) { component.onRefresh() } }
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
//        ListingBaseContent(
//            columns = columns.value,
//            listingData = ld.value,
//            data = data,
//            searchData = sd.value,
//            baseViewModel = favViewModel,
//            noFound = noFound,
//            onRefresh = {
//                favViewModel.onError(ServerErrorException())
//                favViewModel.resetScroll()
//                favViewModel.refresh()
//                data.refresh()
//                updateFilters.value++
//            },
//            filtersContent = { isRefreshingFromFilters, onClose ->
//                when (favViewModel.activeFiltersType.value) {
//                    "filters" -> OfferFilterContent(
//                        favViewModel.openFiltersCat,
//                        favViewModel.catBack,
//                        isRefreshingFromFilters,
//                        ld.value.filters,
//                        favViewModel,
//                        LotsType.FAVORITES,
//                        onClose
//                    )
//
//                    "sorting" -> SortingOffersContent(
//                        isRefreshingFromFilters,
//                        ld.value,
//                        isCabinet = true,
//                        onClose
//                    )
//                }
//            },
//            additionalBar = {
//                DeletePanel(
//                    selectedItems.size,
//                    onCancel = {
//                        favViewModel.selectItems.clear()
//                    },
//                    onDelete = {
//                        component.deleteSelectsItems(selectedItems)
//                    }
//                )
//
////                FiltersBar(
////                    sd.value,
////                    ld.value,
////                    updateFilters.value,
////                    isShowGrid = false,
////                    onFilterClick = {
////                        favViewModel.activeFiltersType.value = "filters"
////                    },
////                    onSortClick = {
////                        favViewModel.activeFiltersType.value = "sorting"
////                    },
////                    onRefresh = {
////                        component.onRefresh()
////                    }
////                )
//            },
//            item = { offer ->
//                val isSelect = rememberUpdatedState(selectedItems.contains(offer.id))
//                val isHideItem = mutableStateOf(
//                    component.isHideItem(offer)
//                )
//
//                CabinetOfferItem(
//                    offer,
//                    isVisible = !isHideItem.value,
//                    baseViewModel = favViewModel,
//                    updateTrigger = favViewModel.updateItemTrigger.value,
//                    isSelected = isSelect.value,
//                    goToProposal = remember {
//                        {
//                            component.goToProposal(it, offer.id)
//                        }
//                    },
//                    onSelectionChange = remember {
//                        { select ->
//                            if (select) {
//                                favViewModel.selectItems.add(offer.id)
//                            } else {
//                                favViewModel.selectItems.remove(offer.id)
//
//                            }
//                        }
//                    },
//                    onUpdateOfferItem = remember {
//                        { id ->
//                            favViewModel.updateItem.value = id
//                            favViewModel.updateItemTrigger.value++
//
//                            favViewModel.updateUserInfo()
//                        }
//                    },
//                    refreshPage = remember {
//                        {
//                            component.refreshTabs()
//                        }
//                    },
//                    onItemClick = remember {
//                        {
//                        if (favViewModel.selectItems.isNotEmpty()) {
//                            if (isSelect.value) {
//                                favViewModel.selectItems.remove(offer.id)
//                            } else {
//                                favViewModel.selectItems.add(offer.id)
//                            }
//                        } else {
//                            component.goToOffer(offer)
//                        }
//                    }
//                                           },
//                    goToCreateOffer =remember {
//                        {
//                            component.goToCreateOffer(it, offer.id)
//                        }
//                    },
//                    goToDynamicSettings = remember {
//                        { type, id ->
//                            goToDynamicSettings(type, id, null)
//                        }
//                    }
//                )
//            },
//            modifier = modifier
//        )
    }
}
