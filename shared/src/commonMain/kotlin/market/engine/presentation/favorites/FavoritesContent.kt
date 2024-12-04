package market.engine.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import market.engine.core.network.functions.OfferOperations
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.items.ToastItem
import market.engine.core.repositories.UserRepository
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.types.ToastType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
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
    val userRepository : UserRepository = koinInject()

    val ld = listingData.value.data.subscribeAsState()
    val sd = listingData.value.searchData.subscribeAsState()

    val selectedItems = remember { ld.value.selectItems }

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big

    val successToast = stringResource(strings.operationSuccess)

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val noFound = @Composable {
        if (ld.value.filters.any {it.interpritation != null && it.interpritation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                ld.value.filters.clear()
                ld.value.filters.addAll(OfferFilters.filtersFav.toList())
                data.refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.emptyFavoritesLabel),
                image = drawables.emptyFavoritesImage
            ) {
                userRepository.updateUserInfo(favViewModel.viewModelScope)
                ld.value.resetScroll()
                data.refresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(Unit) {
        if (ld.value.updateItem.value != null) {
            withContext(Dispatchers.Default) {
                val offer =
                    favViewModel.getUpdatedOfferById(ld.value.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    if (offer != null) {
                        data.itemSnapshotList.items.find { it.id == offer.id }?.isWatchedByMe =
                            offer.isWatchedByMe
                        ld.value.updateItem.value = null
                    }else{
                        ld.value.updateItem.value = null
                    }
                }
            }
        }
    }

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
        listingData = ld.value,
        data = data,
        searchData = sd.value,
        baseViewModel = favViewModel,
        noFound = noFound,
        onRefresh = {
            userRepository.updateUserInfo(favViewModel.viewModelScope)
            ld.value.resetScroll()
            data.refresh()
        },
        filtersContent = { isRefreshingFromFilters, onClose ->
            OfferFilterContent(
                isRefreshingFromFilters,
                ld.value,
                LotsType.FAVORITES,
                onClose
            )
        },
        sortingContent = { isRefreshingFromFilters, onClose ->
            SortingListingContent(
                isRefreshingFromFilters,
                ld.value,
                onClose
            )
        },
        additionalBar = {
            AnimatedVisibility(
                visible = selectedItems.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.animateContentSize()
            ) {
                DeletePanel(
                    selectedItems.size,
                    onCancel = {
                        ld.value.selectItems.clear()
                    },
                    onDelete = {
                        favViewModel.viewModelScope.launch(Dispatchers.IO) {
                            selectedItems.forEach { item ->
                                offerOperations.postOfferOperationUnwatch(item)
                            }

                            withContext(Dispatchers.Main) {
                                ld.value.selectItems.clear()
                                userRepository.updateUserInfo(favViewModel.viewModelScope)
                                data.refresh()
                            }
                        }
                    }
                )
            }
        },
        item = { offer->
            AnimatedVisibility (offer.isWatchedByMe, exit = fadeOut()) {
                OfferItem(
                    offer,
                    isGrid = (columns.value > 1),
                    baseViewModel = favViewModel,
                    isSelection = selectedItems.contains(offer.id),
                    onSelectionChange = { isSelect ->
                        if (isSelect) {
                            ld.value.selectItems.add(offer.id)
                        } else {
                            ld.value.selectItems.remove(offer.id)
                        }
                    },
                    onUpdateOfferItem = { selectedOffer ->
                        data.itemSnapshotList.find { it?.id == selectedOffer.id }?.isWatchedByMe = false
                        userRepository.updateUserInfo(favViewModel.viewModelScope)
                        ld.value.updateItem.value = selectedOffer.id
                        ld.value.updateItem.value = null // update item immediately
                        favViewModel.showToast(ToastItem(
                            isVisible = true,
                            type = ToastType.SUCCESS,
                            message = successToast
                        ))
                    },
                ){
                    if (ld.value.selectItems.isNotEmpty()) {
                        ld.value.selectItems.add(offer.id)
                    } else {
                        component.goToOffer(offer)
                        // set item for update
                        ld.value.updateItem.value = offer.id
                    }
                }
            }
        }
    )
}
