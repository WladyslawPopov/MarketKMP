package market.engine.fragments.root.main.profile.myProposals

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyProposalsContent(
    component: MyProposalsComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val successToast = stringResource(strings.operationSuccess)

    val updateFilters = remember { mutableStateOf(0) }

    val refresh = {
        viewModel.resetScroll()
        viewModel.onRefresh()
        data.refresh()
        updateFilters.value++
    }

    BackHandler(model.backHandler){
        when{
            viewModel.activeFiltersType.value != "" ->{
                if (viewModel.openFiltersCat.value){
                    viewModel.catBack.value = true
                } else {
                    viewModel.activeFiltersType.value = ""
                }
            }
            else -> {
                component.goToBack()
            }
        }
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpretation != null && it.interpretation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                OfferFilters.clearTypeFilter(component.model.value.type)
                listingData.value.filters = OfferFilters.getByTypeFilter(component.model.value.type)
                refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.proposalIcon
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
            withContext(Dispatchers.Default) {
                val offer =
                    viewModel.getOfferById(viewModel.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    val oldItem = data.itemSnapshotList.items.find { it.id == viewModel.updateItem.value }
                    oldItem?.buyerData = offer?.buyerData
                    oldItem?.myMaximalBid = offer?.myMaximalBid.toString()
                    oldItem?.bids = offer?.bids
                    oldItem?.session = offer?.session
                    oldItem?.currentPricePerItem = offer?.currentPricePerItem.toString()
                    oldItem?.watchersCount = offer?.watchersCount ?: 0
                    oldItem?.viewsCount = offer?.viewsCount ?: 0

                    viewModel.updateItemTrigger.value++
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
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
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
                    }
                )
            },
            filtersContent = { isRefreshingFromFilters , onClose ->
                when(viewModel.activeFiltersType.value){
                    "filters" -> OfferFilterContent(
                        viewModel.openFiltersCat,
                        viewModel.catBack,
                        isRefreshingFromFilters,
                        listingData.value.filters,
                        viewModel,
                        model.type,
                        onClose
                    )
                    "sorting" -> SortingOffersContent(
                        isRefreshingFromFilters,
                        listingData.value,
                        onClose
                    )
                }
            },
            item = { offer ->
                MyProposalItem(
                    offer = offer,
                    onUpdateOfferItem = {
                        viewModel.updateItem.value = it.id
                        viewModel.showToast(
                            successToastItem.copy(
                                message = successToast
                            )
                        )

                    },
                    updateTrigger = viewModel.updateItemTrigger.value,
                    goToOffer = {
                        component.goToOffer(offer, true)
                    },
                    goToUser = {
                        component.goToUser(it)
                    },
                    goToDialog = {
                        component.goToDialog(it)
                    },
                    goToProposal = {
                        component.goToProposal(offer.id, it)
                    },
                    baseViewModel = viewModel,
                )
            }
        )
    }
}
