package market.engine.presentation.listing

import market.engine.widgets.items.ColumnItemListing
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
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
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.LD
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Options
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.ListingFiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.buttons.ExpandableSection
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.items.GridItemListing
import market.engine.widgets.items.PriceFilter
import market.engine.widgets.lists.getDropdownMenu
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
    val regions = listingViewModel.regionOptions.value

    val scrollState = rememberLazyGridState(
        initialFirstVisibleItemIndex = listingViewModel.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingViewModel.firstVisibleItemScrollOffset
    )

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    val activeFiltersType = remember { mutableStateOf("") }

    val isHideContent = remember { mutableStateOf(false) }

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
    val isRefreshingByBottomFilters = remember { mutableStateOf(false) }
    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                if (sheetValue == SheetValue.PartiallyExpanded) {
                    if (isRefreshingByBottomFilters.value) {
                        component.onRefresh()
                        isRefreshingByBottomFilters.value = false
                    }
                }
            }
    }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null



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
        onRefresh = { component.onRefresh() },
    ){
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContainerColor = colors.grayLayout,
            modifier = Modifier.fillMaxSize(),
            sheetPeekHeight = 0.dp,
            sheetTonalElevation = 0.dp,
            sheetContent = {
                when (activeFiltersType.value) {
                    "filters" -> {
                        FilterContent(
                            isRefreshingByBottomFilters,
                            listingData,
                            scaffoldState,
                            scope,
                            regions
                        )
                    }

                    "sorting" -> {
                        SortingContent(
                            listingData,
                            scaffoldState,
                            scope,
                            onRefresh = {
                                data.refresh()
                            }
                        )
                    }
                }
            },
        ) {
            Column(modifier = Modifier.background(colors.primaryColor).fillMaxSize()) {

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
                        component.model.value.listingViewModel.settings["listingType"] =
                            it
                        component.onRefresh()
                    },
                    onFilterClick = {
                        activeFiltersType.value = "filters"
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }

                    },
                    onSortClick = {
                        activeFiltersType.value = "sorting"
                        scope.launch {
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
                    }else{
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .animateContentSize()
                        ) {
                            PagingGrid(
                                state = scrollState,
                                data = data,
                                listingData = listingData,
                                content = { offer ->
                                    if (listingData.value.listingType == 0) {
                                        ColumnItemListing(
                                            offer,
                                            onFavouriteClick = {
                                                val currentOffer =
                                                    data[data.itemSnapshotList.items.indexOf(
                                                        it
                                                    )]
                                                if (currentOffer != null) {
                                                    val result = scope.async {
                                                        val item =
                                                            data[data.itemSnapshotList.items.indexOf(
                                                                currentOffer
                                                            )]
                                                        if (item != null) {
                                                            component.addToFavorites(item)
                                                        } else {
                                                            return@async currentOffer.isWatchedByMe
                                                        }
                                                    }
                                                    result.await()
                                                } else {
                                                    return@ColumnItemListing it.isWatchedByMe
                                                }
                                            }
                                        )
                                    } else {
                                        GridItemListing(
                                            offer,
                                            onFavouriteClick = {
                                                val currentOffer =
                                                    data[data.itemSnapshotList.items.indexOf(
                                                        it
                                                    )]
                                                if (currentOffer != null) {
                                                    val result = scope.async {
                                                        val item =
                                                            data[data.itemSnapshotList.items.indexOf(
                                                                currentOffer
                                                            )]
                                                        if (item != null) {
                                                            component.addToFavorites(item)
                                                        } else {
                                                            return@async currentOffer.isWatchedByMe
                                                        }
                                                    }
                                                    result.await()
                                                } else {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterContent(
    isRefreshing: MutableState<Boolean>,
    listingData: State<LD>,
    sheetState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    regionsOptions: ArrayList<Options>,
) {
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

    val specialFilters = listOf(
        "with_video" to stringResource(strings.offerWithVideoParameterName),
        "with_safe_deal" to stringResource(strings.offerWithSafeDealParameterName),
        "promo_main_page" to stringResource(strings.allPromoOffersBtn),
    )

    val timeFilterMap = listOf(
        "1h" to stringResource(strings.oneHour),
        "2h" to stringResource(strings.twoHour),
        "3h" to stringResource(strings.threeHour),
        "4h" to stringResource(strings.fourHour),
        "5h" to stringResource(strings.fiveHour),
        "12h" to stringResource(strings.twelveHour),
        "1d" to stringResource(strings._24Hour),
        "2d" to stringResource(strings._2Days),
        "3d" to stringResource(strings._3Days),
        "4d" to stringResource(strings._4Days),
        "5d" to stringResource(strings._5Days),
        "6d" to stringResource(strings._6Days),
        "7d" to stringResource(strings._7Days)
    )

    val timeOptions = timeFilterMap.map { it.second }


    var isExpanded1 by remember { mutableStateOf(checkActiveSaleType(saleTypeFilters, listingData) != null) }
    var isExpanded2 by remember { mutableStateOf(checkActiveSaleType(specialFilters, listingData) != null) }
    var isExpanded3 by remember { mutableStateOf(checkActiveTimeFilter(listingData) != null) }

    LazyColumn {
        item {
            if(listingData.value.filters?.any { it.interpritation != null } == true) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(dimens.smallPadding)
                ) {
                    Button(
                        onClick = {
                            listingData.value.filters = EmptyFilters.getEmpty()
                            scope.launch {
                                sheetState.bottomSheetState.partialExpand()
                            }
                        },
                        content = {
                            Text(
                                stringResource(strings.clear),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.black
                            )
                        },
                        colors = colors.simpleButtonColors,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        item {
            //SaleType Filters
            ExpandableSection(
                title = stringResource(strings.saleTypeParameterName),
                isExpanded = isExpanded1,
                onExpandChange = { isExpanded1 = !isExpanded1 },
                content = {
                    val selectedFilterKey = remember { mutableStateOf(checkActiveSaleType(saleTypeFilters, listingData)) }
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
                                            applyFilterLogic("clear_saleType", "", listingData)
                                        } else {
                                            selectedFilterKey.value = filterKey
                                            applyFilterLogic(filterKey, filterText, listingData)
                                        }
                                        isRefreshing.value = true
                                    }
                            ) {
                                RadioButton(
                                    isChecked,
                                    {
                                        if (isChecked) {
                                            selectedFilterKey.value = null
                                            applyFilterLogic("clear_saleType", "", listingData)
                                        } else {
                                            selectedFilterKey.value = filterKey
                                            applyFilterLogic(filterKey, filterText, listingData)
                                        }
                                        isRefreshing.value = true
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.inactiveBottomNavIconColor,
                                        unselectedColor = colors.black
                                    )
                                )
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                                Text(filterText, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                            }
                        }
                    }
                }
            )
        }

        item {
            //Special Filters
            ExpandableSection(
                title = stringResource(strings.specialFilters),
                isExpanded = isExpanded2,
                onExpandChange = { isExpanded2 = !isExpanded2 },
                content = {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 500.dp)
                    ) {
                        items(specialFilters) { filter ->
                            val (filterKey, filterText) = filter

                            val isChecked = remember { mutableStateOf(listingData.value.filters?.find { it.key == filter.first }?.interpritation != null) }

                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        isChecked.value = !isChecked.value
                                        applyFilterLogic(filterKey, filterText, listingData)
                                        isRefreshing.value = true
                                    }
                            ) {
                                RadioButton(
                                    isChecked.value,
                                    {
                                        isChecked.value = !isChecked.value
                                        applyFilterLogic(filterKey, filterText, listingData)
                                        isRefreshing.value = true
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.inactiveBottomNavIconColor,
                                        unselectedColor = colors.black
                                    )
                                )
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                                Text(filterText, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                            }
                        }
                    }
                }
            )
        }
        item {
            // Region
            Column(
                modifier = Modifier.padding(dimens.mediumPadding).fillMaxWidth()
            ) {
                Text(
                    text = stringResource(strings.regionParameterName),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(dimens.smallPadding)
                )

                getDropdownMenu(listingData.value.filters?.find { it.key == "region" }?.interpritation, regionsOptions.map { it.name.toString() },
                    onItemClick = { newRegion ->
                        listingData.value.filters?.find { it.key == "region" }?.value = regionsOptions.find { it.name == newRegion }?.code.toString()
                        listingData.value.filters?.find { it.key == "region" }?.interpritation = newRegion
                        isRefreshing.value = true
                    },
                    onClearItem = {
                        listingData.value.filters?.find { it.key == "region" }?.interpritation = null
                        isRefreshing.value = true
                    }
                )
            }
        }

        item {
            // Price Filter
            PriceFilter(listingData){
                isRefreshing.value = true
            }
        }

        item {
            //time filter
            ExpandableSection(
                title = stringResource(strings.timeParameterName),
                isExpanded = isExpanded3,
                onExpandChange = { isExpanded3 = !isExpanded3 },
                content = {
                    LazyColumn(
                        modifier = Modifier
                        .wrapContentWidth()
                        .heightIn(max = 500.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.grayLayout)
                    ) {
                        item {
                            val title = stringResource(strings.offersFor)
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            getDropdownMenu(
                                timeFilterMap.find { time ->
                                    time.first == listingData.value.filters?.find { it.key == "new" }?.value
                                }?.second,
                                timeOptions,
                                onItemClick = { time ->
                                    listingData.value.filters?.find { it.key == "new" }?.value = timeFilterMap.find { it.second == time }?.first ?: ""
                                    listingData.value.filters?.find { it.key == "new" }?.interpritation =
                                        "$title $time"
                                    isRefreshing.value = true
                                },
                                onClearItem = {
                                    listingData.value.filters?.find { it.key == "new" }?.value = ""
                                    listingData.value.filters?.find { it.key == "new" }?.interpritation = null
                                    isRefreshing.value = true
                                }
                            )
                        }

                        item {
                            val title = stringResource(strings.newOffersWithoutRelistedFor)
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            getDropdownMenu(
                                timeFilterMap.find { time ->
                                    time.first == listingData.value.filters?.find { it.key == "new_without_relisted" }?.value
                                }?.second,
                                timeOptions,
                                onItemClick = { time ->
                                    listingData.value.filters?.find { it.key == "new_without_relisted" }?.value = timeFilterMap.find { it.second == time }?.first ?: ""
                                    listingData.value.filters?.find { it.key == "new_without_relisted" }?.interpritation =
                                        "$title $time"
                                    isRefreshing.value = true
                                },
                                onClearItem = {
                                    listingData.value.filters?.find { it.key == "new_without_relisted" }?.value = ""
                                    listingData.value.filters?.find { it.key == "new_without_relisted" }?.interpritation = null
                                    isRefreshing.value = true
                                }
                            )
                        }

                        item {
                            val title = stringResource(strings.endingWith)
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            getDropdownMenu(
                                timeFilterMap.find { time ->
                                    time.first == listingData.value.filters?.find { it.key == "ending" }?.value
                                }?.second,
                                timeOptions,
                                onItemClick = { time ->
                                    listingData.value.filters?.find { it.key == "ending" }?.value = timeFilterMap.find { it.second == time }?.first ?: ""
                                    listingData.value.filters?.find { it.key == "ending" }?.interpritation =
                                        "$title $time"
                                    isRefreshing.value = true
                                },
                                onClearItem = {
                                    listingData.value.filters?.find { it.key == "ending" }?.value = ""
                                    listingData.value.filters?.find { it.key == "ending" }?.interpritation = null
                                    isRefreshing.value = true
                                }
                            )
                        }
                    }
                }
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            sheetState.bottomSheetState.partialExpand()
                        }
                    },
                    content = {
                        Text(
                            stringResource(strings.actionAcceptFilters),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.alwaysWhite
                        )
                    },
                    colors = colors.themeButtonColors,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.align(Alignment.Center)
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

            "with_video" ->{
                if(filters.find { filter -> filter.key == "with_video" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "with_video" }?.value = filterKey
                    filters.find { filter -> filter.key == "with_video" }?.interpritation =
                        filterName
                }else{
                    filters.find { filter -> filter.key == "with_video" }?.value = ""
                    filters.find { filter -> filter.key == "with_video" }?.interpritation = null
                }
            }

            "with_safe_deal" ->{
                if(filters.find { filter -> filter.key == "with_safe_deal" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "with_safe_deal" }?.value = filterKey
                    filters.find { filter -> filter.key == "with_safe_deal" }?.interpritation =
                        filterName
                }else{
                    filters.find { filter -> filter.key == "with_safe_deal" }?.value = ""
                    filters.find { filter -> filter.key == "with_safe_deal" }?.interpritation = null
                }
            }

            "promo_main_page" ->{
                if(filters.find { filter -> filter.key == "promo_main_page" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "promo_main_page" }?.value = filterKey
                    filters.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                        filterName
                }else{
                    filters.find { filter -> filter.key == "promo_main_page" }?.value = ""
                    filters.find { filter -> filter.key == "promo_main_page" }?.interpritation = null
                }
            }

            "clear_saleType" -> {
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

fun checkActiveSaleType(listFilters: List<Pair<String, String>>, listingData: State<LD>): String? {
    val filters = listingData.value.filters
    var res : String? = null
    if (filters != null) {
        listFilters.forEach { filter ->
            if (filter.first != "auction" && filter.first != "buynow") {
                if (filters.find { it.key == filter.first }?.interpritation != null){
                    res =  filter.first
                }
            }else{
                if(filters.find { it.key == "sale_type" }?.value == filter.first
                        && filters.find { it.key == "sale_type" }?.interpritation == filter.second){
                    res = filter.first
                }
            }
        }
    }
    return res
}

fun checkActiveTimeFilter(listingData: State<LD>): String? {
    val filters = listingData.value.filters
    var res : String? = null
    if (filters != null) {
        filters.find { it.key == "new" }?.interpritation?.let {
            res = it
        }
        filters.find { it.key == "new_without_relisted" }?.interpritation?.let {
            res = it
        }
        filters.find { it.key == "ending" }?.interpritation?.let {
            res = it
        }
    }
    return res
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortingContent(
    listingData: State<LD>,
    sheetState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    onRefresh: () -> Unit,
) {
    val isRefreshing = remember { mutableStateOf(false) }

    LazyColumn {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            sheetState.bottomSheetState.hide()
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




