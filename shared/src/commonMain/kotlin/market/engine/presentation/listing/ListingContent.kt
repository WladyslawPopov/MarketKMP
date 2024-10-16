package market.engine.presentation.listing

import market.engine.widgets.items.ColumnItemListing
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.network.ServerErrorException
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.ListingFiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.items.GridItemListing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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

    val scrollState = rememberLazyGridState(
        initialFirstVisibleItemIndex = listingViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingViewModel.firstVisibleItemScrollOffset
    )

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(false)

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
            listingData.value.filters = EmptyFilters.getEmpty()
            component.onRefresh()
        }
    }


    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
                noItem = {
                    showNoItemLayout {
                        searchData.value.clear()
                        component.onRefresh()
                    }
                }
            }

            refresh is LoadStateError -> {
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
        onRefresh = { component.onRefresh() },
        error = error,
        noFound = noItem
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            SwipeTabsBar(
                listingData,
                scrollState,
                onRefresh = {
                    component.model.value.listingViewModel.firstVisibleItemIndex = 0
                    component.model.value.listingViewModel.firstVisibleItemScrollOffset = 0
                    component.onRefresh()
                }
            )

            ListingFiltersBar(
                listingData,
                searchData,
                onChangeTypeList = {
                    component.model.value.listingViewModel.firstVisibleItemIndex = 0
                    component.model.value.listingViewModel.firstVisibleItemScrollOffset = 0
                    component.model.value.listingViewModel.settings["listingType"] = it
                    component.onRefresh()
                },
                onFilterClick = {
                    scope.launch {
                        bottomSheetState.show()
                    }
                },
                onRefresh = { component.onRefresh() }
            )

            Box(modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
            ) {
                if (bottomSheetState.isVisible) {
                    FilterableContent(
                        bottomSheetState,
                        scope
                    )
                }

                PagingGrid(
                    state = scrollState,
                    data = data,
                    listingData = listingData,
                    content = { offer ->
                        if (listingData.value.listingType == 0) {
                            ColumnItemListing(
                                offer,
                                onFavouriteClick = {
                                    val currentOffer = data[data.itemSnapshotList.items.indexOf(it)]
                                    if (currentOffer != null) {
                                        val result = scope.async {
                                            val item = data[data.itemSnapshotList.items.indexOf(
                                                currentOffer
                                            )]
                                            if (item != null) {
                                                component.addToFavorites(item)
                                            } else {
                                                return@async currentOffer.isWatchedByMe
                                            }
                                        }
                                        result.await()
                                    }else{
                                        return@ColumnItemListing it.isWatchedByMe
                                    }
                                }
                            )
                        }else{
                            GridItemListing(
                                offer,
                                onFavouriteClick = {
                                    val currentOffer = data[data.itemSnapshotList.items.indexOf(it)]
                                    if (currentOffer != null) {
                                        val result = scope.async {
                                            val item = data[data.itemSnapshotList.items.indexOf(
                                                currentOffer
                                            )]
                                            if (item != null) {
                                                component.addToFavorites(item)
                                            } else {
                                                return@async currentOffer.isWatchedByMe
                                            }
                                        }
                                        result.await()
                                    }else{
                                        return@GridItemListing it.isWatchedByMe
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterableContent(
    bottomSheetState: SheetState,
    scope: CoroutineScope
) {
    var selectedFilters by remember { mutableStateOf(listOf<String>()) }

    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = {
            scope.launch {
                bottomSheetState.hide()
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filters", style = MaterialTheme.typography.titleSmall)

            // Пример реактивных фильтров
            CheckboxList(
                options = listOf("Filter 1", "Filter 2", "Filter 3"),
                selectedOptions = selectedFilters,
                onSelectionChange = { newSelection ->
                    selectedFilters = newSelection
                }
            )

            // Кнопка для применения фильтров и закрытия окна
            Button(onClick = {
                scope.launch {
                    bottomSheetState.hide() // Закрыть окно после применения фильтров
                }
            }) {
                Text("Apply Filters")
            }
        }
    }
}

@Composable
fun CheckboxList(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    Column {
        options.forEach { option ->
            val isChecked = selectedOptions.contains(option)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        val newSelection = if (isChecked) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    }
                )
                Text(option)
            }
        }
    }
}
