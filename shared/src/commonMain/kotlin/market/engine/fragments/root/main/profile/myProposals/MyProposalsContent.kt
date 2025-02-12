package market.engine.fragments.root.main.profile.myProposals

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
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
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
    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val successToast = stringResource(strings.operationSuccess)

    val updateFilters = remember { mutableStateOf(0) }

    val refresh = {
        viewModel.resetScroll()
        viewModel.onRefresh()
        updateFilters.value++
    }

    BackHandler(model.backHandler){
        when{
            viewModel.activeFiltersType.value != "" ->{
                viewModel.activeFiltersType.value = ""
            }
            else -> {
                component.goToBack()
            }
        }
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpritation != null && it.interpritation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                when(component.model.value.type){
                    LotsType.ALL_PROPOSAL ->{
                        OfferFilters.clearTypeFilter(LotsType.ALL_PROPOSAL)
                    }
                    LotsType.NEED_RESPOSE ->{
                        OfferFilters.clearTypeFilter(LotsType.NEED_RESPOSE)
                    }
                    else ->{
                        OfferFilters.clearTypeFilter(LotsType.ALL_PROPOSAL)
                    }
                }
                viewModel.onRefresh()
                updateFilters.value++
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.proposalIcon
            ) {
                viewModel.resetScroll()
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
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
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
            filtersContent = { isRefreshingFromFilters, onClose ->
                when(viewModel.activeFiltersType.value){
                    "filters" -> OfferFilterContent(
                        isRefreshingFromFilters,
                        listingData.value,
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
                if(offer.bids?.isNotEmpty() == true) {
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
                            component.goToProposal(offer.id, ProposalType.ACT_ON_PROPOSAL)
                        },
                        baseViewModel = viewModel,
                    )
                }
            }
        )
    }
}
