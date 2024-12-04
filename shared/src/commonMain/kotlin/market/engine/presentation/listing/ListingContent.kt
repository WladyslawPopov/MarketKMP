package market.engine.presentation.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.operations.operationFavorites
import market.engine.core.repositories.UserRepository
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.FilterListingContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.items.PromoOfferRowItem
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel
    val searchData = listingViewModel.listingData.searchData.subscribeAsState()
    val listingData = listingViewModel.listingData.data.subscribeAsState()
    val promoList = listingViewModel.responseOffersRecommendedInListing.collectAsState()
    val regions = listingViewModel.regionOptions.value

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val userRepository: UserRepository = KoinPlatform.getKoin().get()

    val columns =
        remember { mutableStateOf(if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2) }

    val data = remember { listingViewModel.pagingDataFlow }.collectAsLazyPagingItems()

    val noFound = @Composable {
        if (listingData.value.filters.any {it.interpritation != null && it.interpritation != "" } ||
            searchData.value.userSearch || searchData.value.searchString?.isNotEmpty() == true
        ){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                searchData.value.clear()
                listingData.value.filters.clear()
                listingData.value.filters.addAll(EmptyFilters.getEmpty())
                data.refresh()
            }
        }else {
            showNoItemLayout {
                listingData.value.resetScroll()
                listingViewModel.refresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(Unit) {
        if (listingData.value.updateItem.value != null){
            withContext(Dispatchers.IO) {
                val offer =
                    listingViewModel.getUpdatedOfferById(listingData.value.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    if (offer != null) {
                        data.itemSnapshotList.items.find { it.id == offer.id }?.isWatchedByMe =
                            offer.isWatchedByMe
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
        searchData.value,
        data = data,
        baseViewModel = listingViewModel,
        noFound = noFound,
        topBar = {
            ListingAppBar(
                searchData.value.searchCategoryName ?: stringResource(strings.categoryMain),
                modifier,
                onSearchClick = {
                    component.goToSearch()
                },
                onBeakClick = {
                    component.onBackClicked()
                }
            )
        },
        onRefresh = {
            listingData.value.resetScroll()
            columns.value = if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2
            listingViewModel.refresh()
        },
        filtersContent = { isRefreshingFromFilters, onClose ->
            FilterListingContent(
                isRefreshingFromFilters,
                listingData.value,
                regions,
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
        additionalBar = { state ->
            SwipeTabsBar(
                listingData.value,
                state,
                onRefresh = {
                    data.refresh()
                }
            )
        },
        item = { offer ->
            OfferItem(
                offer,
                isGrid = listingData.value.listingType == 1,
                baseViewModel = listingViewModel,
                onFavouriteClick = {
                    val currentOffer =
                        data[data.itemSnapshotList.items.indexOf(
                            it
                        )]
                    if (currentOffer != null) {
                        val res =
                            operationFavorites(currentOffer, listingViewModel.viewModelScope)
                        userRepository.updateUserInfo(listingViewModel.viewModelScope)
                        return@OfferItem res
                    } else {
                        return@OfferItem it.isWatchedByMe
                    }
                }
            ) {
                listingData.value.updateItem.value = offer.id
                component.goToOffer(offer)
            }
        },
        promoList = promoList.value,
        promoContent = { offer ->
            PromoOfferRowItem(
                offer
            ) {
                component.goToOffer(offer, true)
            }
        },
        isShowGrid = true,
        onSearchClick = {
            component.goToSearch()
        }
    )
}
