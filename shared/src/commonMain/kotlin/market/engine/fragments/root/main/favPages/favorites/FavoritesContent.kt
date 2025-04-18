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
import market.engine.core.network.functions.OfferOperations
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
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

    val ld = listingData.data
    val sd = listingData.searchData

    val offerOperations : OfferOperations = koinInject()

    val selectedItems = remember { favViewModel.selectItems }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val columns = remember { mutableStateOf(if (isBigScreen.value) 2 else 1) }

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

    val updateFilters = remember { mutableStateOf(0) }

    val refresh = {
        favViewModel.onError(ServerErrorException())
        favViewModel.resetScroll()
        favViewModel.refresh()
        data.refresh()
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

    val err = favViewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err) { refresh() } }
    }else{
        null
    }

    //update item when we back
    LaunchedEffect(favViewModel.updateItem.value) {
        if (favViewModel.updateItem.value != null) {
            val offer = withContext(Dispatchers.IO) {
                    favViewModel.getOfferById(favViewModel.updateItem.value!!)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    val item = data.itemSnapshotList.items.find { it.id == offer.id }
                    item?.state = offer.state
                    item?.session = offer.session
                    item?.buyNowPrice = offer.buyNowPrice
                    item?.images = offer.images
                    item?.freeLocation = offer.freeLocation
                    item?.currentPricePerItem = offer.currentPricePerItem
                    item?.title = offer.title
                    item?.region = offer.region
                    item?.note = offer.note
                    item?.relistingMode = offer.relistingMode
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
                favViewModel.resetScroll()
                data.refresh()
            },
            filtersContent = { isRefreshingFromFilters , onClose ->
                when (favViewModel.activeFiltersType.value){
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
                val fav =
                    mutableStateOf(
                        when(model.favType){
                            FavScreenType.NOTES -> {
                                offer.note != null && offer.note != ""
                            }
                            FavScreenType.FAVORITES -> {
                                offer.isWatchedByMe
                            }
                            else -> {
                                true
                            }
                        }
                    )


                AnimatedVisibility(fav.value, enter = fadeIn(), exit = fadeOut()) {
                    OfferItem(
                        offer,
                        isGrid = (columns.value > 1),
                        notesShow = true,
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
                            favViewModel.updateItem.value = selectedOffer.id
                            favViewModel.updateItemTrigger.value++

                            favViewModel.updateUserInfo()
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
