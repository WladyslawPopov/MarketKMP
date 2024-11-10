package market.engine.presentation.subscriptions

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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.repositories.UserRepository
import market.engine.core.types.FavScreenType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.presentation.favorites.FavoritesAppBar
import org.koin.mp.KoinPlatform

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubscribesContent(
    modifier: Modifier,
    component: SubscribesComponent,
) {
    val modelState = component.model.subscribeAsState()
    val subViewModel = modelState.value.subViewModel
//    val searchData = subViewModel.listingData.searchData.subscribeAsState()
//    val listingData = subViewModel.listingData.data.subscribeAsState()
//    val data = subViewModel.pagingDataFlow.collectAsLazyPagingItems()

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = subViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = subViewModel.firstVisibleItemScrollOffset
    )
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(subViewModel.bottomSheetState.value)
    )
    val isHideContent = remember {mutableStateOf(false) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    val isLoading : State<Boolean> = rememberUpdatedState(false)
//    var error : (@Composable () -> Unit)? = null
//    var noItem : (@Composable () -> Unit)? = null

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val userRepository : UserRepository = KoinPlatform.getKoin().get()

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            subViewModel.firstVisibleItemIndex = index
            subViewModel.firstVisibleItemScrollOffset = offset
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                subViewModel.bottomSheetState.value = sheetValue
                if (sheetValue == BottomSheetValue.Collapsed) {
                    if (isRefreshingFromFilters.value) {
                        component.onRefresh()
                        isRefreshingFromFilters.value = false
                    }
                }
            }
    }

//    data.loadState.apply {
//        when {
//            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
//                isHideContent.value = true
//                noItem = {
//                    showNoItemLayout {
//                        searchData.value.clear()
//                        listingData.value.filters = EmptyFilters.getEmpty()
//                        component.onRefresh()
//                    }
//                }
//            }
//
//            refresh is LoadStateError -> {
//                isHideContent.value = true
//                error = {
//                    onError(
//                        ServerErrorException(
//                            (data.loadState.refresh as LoadStateError).error.message ?: "", ""
//                        )
//                    ) {
//                        data.retry()
//                    }
//                }
//            }
//        }
//    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        topBar = {
            FavoritesAppBar(
                FavScreenType.SUBSCRIBED,
                modifier
            ) { type ->
                if (type == FavScreenType.FAVORITES) {
                    component.goToFavorites()
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
//                    when (activeFiltersType.value) {
//                        "filters" -> {
//                            OffersFilterContent(
//                                isRefreshingFromFilters,
//                                listingData,
//                                scaffoldState,
//                                LotsType.FAVORITES,
//                                scope,
//                            )
//                        }
//
//                        "sorting" -> {
//                            SortingListingContent(
//                                isRefreshingFromFilters,
//                                listingData,
//                                scaffoldState,
//                                scope,
//                            )
//                        }
//                    }
                },
            ) {
                Column(modifier = Modifier.background(colors.primaryColor).fillMaxSize()) {

//                    FiltersBar(
//                        listingData,
//                        searchData,
//                        onSortClick = {
//                            scope.launch {
//                                scaffoldState.bottomSheetState.expand()
//                                scaffoldState.bottomSheetState.expand()
//                            }
//                        },
//                        onRefresh = { component.onRefresh() }
//                    )

//                    if (error != null) {
//                        error!!()
//                    } else {
//                        if (noItem != null) {
//                            noItem!!()
//                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .animateContentSize()
                            ) {

                            }
//                        }
//                    }
                }
            }
        }
    }
}
