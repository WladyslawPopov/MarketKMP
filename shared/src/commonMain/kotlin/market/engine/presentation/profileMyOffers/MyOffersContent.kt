package market.engine.presentation.profileMyOffers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.items.ToastItem
import market.engine.core.types.LotsType
import market.engine.core.types.ToastType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getCurrentDate
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.OfferItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    drawerState: DrawerState,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data.subscribeAsState()
    val searchData = viewModel.listingData.value.searchData.subscribeAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val successToast = stringResource(strings.operationSuccess)

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
    LaunchedEffect(Unit) {
        if (listingData.value.updateItem.value != null) {
            withContext(Dispatchers.Default) {
                val offer =
                    viewModel.getUpdatedOfferById(listingData.value.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    if (offer != null) {
                        val item = data.itemSnapshotList.items.find { it.id == offer.id }
                        item?.state = offer.state
                        item?.session = offer.session
                    }

                    listingData.value.updateItem.value = null
                }
            }
        }
    }

    ListingBaseContent(
        columns = columns,
        modifier = modifier,
        listingData.value,
        searchData = searchData.value,
        data = data,
        baseViewModel = viewModel,
        onRefresh = {
            listingData.value.resetScroll()
            viewModel.onRefresh()
        },
        noFound = noFound,
        topBar = {
            ProfileMyOffersAppBar(
                model.type,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectMyOfferPage(newType)
                }
            )
        },
        floatingActionButton = {
            floatingCreateOfferButton {

            }
        },
        filtersContent = { isRefreshingFromFilters, onClose ->
            OfferFilterContent(
                isRefreshingFromFilters,
                listingData.value,
                model.type,
                onClose
            )
        },
        sortingContent = { isRefreshingFromFilters, onClose ->
            SortingListingContent(
                isRefreshingFromFilters,
                listingData.value,
                onClose
            )
        },
        item = { offer->
            var checkItemSession = true
            when (model.type){
                LotsType.MYLOT_ACTIVE ->{
                    checkItemSession = offer.state == "active" && offer.session != null
                }

                LotsType.MYLOT_UNACTIVE ->{
                    checkItemSession = offer.state != "active"
                }

                LotsType.MYLOT_FUTURE ->{
                    val currentDate : Long? = getCurrentDate().toLongOrNull()
                    if (currentDate != null) {
                        val initD = (offer.session?.start?.toLongOrNull() ?:1L) - currentDate
                        checkItemSession =
                            offer.state == "active" && initD > 0
                    }
                }
                else ->{}
            }
            AnimatedVisibility(checkItemSession, exit = fadeOut()) {
                OfferItem(
                    offer,
                    isGrid = (columns.value > 1),
                    baseViewModel = viewModel,
                    onUpdateOfferItem = {
                        listingData.value.updateItem.value = it.id
                        viewModel.showToast(
                            ToastItem(
                                isVisible = true,
                                type = ToastType.SUCCESS,
                                message = successToast
                            )
                        )
                    },
                ){
                    component.goToOffer(offer)
                }
            }
        }
    )
}
