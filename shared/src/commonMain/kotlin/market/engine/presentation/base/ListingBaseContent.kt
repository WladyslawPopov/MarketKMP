package market.engine.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.items.ListingData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.grids.PagingList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun <T : Any>ListingBaseContent(
    columns : Int = 1,
    modifier: Modifier = Modifier,
    filtersData: MutableState<ListingData>,
    data : LazyPagingItems<T>,
    baseViewModel: BaseViewModel,
    onRefresh : () -> Unit,
    item : @Composable (T) -> Unit,
    filtersContent : @Composable (MutableState<Boolean>, onClose : () ->Unit) -> Unit,
    sortingContent : @Composable (MutableState<Boolean>, onClose : () ->Unit) -> Unit,
    additionalBar : @Composable (LazyListState) -> Unit = {},
    promoList :  ArrayList<Offer>? = null,
    promoContent : (@Composable (Offer) -> Unit)? = null,
    isShowGrid : Boolean = false,
    onSearchClick : () -> Unit = {}
){
    val searchData = filtersData.value.searchData.subscribeAsState()
    val listingData = filtersData.value.data.subscribeAsState()

    val activeFiltersType = remember { baseViewModel.activeFiltersType }

    val isHideContent = remember { mutableStateOf(baseViewModel.isHideContent.value) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }


    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = baseViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = baseViewModel.firstVisibleItemScrollOffset
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(baseViewModel.bottomSheetState.value)
    )

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    val mainViewModel : MainViewModel = koinViewModel()

    LaunchedEffect(searchData.value){
        if (searchData.value.isRefreshing && scaffoldState.bottomSheetState.isCollapsed) {
            searchData.value.isRefreshing = false
            onRefresh()
        }
    }

    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount < 1 && !isLoading.value -> {
                isHideContent.value = true
                noItem = {
                    showNoItemLayout {
                        data.refresh()
                    }
                }
            }

            refresh is LoadStateError && !isLoading.value -> {
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

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                baseViewModel.bottomSheetState.value = sheetValue
                if (sheetValue == BottomSheetValue.Collapsed) {
                    if (isRefreshingFromFilters.value) {
                        data.refresh()
                        isRefreshingFromFilters.value = false
                    }
                }
            }
    }


    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            baseViewModel.firstVisibleItemIndex = index
            baseViewModel.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(activeFiltersType.value){
        snapshotFlow {
            activeFiltersType.value
        }.collect { type ->
            if (type.isNotEmpty()) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }

            baseViewModel.activeFiltersType.value = type
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.sendEvent(
            UIMainEvent.UpdateError(error)
        )

        mainViewModel.sendEvent(UIMainEvent.UpdateNotFound(noItem))
    }

    SwipeRefreshContent(
        isRefreshing = isLoading.value,
        modifier = modifier.fillMaxSize(),
        onRefresh = {
            if (scaffoldState.bottomSheetState.isCollapsed)
                onRefresh()
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
                            filtersContent(isRefreshingFromFilters){
                                activeFiltersType.value = ""
                            }
                        }
                        "sorting" -> {
                           sortingContent(isRefreshingFromFilters){
                               activeFiltersType.value = ""
                           }
                        }
                    }
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    additionalBar(scrollState)

                    FiltersBar(
                        listingData.value,
                        searchData.value,
                        isShowGrid = isShowGrid,
                        onChangeTypeList = {
                            baseViewModel.settings.setSettingValue("listingType", it)
                            listingData.value.listingType = it
                            data.refresh()
                        },
                        onFilterClick = {
                            activeFiltersType.value = "filters"
                        },
                        onSortClick = {
                            activeFiltersType.value = "sorting"

                        },
                        onSearchClick = {
                            onSearchClick()
                        },
                        onRefresh = { data.refresh() }
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
                                    columns = columns,
                                    content = { 
                                        item(it)
                                    },
                                    promoList = promoList,
                                    promoContent = promoContent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
