package market.engine.fragments.root.main.profile.myOffers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.offer_Items.CabinetOfferItemList
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val err = viewModel.errorMessage.collectAsState()

    val updateFilters = remember { mutableStateOf(0) }

    val refresh = {
        viewModel.resetScroll()
        viewModel.onRefresh()
        data.refresh()
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
                icon = drawables.emptyOffersIcon
            ) {
                refresh()
            }
        }
    }

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
                    if (offer != null) {
                        val item = data.itemSnapshotList.items.find { it.id == offer.id }
                        item?.setNewParams(offer)
                    }else{
                        val item = data.itemSnapshotList.items.find { it.id == viewModel.updateItem.value }
                        item?.session = null
                        item?.state = null
                    }

                    var isEmpty = false
                    when (model.type) {
                        LotsType.MYLOT_ACTIVE -> {
                            isEmpty = data.itemSnapshotList.items.none { it.state == "active" && it.session != null }
                        }

                        LotsType.MYLOT_UNACTIVE -> {
                            isEmpty = data.itemSnapshotList.items.none { it.state != "active" }
                        }

                        LotsType.MYLOT_FUTURE -> {
                            val currentDate: Long? = getCurrentDate().toLongOrNull()
                            if (currentDate != null) {
                                isEmpty = data.itemSnapshotList.items.none {
                                    val initD = (it.session?.start?.toLongOrNull() ?: 1L) - currentDate
                                    it.state == "active" && initD > 0
                                }
                            }
                        }

                        else -> {}
                    }

                    if(isEmpty){
                        refresh()
                    }

                    viewModel.updateItem.value = null
                    viewModel.updateItemTrigger.value++
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
        floatingActionButton = {
            floatingCreateOfferButton {
                component.goToCreateOffer(CreateOfferType.CREATE, null, null)
            }
        },
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
                        updateFilters.value++
                    }
                )
            },
            filtersContent = { isRefreshingFromFilters, onClose ->
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
                        isCabinet = true,
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
                    CabinetOfferItemList(
                        offer,
                        baseViewModel = viewModel,
                        updateTrigger = viewModel.updateItemTrigger.value,
                        onUpdateOfferItem = { id ->
                            viewModel.updateItem.value = id
                        },
                        onItemClick = {
                            component.goToOffer(offer)
                        },
                        goToDynamicSettings = { type, id ->
                            component.goToDynamicSettings(type, id)
                        },
                        goToCreateOffer = { type ->
                            component.goToCreateOffer(type, offer.id, offer.catPath)
                        },
                        goToProposal = {
                            component.goToProposals(offer.id, it)
                        },
                    )
                }
            }
        )
    }
}
