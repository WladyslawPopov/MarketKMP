package market.engine.presentation.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.network.ServerErrorException
import market.engine.core.operations.operationFavorites
import market.engine.core.repositories.UserRepository
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.presentation.main.bottomBar
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.grids.PagingList
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.PromoLotItem
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel
    val searchData = listingViewModel.listingData.searchData.subscribeAsState()
    val listingData = listingViewModel.listingData.data.subscribeAsState()
    val data = listingViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val promoList = listingViewModel.responseOffersRecommendedInListing.collectAsState()
    val regions = listingViewModel.regionOptions.value

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = listingViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingViewModel.firstVisibleItemScrollOffset
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(listingViewModel.bottomSheetState.value)
    )
    val activeFiltersType = remember { listingViewModel.activeFiltersType }

    val scope = rememberCoroutineScope()

    val isHideContent = remember { mutableStateOf(false) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val userRepository : UserRepository = KoinPlatform.getKoin().get()

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            listingViewModel.firstVisibleItemIndex = index
            listingViewModel.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(searchData){
        if (searchData.value.isRefreshing) {
            searchData.value.isRefreshing = false
            if (listingData.value.filters == null)
                listingData.value.filters = EmptyFilters.getEmpty()
            component.onRefresh()
        }
    }

    LaunchedEffect(activeFiltersType){
        snapshotFlow {
            activeFiltersType.value
        }.collect {
            listingViewModel.activeFiltersType.value = it
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                listingViewModel.bottomSheetState.value = sheetValue
                if (sheetValue == BottomSheetValue.Collapsed) {
                    if (isRefreshingFromFilters.value) {
                        component.onRefresh()
                        isRefreshingFromFilters.value = false
                    }
                }
            }
    }


    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
                isHideContent.value = true
                noItem = {
                    showNoItemLayout {
                        searchData.value.clear()
                        listingData.value.filters = EmptyFilters.getEmpty()
                        component.onRefresh()
                    }
                }
            }

            refresh is LoadStateError -> {
                isHideContent.value = true
                error = {
                    onError(
                        ServerErrorException(
                            (data.loadState.refresh as LoadStateError).error.message ?: "", ""
                        )
                    ) {
                        data.retry()
                    }
                }
            }
        }
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
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
        bottomBar = bottomBar,
        onRefresh = {
            component.onRefresh()
        },
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = !isLoading.value,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                modifier = Modifier.fillMaxSize(),
                sheetContentColor = colors.primaryColor,
                sheetBackgroundColor = colors.primaryColor,
                contentColor = colors.primaryColor,
                backgroundColor = colors.primaryColor,
                sheetPeekHeight = 0.dp,
                sheetGesturesEnabled = false,
                sheetContent = {
                    when (activeFiltersType.value) {
                        "filters" -> {
                            FilterListingContent(
                                isRefreshingFromFilters,
                                listingData,
                                scaffoldState,
                                scope,
                                regions
                            )
                        }

                        "sorting" -> {
                            SortingListingContent(
                                isRefreshingFromFilters,
                                listingData,
                                scaffoldState,
                                scope,
                            )
                        }
                    }
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    SwipeTabsBar(
                        listingData,
                        scrollState,
                        onRefresh = {
                            component.onRefresh()
                        }
                    )
                    val auctionStr = stringResource(strings.ordinaryAuction)
                    val buyNowStr = stringResource(strings.buyNow)
                    FiltersBar(
                        listingData,
                        searchData,
                        auction = auctionStr,
                        buyNow = buyNowStr,
                        isShowGrid = true,
                        onChangeTypeList = {
                            listingViewModel.settings.setSettingValue("listingType", it)
                            component.onRefresh()
                        },
                        onFilterClick = {
                            activeFiltersType.value = "filters"
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                                scaffoldState.bottomSheetState.expand()
                            }

                        },
                        onSearchClick = {
                            component.goToSearch()
                        },
                        onSortClick = {
                            activeFiltersType.value = "sorting"
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                                scaffoldState.bottomSheetState.expand()
                            }
                        },
                        onRefresh = { component.onRefresh() }
                    )

                    if (error != null) {
                        error!!()
                    } else {
                        if (noItem != null) {
                            noItem!!()
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .animateContentSize()
                            ) {
                                PagingList(
                                    state = scrollState,
                                    data = data,
                                    listingData = listingData,
                                    searchData = searchData,
                                    columns = if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2,
                                    promoList = promoList.value,
                                    fromListing = true,
                                    promoContent = { offer ->
                                        PromoLotItem(
                                            offer
                                        ) {
                                            component.goToOffer(offer, true)
                                        }
                                    },
                                    content = { offer ->
                                        if (listingData.value.listingType == 0) {
                                            ListingItem(
                                                offer,
                                                isGrid = false,
                                                onFavouriteClick = {
                                                    val currentOffer =
                                                        data[data.itemSnapshotList.items.indexOf(
                                                            it
                                                        )]
                                                    if (currentOffer != null) {
                                                        val res =
                                                            operationFavorites(currentOffer, scope)
                                                        userRepository.updateUserInfo(scope)
                                                        return@ListingItem res
                                                    } else {
                                                        return@ListingItem it.isWatchedByMe
                                                    }
                                                }
                                            ) {
                                                component.goToOffer(offer)
                                            }
                                        } else {
                                            ListingItem(offer,
                                                isGrid = true,
                                                onFavouriteClick = {
                                                    val currentOffer =
                                                        data[data.itemSnapshotList.items.indexOf(
                                                            it
                                                        )]
                                                    if (currentOffer != null) {
                                                        val res =
                                                            operationFavorites(currentOffer, scope)
                                                        userRepository.updateUserInfo(scope)
                                                        return@ListingItem res
                                                    } else {
                                                        return@ListingItem it.isWatchedByMe
                                                    }
                                                }
                                            ) {
                                                component.goToOffer(offer)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}






