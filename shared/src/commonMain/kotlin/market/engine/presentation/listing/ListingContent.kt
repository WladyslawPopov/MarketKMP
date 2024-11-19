package market.engine.presentation.listing


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.operations.operationFavorites
import market.engine.core.repositories.UserRepository
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.PromoLotItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.mp.KoinPlatform

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel
    val mainViewModel : MainViewModel = koinViewModel()
    val filtersData = listingViewModel.listingData


    val searchData = listingViewModel.listingData.value.searchData.subscribeAsState()
    val listingData = listingViewModel.listingData.value.data.subscribeAsState()
    val data = listingViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val promoList = listingViewModel.responseOffersRecommendedInListing.collectAsState()
    val regions = listingViewModel.regionOptions.value

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val userRepository : UserRepository = KoinPlatform.getKoin().get()


    LaunchedEffect(Unit) {
        mainViewModel.sendEvent(
            UIMainEvent.UpdateTopBar {
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
            }
        )

        mainViewModel.sendEvent(
            UIMainEvent.UpdateFloatingActionButton {}
        )

    }

    ListingBaseContent(
        columns = if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2,
        modifier = modifier,
        filtersData = filtersData,
        data = data,
        baseViewModel = listingViewModel,
        onRefresh = {
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
                component.goToOffer(offer)
            }
        },
        promoList = promoList.value,
        promoContent = { offer ->
            PromoLotItem(
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






