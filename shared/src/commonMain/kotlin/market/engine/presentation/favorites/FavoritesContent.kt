package market.engine.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import market.engine.core.network.functions.OfferOperations
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import org.koin.compose.koinInject

@Composable
fun FavoritesContent(
    modifier: Modifier,
    component: FavoritesComponent,
) {
    val modelState = component.model.subscribeAsState()
    val favViewModel = modelState.value.favViewModel
    val listingData = favViewModel.listingData
    val data = favViewModel.pagingDataFlow.collectAsLazyPagingItems()

    val offerOperations : OfferOperations = koinInject()

    val selectedItems = remember { favViewModel.selectItems }

    val ld = listingData.value.data.subscribeAsState()

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big

    LaunchedEffect(selectedItems){
        snapshotFlow {
            selectedItems
        }.collect {
            favViewModel.selectItems = it
        }
    }

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    ListingBaseContent(
        topBar = {
            FavoritesAppBar(
                FavScreenType.FAVORITES,
                modifier
            ) { type ->
                if (type == FavScreenType.SUBSCRIBED) {
                    component.goToSubscribes()
                }
            }
        },
        columns = columns,
        modifier = modifier,
        listingData = ld,
        data = data,
        searchData = listingData.value.searchData.subscribeAsState(),
        baseViewModel = favViewModel,
        onRefresh = {
            data.refresh()
        },
        filtersContent = { isRefreshingFromFilters, onClose ->
            OfferFilterContent(
                isRefreshingFromFilters,
                ld,
                LotsType.FAVORITES,
                onClose
            )
        },
        sortingContent = { isRefreshingFromFilters, onClose ->
            SortingListingContent(
                isRefreshingFromFilters,
                ld,
                onClose
            )
        },
        additionalBar = {
            AnimatedVisibility(
                visible = selectedItems.isNotEmpty(),
                enter = fadeIn() ,
                exit = fadeOut() ,
                modifier = Modifier.animateContentSize()
            ) {
                DeletePanel(
                    selectedItems.size,
                    onCancel = {
                        selectedItems.clear()
                    },
                    onDelete = {
                        favViewModel.viewModelScope.launch(Dispatchers.IO) {
                            selectedItems.forEach { item ->
                                offerOperations.postOfferOperationUnwatch(item)
                            }

                            withContext(Dispatchers.Main) {
                                selectedItems.clear()
                                data.refresh()
                            }
                        }
                    }
                )
            }
        },
        item = { offer->
            FavItem(
                offer,
                favViewModel,
                onSelectionChange = { isSelect ->
                    if (isSelect) {
                        selectedItems.add(offer.id)
                    } else {
                        selectedItems.remove(offer.id)
                    }
                },
                onUpdateOfferItem = {
                    data.refresh()
                },
                isSelected = selectedItems.contains(offer.id),
            ) {
                if (selectedItems.isNotEmpty()) {
                    selectedItems.add(offer.id)
                } else {
                    component.goToOffer(offer)
                }
            }
        }
    )
}
