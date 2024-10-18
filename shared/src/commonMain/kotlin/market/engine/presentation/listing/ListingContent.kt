package market.engine.presentation.listing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import market.engine.widgets.items.ColumnItemListing
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
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
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.LD
import market.engine.core.network.ServerErrorException
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.ListingFiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.buttons.ExpandableSection
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.items.GridItemListing
import market.engine.widgets.texts.TitleText
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
    val bottomSheetState = rememberModalBottomSheetState(true)

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
                    component.onRefresh()
                }
            )

            ListingFiltersBar(
                listingData,
                searchData,
                onChangeTypeList = {
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
                    FilterContent(
                        listingData,
                        bottomSheetState,
                        scope
                    ) {
                        data.refresh()
                    }
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


fun applyFilterLogic(filterKey: String, filterName: String, listingData: State<LD>) {
    val filters = listingData.value.filters
    if (filters != null) {
        when (filterKey) {
            "buynow" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = filterKey
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = filterName
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "auction" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = filterKey
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = filterName
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "starting_price" -> {
                filters.find { filter -> filter.key == "starting_price" }?.value = "1"
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = filterName

                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "discount_price" -> {

                filters.find { filter -> filter.key == "discount_price" }?.value = "0"
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = filterName

                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = null
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "price_proposal" -> {
                filters.find { filter -> filter.key == "price_proposal" }?.value = "enabled"
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = filterName

                filters.find { filter -> filter.key == "sale_type" }?.value = "buynow"
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = ""

                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
            }

            "clear" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = null
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            else -> {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterContent(
    listingData: State<LD>,
    bottomSheetState: SheetState,
    scope: CoroutineScope,
    onRefresh: () -> Unit
) {
    var isExpanded1 by remember { mutableStateOf(!(listingData.value.filters?.filter { it.interpritation != null && it.value != "" }.isNullOrEmpty())) }
    var isExpanded2 by remember { mutableStateOf(false) }
    var isExpanded3 by remember { mutableStateOf(false) }

    val selectedFilterKey = remember { mutableStateOf<String?>(null) }

    val isRefreshing = remember { mutableStateOf(false) }

    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = {
            scope.launch {
                bottomSheetState.hide()
                if (isRefreshing.value)
                    onRefresh()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(modifier = Modifier.padding(bottom = 60.dp)) {

                    ExpandableSection(
                        title = stringResource(strings.saleTypeParameterName),
                        isExpanded = isExpanded1,
                        onExpandChange = { isExpanded1 = !isExpanded1 },
                        content = {

                            val saleTypeFilters = listOf(
                                "buynow" to stringResource(strings.buyNow),
                                "auction" to stringResource(strings.ordinaryAuction),
                                "starting_price" to stringResource(strings.saleTypeNameFromOne) + " 1 ${
                                    stringResource(
                                        strings.currencyCode
                                    )
                                }",
                                "discount_price" to stringResource(strings.discountPriceParameterName),
                                "price_proposal" to stringResource(strings.proposalTitle)
                            )

                            saleTypeFilters.forEach { filter ->
                                val isF = if (filter.first != "auction" && filter.first != "buynow")
                                        listingData.value.filters?.find { it.key == filter.first }?.interpritation != null
                                    else (listingData.value.filters?.find { it.key == "sale_type" }?.value == filter.first
                                            && listingData.value.filters?.find { it.key == "sale_type" }?.interpritation == filter.second)

                                        if (isF)
                                            selectedFilterKey.value = filter.first
                            }

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 500.dp)
                            ) {
                                items(saleTypeFilters) { filter ->
                                    val (filterKey, filterText) = filter
                                    val isChecked = selectedFilterKey.value == filterKey

                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                if (isChecked) {
                                                    selectedFilterKey.value = null
                                                    applyFilterLogic("clear", "", listingData)
                                                } else {
                                                    selectedFilterKey.value = filterKey
                                                    applyFilterLogic(filterKey, filterText, listingData)
                                                }
                                                isRefreshing.value = true
                                            }
                                    ) {
                                        RadioButton(
                                            isChecked,
                                            {},
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colors.inactiveBottomNavIconColor,
                                                unselectedColor = colors.black
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(dimens.smallPadding))
                                        Text(filterText)
                                        Spacer(modifier = Modifier.width(dimens.smallPadding))
                                    }
                                }
                            }
                        }
                    )

                    // Второй ExpandableLayout
                    ExpandableSection(
                        title = "Expandable 2",
                        isExpanded = isExpanded2,
                        onExpandChange = { isExpanded2 = !isExpanded2 },
                        content = {
                            // Контент для второго Expandable
                            Text("Expandable content 2")
                        }
                    )

                    // Region Parameter
                    Text(
                        text = "Region Parameter",
                        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Dropdown заменяющий Spinner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        getDropdownMenu()
                    }

                    // Price Filter
                    Text(
                        text = "Price",
                        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Ценовые фильтры
                    Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            label = { Text("From") },
                            singleLine = true
                        )
                        Text(
                            text = "-",
                            modifier = Modifier.padding(horizontal = 10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            label = { Text("To") },
                            singleLine = true
                        )
                    }

                    // Третий ExpandableLayout
                    ExpandableSection(
                        title = "Expandable 3",
                        isExpanded = isExpanded3,
                        onExpandChange = { isExpanded3 = !isExpanded3 },
                        content = {
                            // Контент для третьего Expandable
                            Text("Expandable content 3")
                        }
                    )
                }
            }
            item{
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                                if (isRefreshing.value)
                                    onRefresh()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text("Apply Filters")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun getDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf(items[0]) }

    Box(
        modifier = Modifier.padding(16.dp)
    ) {

        Button(onClick = { expanded = true }) {
            Text(selectedOption)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        selectedOption = option
                        expanded = false
                    },
                    text = { Text(option) }
                )
            }
        }
    }
}
