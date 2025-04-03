package market.engine.widgets.filterContents

import androidx.compose.runtime.MutableState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.network.networkObjects.Options
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.checkboxs.RadioOptionRow
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterListingContent(
    isRefreshing: MutableState<Boolean>,
    listingData: ArrayList<Filter>,
    regionsOptions: ArrayList<Options>,
    onClosed: () -> Unit,
) {
    val checkSize: () -> Boolean = {
        listingData.any { it.interpretation?.isNotBlank() == true }
    }

    val isShowClear = remember { mutableStateOf(checkSize()) }

    val focusManager: FocusManager = LocalFocusManager.current

    val saleTypeFilters = listOf(
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

    val regionSelected = remember { mutableStateOf(listingData.find { it.key == "region" }?.interpretation) }

    val timeNewSelected = remember {
        mutableStateOf(
            timeFilterMap.find { time ->
                time.first == listingData.find { it.key == "new" }?.value
            }?.second
        )
    }
    val timeNewWithoutRelistedSelected = remember {
        mutableStateOf(
            timeFilterMap.find { time ->
                time.first == listingData.find { it.key == "new_without_relisted" }?.value
            }?.second
        )
    }
    val timeEndingSelected = remember {
        mutableStateOf(
            timeFilterMap.find { time ->
                time.first == listingData.find { it.key == "ending" }?.value
            }?.second
        )
    }

    val scrollState = rememberLazyListState()

    val saleTypeFilterKey = remember {
        mutableStateOf(
            saleTypeFilters.find { f->
                listingData.find {
                    it.key == f.first &&
                            it.interpretation != null &&
                            it.interpretation != ""
                } != null
            }?.first
        )
    }

    var isExpanded1 by remember {
        mutableStateOf(
            checkActiveSaleType(
                saleTypeFilters,
                listingData
            ) != null
        )
    }

    var isExpanded2 by remember {
        mutableStateOf(
            checkActiveSaleType(
                specialFilters,
                listingData
            ) != null
        )
    }

    var isExpanded3 by remember { mutableStateOf(checkActiveTimeFilter(listingData) != null) }

    LaunchedEffect(isExpanded3){
        if (isExpanded3){
            scrollState.animateScrollToItem(4, 999)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }.padding(dimens.smallPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        FilterContentHeaderBar(
            title = stringResource(strings.filter),
            isShowClearBtn = isShowClear.value,
            onClear = {
                listingData.clear()
                listingData.addAll(ListingFilters.getEmpty())
                isRefreshing.value = true
                isShowClear.value = false
                onClosed()
            },
            onClosed = {
                onClosed()
            }
        )

        LazyColumnWithScrollBars(
            modifierList = Modifier.fillMaxSize().padding(bottom = 60.dp, top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = dimens.smallPadding,
            state = scrollState
        ) {
            //SaleType Filters
            item {
                ExpandableSection(
                    title = stringResource(strings.saleTypeParameterName),
                    isExpanded = isExpanded1,
                    onExpandChange = { isExpanded1 = !isExpanded1 },
                    content = {
                        Column {
                            saleTypeFilters.forEach { pair ->
                                RadioOptionRow(
                                    pair,
                                    saleTypeFilterKey.value
                                ){ isChecked, choice ->
                                    if(isChecked) {
                                        saleTypeFilterKey.value = null
                                        applyFilterLogic(
                                            "clear_saleType",
                                            "",
                                            listingData
                                        )
                                    }else{
                                        saleTypeFilterKey.value = choice
                                        applyFilterLogic(
                                            choice,
                                            saleTypeFilters.find { it.first == choice }?.second ?: "",
                                            listingData
                                        )
                                    }
                                    isShowClear.value = checkSize()
                                    isRefreshing.value = true
                                }
                            }
                        }
                    }
                )
            }
            //Special Filters
            item {
                ExpandableSection(
                    title = stringResource(strings.specialFilters),
                    isExpanded = isExpanded2,
                    onExpandChange = { isExpanded2 = !isExpanded2 },
                    content = {
                        Column {
                            specialFilters.forEach { filter ->
                                val isCheckedFilter = remember {
                                    mutableStateOf(
                                        listingData.find {
                                            it.key == filter.first &&
                                                    it.interpretation != null &&
                                                    it.interpretation != ""
                                        }?.value
                                    )
                                }

                                RadioOptionRow(
                                    filter,
                                    isCheckedFilter.value
                                ) { isChecked, choice ->
                                    if (isChecked){
                                        isCheckedFilter.value = null
                                    }else{
                                        isCheckedFilter.value = choice
                                    }
                                    applyFilterLogic(
                                        choice,
                                        specialFilters.find { it.first == choice }?.second ?: "",
                                        listingData
                                    )
                                    isShowClear.value = checkSize()
                                    isRefreshing.value = true
                                }
                            }
                        }
                    }
                )
            }
            // Region
            item {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Text(
                        text = stringResource(strings.regionParameterName),
                        style = MaterialTheme.typography.titleSmall
                    )

                    getDropdownMenu(
                        modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                        selectedText = regionSelected.value ?: stringResource(strings.chooseAction),
                        selects = regionsOptions.map { it.name.toString() },
                        onItemClick = { newRegion ->
                            listingData.find { it.key == "region" }?.value =
                                regionsOptions.find { it.name == newRegion }?.code.toString()
                            listingData.find { it.key == "region" }?.interpretation =
                                newRegion

                            regionSelected.value = newRegion

                            isShowClear.value = checkSize()
                            isRefreshing.value = true
                        },
                        onClearItem = {
                            listingData.find { it.key == "region" }?.interpretation =
                                null
                            regionSelected.value = null
                            isShowClear.value = checkSize()
                            isRefreshing.value = true
                        }
                    )
                }
            }
            // Price Filter
            item {
                PriceFilter(listingData) {
                    isShowClear.value = checkSize()
                    isRefreshing.value = true
                }
            }
            //time filter
            item {
                ExpandableSection(
                    title = stringResource(strings.timeParameterName),
                    isExpanded = isExpanded3,
                    onExpandChange = { isExpanded3 = !isExpanded3 },
                    content = {
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 800.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(colors.primaryColor)
                                .fillMaxWidth()
                                .padding(dimens.smallPadding),
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                val title = stringResource(strings.offersFor)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                    )

                                    getDropdownMenu(
                                        selectedText = timeNewSelected.value ?: stringResource(strings.chooseAction),
                                        selects = timeOptions,
                                        onItemClick = { time ->
                                            listingData.find { it.key == "new" }?.value =
                                                timeFilterMap.find { it.second == time }?.first
                                                    ?: ""
                                            listingData.find { it.key == "new" }?.interpretation =
                                                "$title $time"

                                            timeNewSelected.value = time

                                            isShowClear.value = checkSize()
                                            isRefreshing.value = true
                                        },
                                        onClearItem = {
                                            listingData.find { it.key == "new" }?.value =
                                                ""
                                            listingData.find { it.key == "new" }?.interpretation =
                                                null

                                            timeNewSelected.value = null
                                            isShowClear.value = checkSize()
                                            isRefreshing.value = true
                                        }
                                    )
                                }
                            }

                            item {
                                val title =
                                    stringResource(strings.newOffersWithoutRelistedFor)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    getDropdownMenu(
                                        selectedText = timeNewWithoutRelistedSelected.value ?: stringResource(strings.chooseAction),
                                        selects = timeOptions,
                                        onItemClick = { time ->
                                            listingData.find { it.key == "new_without_relisted" }?.value =
                                                timeFilterMap.find { it.second == time }?.first
                                                    ?: ""
                                            listingData.find { it.key == "new_without_relisted" }?.interpretation =
                                                "$title $time"

                                            timeNewWithoutRelistedSelected.value = time

                                            isShowClear.value = checkSize()
                                            isRefreshing.value = true
                                        },
                                        onClearItem = {
                                            listingData.find { it.key == "new_without_relisted" }?.value =
                                                ""
                                            listingData.find { it.key == "new_without_relisted" }?.interpretation =
                                                null

                                            timeNewWithoutRelistedSelected.value = null

                                            isShowClear.value = checkSize()
                                            isRefreshing.value = true
                                        }
                                    )
                                }
                            }

                            item {
                                val title = stringResource(strings.endingWith)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    getDropdownMenu(
                                        selectedText = timeEndingSelected.value ?: stringResource(strings.chooseAction),
                                        selects = timeOptions,
                                        onItemClick = { time ->
                                            listingData.find { it.key == "ending" }?.value =
                                                timeFilterMap.find { it.second == time }?.first
                                                    ?: ""
                                            listingData.find { it.key == "ending" }?.interpretation =
                                                "$title $time"

                                            timeEndingSelected.value = time

                                            isShowClear.value = checkSize()
                                            isRefreshing.value = true
                                        },
                                        onClearItem = {
                                            listingData.find { it.key == "ending" }?.value =
                                                ""
                                            listingData.find { it.key == "ending" }?.interpretation =
                                                null

                                            timeEndingSelected.value = null

                                            isShowClear.value = checkSize()
                                            isRefreshing.value = true
                                        }
                                    )
                                }
                            }

                            item {  }
                        }
                    }
                )
            }
        }

        AcceptedPageButton(
            strings.actionAcceptFilters,
            Modifier.wrapContentWidth().padding(dimens.smallPadding)
                .align(Alignment.BottomCenter)
        ) {
            onClosed()
        }
    }
}

fun applyFilterLogic(filterKey: String, filterName: String, filters: ArrayList<Filter>) {
    if (filters.isNotEmpty()) {
        when (filterKey) {
            "buynow" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = filterKey
                filters.find { filter -> filter.key == "sale_type" }?.interpretation = ""
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpretation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpretation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpretation = null
            }

            "auction" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = filterKey
                filters.find { filter -> filter.key == "sale_type" }?.interpretation = ""
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpretation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpretation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpretation = null
            }

            "starting_price" -> {
                filters.find { filter -> filter.key == "starting_price" }?.value = "1"
                filters.find { filter -> filter.key == "starting_price" }?.interpretation =
                    filterName

                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpretation = null
                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpretation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpretation = null
            }

            "discount_price" -> {

                filters.find { filter -> filter.key == "discount_price" }?.value = "0"
                filters.find { filter -> filter.key == "discount_price" }?.interpretation =
                    filterName

                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpretation = null
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpretation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpretation = null
            }

            "price_proposal" -> {
                filters.find { filter -> filter.key == "price_proposal" }?.value = "enabled"
                filters.find { filter -> filter.key == "price_proposal" }?.interpretation =
                    filterName

                filters.find { filter -> filter.key == "sale_type" }?.value = "buynow"
                filters.find { filter -> filter.key == "sale_type" }?.interpretation = ""

                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpretation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpretation = null
            }

            "with_video" -> {
                if (filters.find { filter -> filter.key == "with_video" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "with_video" }?.value = filterKey
                    filters.find { filter -> filter.key == "with_video" }?.interpretation =
                        filterName
                } else {
                    filters.find { filter -> filter.key == "with_video" }?.value = ""
                    filters.find { filter -> filter.key == "with_video" }?.interpretation = null
                }
            }

            "with_safe_deal" -> {
                if (filters.find { filter -> filter.key == "with_safe_deal" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "with_safe_deal" }?.value = filterKey
                    filters.find { filter -> filter.key == "with_safe_deal" }?.interpretation =
                        filterName
                } else {
                    filters.find { filter -> filter.key == "with_safe_deal" }?.value = ""
                    filters.find { filter -> filter.key == "with_safe_deal" }?.interpretation = null
                }
            }

            "promo_main_page" -> {
                if (filters.find { filter -> filter.key == "promo_main_page" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "promo_main_page" }?.value = filterKey
                    filters.find { filter -> filter.key == "promo_main_page" }?.interpretation =
                        filterName
                } else {
                    filters.find { filter -> filter.key == "promo_main_page" }?.value = ""
                    filters.find { filter -> filter.key == "promo_main_page" }?.interpretation =
                        null
                }
            }

            "clear_saleType" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpretation = null
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpretation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpretation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpretation = null
            }

            else -> {

            }
        }
    }
}

fun checkActiveSaleType(listFilters: List<Pair<String, String>>, filters: ArrayList<Filter>): String? {
    var res : String? = null

    listFilters.forEach { filter ->
        if (filter.first != "auction" && filter.first != "buynow") {
            if (filters.find { it.key == filter.first }?.interpretation != null){
                res =  filter.first
            }
        }else{
            if(filters.find { it.key == "sale_type" }?.value == filter.first
                && filters.find { it.key == "sale_type" }?.interpretation == filter.second){
                res = filter.first
            }
        }
    }
    return res
}

fun checkActiveTimeFilter(filters: ArrayList<Filter>): String? {
    var res : String? = null

    filters.find { it.key == "new" }?.interpretation?.let {
        res = it
    }
    filters.find { it.key == "new_without_relisted" }?.interpretation?.let {
        res = it
    }
    filters.find { it.key == "ending" }?.interpretation?.let {
        res = it
    }
    return res
}
