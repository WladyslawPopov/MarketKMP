package market.engine.presentation.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.core.operations.operationFavorites
import market.engine.core.repositories.UserRepository
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.filterContents.SortingListingContent
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

    LaunchedEffect(searchData.value) {
        if (searchData.value.isRefreshing) {
            if (listingViewModel.updateItem.value != null){
                withContext(Dispatchers.IO) {
                    val offer =
                        listingViewModel.getUpdatedOfferById(listingViewModel.updateItem.value!!)
                    withContext(Dispatchers.Main) {
                        if (offer != null) {
                            data.itemSnapshotList.items.find { it.id == offer.id }?.isWatchedByMe =
                                offer.isWatchedByMe
                        }
                        listingViewModel.updateItem.value = null
                    }
                }
            }
            searchData.value.isRefreshing = false
        }
    }

    ListingBaseContent(
        columns = columns,
        modifier = modifier,
        listingData,
        searchData,
        data = data,
        baseViewModel = listingViewModel,
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
            columns.value = if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2
            listingViewModel.refresh()
        },
        filtersContent = { isRefreshingFromFilters, onClose ->
            FilterListingContent(
                isRefreshingFromFilters,
                listingData,
                regions,
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
            ListingItem(
                offer,
                isGrid = listingData.value.listingType == 1,
                onFavouriteClick = {
                    val currentOffer =
                        data[data.itemSnapshotList.items.indexOf(
                            it
                        )]
                    if (currentOffer != null) {
                        val res =
                            operationFavorites(currentOffer, listingViewModel.viewModelScope)
                        userRepository.updateUserInfo(listingViewModel.viewModelScope)
                        return@ListingItem res
                    } else {
                        return@ListingItem it.isWatchedByMe
                    }
                }
            ) {
                listingViewModel.updateItem.value = offer.id
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
