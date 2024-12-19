package market.engine.fragments.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ToastType
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import org.jetbrains.compose.resources.stringResource
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

    val ld = listingData.data.subscribeAsState()
    val sd = listingData.searchData.subscribeAsState()

    val selectedItems = remember { favViewModel.selectItems }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val successToast = stringResource(strings.operationSuccess)

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val noFound = @Composable {
        if (ld.value.filters.any {it.interpritation != null && it.interpritation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                ld.value.filters.clear()
                ld.value.filters.addAll(OfferFilters.filtersFav.toList())
                favViewModel.refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.emptyFavoritesLabel),
                image = drawables.emptyFavoritesImage
            ) {
                favViewModel.updateUserInfo()
                ld.value.resetScroll()
                favViewModel.refresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(favViewModel.updateItem.value) {
        if (favViewModel.updateItem.value != null) {
            withContext(Dispatchers.Default) {
                val offer =
                    favViewModel.getUpdatedOfferById(favViewModel.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    if (offer != null) {
                        data.itemSnapshotList.items.find { it.id == offer.id }?.isWatchedByMe =
                            offer.isWatchedByMe
                    }
                    favViewModel.updateItem.value = null
                }
            }
        }
    }

    BaseContent(
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
        onRefresh = {
            favViewModel.updateUserInfo()
            ld.value.resetScroll()
            favViewModel.refresh()
        },
        error = null,
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
                favViewModel.updateUserInfo()
                ld.value.resetScroll()
                data.refresh()
            },
            filtersContent = { isRefreshingFromFilters, onClose ->
                when (favViewModel.activeFiltersType.value){
                    "filters" -> OfferFilterContent(
                        isRefreshingFromFilters,
                        ld.value,
                        favViewModel,
                        LotsType.FAVORITES,
                        onClose
                    )
                    "sorting" -> SortingListingContent(
                        isRefreshingFromFilters,
                        ld.value,
                        onClose
                    )
                }
            },
            additionalBar = {
                AnimatedVisibility(
                    visible = selectedItems.isNotEmpty(),
                    enter = expandIn(),
                    exit = fadeOut(),
                    modifier = Modifier.animateContentSize()
                ) {
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
                                    favViewModel.updateUserInfo()
                                    data.refresh()
                                }
                            }
                        }
                    )
                }
            },
            item = { offer ->
                val isSelect = rememberUpdatedState(selectedItems.contains(offer.id))
                AnimatedVisibility(offer.isWatchedByMe, exit = fadeOut()) {
                    OfferItem(
                        offer,
                        isGrid = (columns.value > 1),
                        baseViewModel = favViewModel,
                        isSelection = isSelect.value,
                        onSelectionChange = { isSelect ->
                            if (isSelect) {
                                favViewModel.selectItems.add(offer.id)
                            } else {
                                favViewModel.selectItems.remove(offer.id)
                            }
                        },
                        onUpdateOfferItem = { selectedOffer ->
                            data.itemSnapshotList.find { it?.id == selectedOffer.id }?.isWatchedByMe =
                                false
                            favViewModel.updateUserInfo()
                            favViewModel.updateItem.value = selectedOffer.id
                            favViewModel.updateItem.value = null // update item immediately
                            favViewModel.showToast(
                                ToastItem(
                                    isVisible = true,
                                    type = ToastType.SUCCESS,
                                    message = successToast
                                )
                            )
                        },
                    ) {
                        if (favViewModel.selectItems.isNotEmpty()) {
                            if (favViewModel.selectItems.contains(offer.id)) {
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
                }
            }
        )
    }
}
