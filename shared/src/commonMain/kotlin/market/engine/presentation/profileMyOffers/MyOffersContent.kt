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
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.ToastItem
import market.engine.core.types.LotsType
import market.engine.core.types.ToastType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getCurrentDate
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
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
                        viewModel.updateItem.value = null
                    }else{
                        viewModel.updateItem.value = null
                    }
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
            viewModel.firstVisibleItemIndex = 0
            viewModel.firstVisibleItemScrollOffset = 0
            data.refresh()
        },
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
                listingData,
                model.type,
                onClose
            )
        },
        sortingContent = { isRefreshingFromFilters, onClose ->
            SortingListingContent(
                isRefreshingFromFilters,
                listingData,
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
                MyOffersItem(
                    offer = offer,
                    viewModel,
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
                    onItemClick = {
                        component.goToOffer(offer)
                        // set item for update
                        viewModel.updateItem.value = offer.id
                    }
                )
            }
        }
    )
}
