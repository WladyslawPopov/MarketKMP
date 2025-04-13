package market.engine.fragments.root.main.favPages.notes

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
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.types.LotsType
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesContent(
    component: NotesComponent,
    modifier: Modifier,
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val viewModel = model.notesViewModel
    val listingData = viewModel.listingData.value
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val ld = listingData.data
    val sd = listingData.searchData

    val selectedItems = remember { viewModel.selectItems }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val columns = remember { mutableStateOf(if (isBigScreen.value) 2 else 1) }

    BackHandler(model.backHandler){
        when{
            viewModel.activeFiltersType.value != "" ->{
                if (viewModel.openFiltersCat.value){
                    viewModel.catBack.value = true
                }else {
                    viewModel.activeFiltersType.value = ""
                }
            }
            else -> {

            }
        }
    }

    val updateFilters = remember { mutableStateOf(0) }

    val refresh = {
        viewModel.onError(ServerErrorException())
        viewModel.resetScroll()
        viewModel.refresh()
        data.refresh()
        updateFilters.value++
    }

    val noFound = @Composable {
        if (ld.value.filters.any {it.interpretation != null && it.interpretation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                listingData.data.value.filters.clear()
                refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                image = drawables.emptyFavoritesImage
            ) {
                refresh()
            }
        }
    }

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err) { refresh() } }
    }else{
        null
    }

    //update item when we back
    LaunchedEffect(viewModel.updateItem.value) {
        if (viewModel.updateItem.value != null) {
            val offer = withContext(Dispatchers.IO) {
                    viewModel.getOfferById(viewModel.updateItem.value!!)
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
                viewModel.updateItemTrigger.value++
                viewModel.updateItem.value = null
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
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            columns = columns,
            listingData = ld.value,
            data = data,
            searchData = sd.value,
            baseViewModel = viewModel,
            noFound = noFound,
            onRefresh = {
                viewModel.resetScroll()
                data.refresh()
            },
            filtersContent = { isRefreshingFromFilters , onClose ->
                when (viewModel.activeFiltersType.value){
                    "filters" -> OfferFilterContent(
                        viewModel.openFiltersCat,
                        viewModel.catBack,
                        isRefreshingFromFilters,
                        ld.value.filters,
                        viewModel,
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
                FiltersBar(
                    sd.value,
                    ld.value,
                    updateFilters.value,
                    isShowGrid = false,
                    onFilterClick = {
                        viewModel.activeFiltersType.value = "filters"
                    },
                    onSortClick = {
                        viewModel.activeFiltersType.value = "sorting"
                    },
                    onRefresh = {
                        refresh()
                        updateFilters.value++
                    }
                )
            },
            item = { offer ->
                val isSelect = rememberUpdatedState(selectedItems.contains(offer.id))
                val noty = viewModel.updateItemTrigger.value >= 0 && offer.note?.isNotEmpty() == true

                AnimatedVisibility(noty, enter = fadeIn(), exit = fadeOut()) {
                    OfferItem(
                        offer,
                        isGrid = (columns.value > 1),
                        baseViewModel = viewModel,
                        updateTrigger = viewModel.updateItemTrigger.value,
                        isSelection = isSelect.value,
                        notesShow = true,
                        onSelectionChange = { select ->
                            if (select) {
                                viewModel.selectItems.add(offer.id)
                            } else {
                                viewModel.selectItems.remove(offer.id)
                            }
                        },
                        onUpdateOfferItem = { selectedOffer ->
                            viewModel.updateItem.value = selectedOffer.id
                            viewModel.updateItemTrigger.value++
                            viewModel.updateUserInfo()
                        },
                        onItemClick = {
                            if (viewModel.selectItems.isNotEmpty()) {
                                if (isSelect.value) {
                                    viewModel.selectItems.remove(offer.id)
                                } else {
                                    viewModel.selectItems.add(offer.id)
                                }
                            } else {
                                component.goToOffer(offer)
                                // set item for update
                                viewModel.updateItem.value = offer.id
                            }
                        }
                    )
                }
            }
        )
    }
}
