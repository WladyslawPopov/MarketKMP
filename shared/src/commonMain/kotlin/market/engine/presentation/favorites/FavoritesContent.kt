package market.engine.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.network.ServerErrorException
import market.engine.core.repositories.UserRepository
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.filterContents.OffersFilterContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.grids.PagingList
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoritesContent(
    modifier: Modifier,
    component: FavoritesComponent,
) {
    val modelState = component.model.subscribeAsState()
    val favViewModel = modelState.value.favViewModel
    val searchData = favViewModel.listingData.searchData.subscribeAsState()
    val listingData = favViewModel.listingData.data.subscribeAsState()
    val data = favViewModel.pagingDataFlow.collectAsLazyPagingItems()

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = favViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = favViewModel.firstVisibleItemScrollOffset
    )
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(favViewModel.bottomSheetState.value)
    )
    val selectFav = remember { favViewModel.selectFav }
    val activeFiltersType = remember { favViewModel.activeFiltersType }

    val isHideContent = remember {mutableStateOf(false) }
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
            favViewModel.firstVisibleItemIndex = index
            favViewModel.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(activeFiltersType){
        snapshotFlow {
            activeFiltersType.value
        }.collect {
            favViewModel.activeFiltersType.value = it
        }
    }

    LaunchedEffect(selectFav){
        snapshotFlow {
            selectFav
        }.collect {
            favViewModel.selectFav = it
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

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                favViewModel.bottomSheetState.value = sheetValue
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
            FavoritesAppBar(
                modifier
            ) { type ->
                if (type == FavScreenType.SUBSCRIBED) {
                    component.goToSubscribes()
                }
            }
        },
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
                sheetBackgroundColor = colors.primaryColor,
                sheetPeekHeight = 0.dp,
                sheetGesturesEnabled = false,
                sheetContent = {
                    when (activeFiltersType.value) {
                        "filters" -> {
                            OffersFilterContent(
                                isRefreshingFromFilters,
                                listingData,
                                scaffoldState,
                                LotsType.FAVORITES,
                                scope,
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
                Column(modifier = Modifier.background(colors.primaryColor).fillMaxSize()) {

                    if (selectFav.isNotEmpty()) {
                        DeletePanel(
                            selectFav.size,
                            scrollState = scrollState,
                            onCancel = {
                                selectFav.clear()
                            },
                            onDelete = {
                                selectFav.clear()
                            }
                        )
                    }

                    FiltersBar(
                        listingData,
                        searchData,
                        onChangeTypeList = {
                            favViewModel.settings.setSettingValue("listingType", it)
                            component.onRefresh()
                        },
                        onFilterClick = {
                            activeFiltersType.value = "filters"
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                                scaffoldState.bottomSheetState.expand()
                            }

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
                                    columns = if (isBigScreen) 2 else 1,
                                    content = { offer ->
                                        FavItem(
                                            offer,
                                            onSelectionChange = { isSelect ->
                                                if (isSelect) {
                                                    selectFav.add(offer.id)
                                                } else {
                                                    selectFav.remove(offer.id)
                                                }
                                            },
                                            onMenuClick = {

                                            },
                                            isSelected = selectFav.contains(offer.id),
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
