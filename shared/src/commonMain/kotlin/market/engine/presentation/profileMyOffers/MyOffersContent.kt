package market.engine.presentation.profileMyOffers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.network.ServerErrorException
import market.engine.core.types.LotsType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.favorites.FavItem
import market.engine.presentation.home.DrawerContent
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.presentation.profile.ProfileDrawer
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.OfferFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.grids.PagingList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyOffersContent(
    component: MyOffersComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel

    val searchData = viewModel.listingData.searchData.subscribeAsState()
    val listingData = viewModel.listingData.data.subscribeAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = viewModel.firstVisibleItemScrollOffset
    )
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(viewModel.bottomSheetState.value)
    )
    val activeFiltersType = remember { viewModel.activeFiltersType }

    val isHideContent = remember { mutableStateOf(viewModel.isHideContent.value) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    val isLoading: State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error: (@Composable () -> Unit)? = null
    var noItem: (@Composable () -> Unit)? = null

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val mainViewModel : MainViewModel = koinViewModel()



    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.firstVisibleItemIndex = index
            viewModel.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.sendEvent(
            UIMainEvent.UpdateFloatingActionButton {}
        )

        mainViewModel.sendEvent(
            UIMainEvent.UpdateError(error)
        )

        mainViewModel.sendEvent(
            UIMainEvent.UpdateNotFound(noItem)
        )
    }

    LaunchedEffect(activeFiltersType) {
        snapshotFlow { activeFiltersType.value }.collect {
            viewModel.activeFiltersType.value = it
        }
    }

    LaunchedEffect(searchData) {
        if(data.itemCount > 0) {
            if (searchData.value.isRefreshing && scaffoldState.bottomSheetState.isCollapsed) {
                searchData.value.isRefreshing = false
                component.onRefresh()
            }
        }else{
            component.onRefresh()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                viewModel.bottomSheetState.value = sheetValue
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
                        listingData.value.filters?.clear()
                        OfferFilters.filtersFav.forEach {
                            listingData.value.filters?.add(it.copy())
                        }
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

    SwipeRefreshContent(
        isRefreshing = isLoading.value,
        modifier = modifier.fillMaxSize(),
        onRefresh = {
            if (scaffoldState.bottomSheetState.isCollapsed)
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
                            OfferFilterContent(
                                isRefreshingFromFilters,
                                listingData,
                                scaffoldState,
                                LotsType.FAVORITES,
                                coroutineScope,
                            )
                        }

                        "sorting" -> {
                            SortingListingContent(
                                isRefreshingFromFilters,
                                listingData,
                                scaffoldState,
                                coroutineScope,
                            )
                        }
                    }
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    FiltersBar(
                        listingData,
                        searchData,
                        onChangeTypeList = {
                            viewModel.settings.setSettingValue("listingType", it)
                            component.onRefresh()
                        },
                        onFilterClick = {
                            activeFiltersType.value = "filters"
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        },
                        onSortClick = {
                            activeFiltersType.value = "sorting"
                            coroutineScope.launch {
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
                                    columns = if (isBigScreen) 2 else 1,
                                    content = { offer ->
                                        FavItem(
                                            offer,
                                            onSelectionChange = { isSelect ->
                                                // Обработка выбора
                                            },
                                            onMenuClick = {
                                                // Обработка клика по меню
                                            },
                                            isSelected = false,
                                        ) {
                                            component.goToOffer(offer)
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
