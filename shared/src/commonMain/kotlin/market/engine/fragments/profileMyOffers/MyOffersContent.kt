package market.engine.fragments.profileMyOffers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ToastType
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.OfferItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = model.listingData.data
    val searchData = model.listingData.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()


    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val successToast = stringResource(strings.operationSuccess)

    val refresh = {
        listingData.value.resetScroll()
        viewModel.onRefresh()
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpritation != null && it.interpritation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                when(component.model.value.type){
                    LotsType.MYLOT_ACTIVE ->{
                        OfferFilters.clearTypeFilter(LotsType.MYLOT_ACTIVE)
                        listingData.value.filters.clear()
                        listingData.value.filters.addAll(OfferFilters.filtersMyLotsActive.toList())
                    }
                    LotsType.MYLOT_UNACTIVE ->{
                        OfferFilters.clearTypeFilter(LotsType.MYLOT_UNACTIVE)
                        listingData.value.filters.clear()
                        listingData.value.filters.addAll(OfferFilters.filtersMyLotsUnactive.toList())
                    }
                    LotsType.MYLOT_FUTURE ->{
                        OfferFilters.clearTypeFilter(LotsType.MYLOT_FUTURE)
                        listingData.value.filters.clear()
                        listingData.value.filters.addAll(OfferFilters.filtersMyLotsFuture.toList())
                    }
                    else ->{
                        listingData.value.filters.clear()
                    }
                }
                viewModel.onRefresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.emptyOffersIcon
            ) {
                listingData.value.resetScroll()
                viewModel.onRefresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(viewModel.updateItem.value) {
        if (viewModel.updateItem.value != null) {
            withContext(Dispatchers.Default) {
                val offer =
                    viewModel.getUpdatedOfferById(viewModel.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    if (offer != null) {
                        val item = data.itemSnapshotList.items.find { it.id == offer.id }
                        item?.state = offer.state
                        item?.session = offer.session
                    }

                    viewModel.updateItem.value = null
                }
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
        toastItem = viewModel.toastItem,
        floatingActionButton = {
            floatingCreateOfferButton {
                component.goToCreateOffer(CreateOfferType.CREATE)
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            columns = columns,
            listingData = listingData.value,
            searchData = searchData.value,
            data = data,
            baseViewModel = viewModel,
            onRefresh = {
                refresh()
            },
            noFound = noFound,
            additionalBar = {
                FiltersBar(
                    searchData.value,
                    listingData.value,
                    isShowGrid = false,
                    onFilterClick = {
                        viewModel.activeFiltersType.value = "filters"
                    },
                    onSortClick = {
                        viewModel.activeFiltersType.value = "sorting"
                    },
                    onRefresh = {
                        refresh()
                    }
                )
            },
            filtersContent = { isRefreshingFromFilters, onClose ->
                when(viewModel.activeFiltersType.value){
                    "filters" -> OfferFilterContent(
                        isRefreshingFromFilters,
                        listingData.value,
                        viewModel,
                        model.type,
                        onClose
                    )
                    "sorting" -> SortingListingContent(
                        isRefreshingFromFilters,
                        listingData.value,
                        onClose
                    )
                }
            },
            item = { offer ->
                var checkItemSession = true
                when (model.type) {
                    LotsType.MYLOT_ACTIVE -> {
                        checkItemSession = offer.state == "active" && offer.session != null
                    }

                    LotsType.MYLOT_UNACTIVE -> {
                        checkItemSession = offer.state != "active"
                    }

                    LotsType.MYLOT_FUTURE -> {
                        val currentDate: Long? = getCurrentDate().toLongOrNull()
                        if (currentDate != null) {
                            val initD = (offer.session?.start?.toLongOrNull() ?: 1L) - currentDate
                            checkItemSession =
                                offer.state == "active" && initD > 0
                        }
                    }

                    else -> {}
                }
                AnimatedVisibility(checkItemSession, enter = fadeIn(), exit = fadeOut()) {
                    OfferItem(
                        offer,
                        isGrid = (columns.value > 1),
                        baseViewModel = viewModel,
                        goToCreateOffer = { type ->
                            component.goToCreateOffer(type, offer.id, offer.catpath[0])
                        },
                        onUpdateOfferItem = {
                            viewModel.updateItem.value = it.id
                            viewModel.showToast(
                                ToastItem(
                                    isVisible = true,
                                    type = ToastType.SUCCESS,
                                    message = successToast
                                )
                            )
                        },
                    ) {
                        component.goToOffer(offer)
                    }
                }
            }
        )
    }
}
