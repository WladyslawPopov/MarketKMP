package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.MutableState
import market.engine.core.baseFilters.LD
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
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
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.network.networkObjects.Options
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.rows.PriceFilter
import market.engine.widgets.dropdown_menu.getDropdownMenu
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterListingContent(
    isRefreshing: MutableState<Boolean>,
    listingData: LD,
    regionsOptions: ArrayList<Options>,
    onClosed: () -> Unit,
) {
    val focusManager: FocusManager = LocalFocusManager.current

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

    val regionSelected = remember { mutableStateOf(listingData.filters.find { it.key == "region" }?.interpritation) }

    val timeNewSelected = remember {
        mutableStateOf(
            timeFilterMap.find { time ->
                time.first == listingData.filters.find { it.key == "new" }?.value
            }?.second
        )
    }
    val timeNewWithoutRelistedSelected = remember {
        mutableStateOf(
            timeFilterMap.find { time ->
                time.first == listingData.filters.find { it.key == "new_without_relisted" }?.value
            }?.second
        )
    }
    val timeEndingSelected = remember {
        mutableStateOf(
            timeFilterMap.find { time ->
                time.first == listingData.filters.find { it.key == "ending" }?.value
            }?.second
        )
    }

    val scrollState = rememberLazyListState()

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
        },
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.smallPadding).align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmallIconButton(
                    drawables.closeBtn,
                    colors.black,
                ){
                    onClosed()
                }

                Text(
                    stringResource(strings.filter),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(dimens.smallPadding)
                )
            }

            if (isRefreshing.value || listingData.filters.find { it.interpritation != null } != null) {
                Button(
                    onClick = {
                        isRefreshing.value = true
                        listingData.filters = EmptyFilters.getEmpty()
                        onClosed()
                    },
                    content = {
                        Text(
                            stringResource(strings.clear),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.black
                        )
                    },
                    colors = colors.simpleButtonColors
                )
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.padding(bottom = 60.dp, top = 60.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                state = scrollState
            ) {
                item {
                    //SaleType Filters
                    ExpandableSection(
                        title = stringResource(strings.saleTypeParameterName),
                        isExpanded = isExpanded1,
                        onExpandChange = { isExpanded1 = !isExpanded1 },
                        content = {
                            val selectedFilterKey = remember {
                                mutableStateOf(
                                    checkActiveSaleType(
                                        saleTypeFilters,
                                        listingData
                                    )
                                )
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
                                                    applyFilterLogic(
                                                        "clear_saleType",
                                                        "",
                                                        listingData
                                                    )
                                                } else {
                                                    selectedFilterKey.value = filterKey
                                                    applyFilterLogic(
                                                        filterKey,
                                                        filterText,
                                                        listingData
                                                    )
                                                }

                                                isRefreshing.value = true
                                            }
                                    ) {
                                        RadioButton(
                                            isChecked,
                                            {
                                                if (isChecked) {
                                                    selectedFilterKey.value = null
                                                    applyFilterLogic(
                                                        "clear_saleType",
                                                        "",
                                                        listingData
                                                    )
                                                } else {
                                                    selectedFilterKey.value = filterKey
                                                    applyFilterLogic(
                                                        filterKey,
                                                        filterText,
                                                        listingData
                                                    )
                                                }

                                                isRefreshing.value = true
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colors.inactiveBottomNavIconColor,
                                                unselectedColor = colors.black
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(dimens.smallPadding))
                                        Text(
                                            filterText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
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

                                    val isChecked =
                                        remember { mutableStateOf(listingData.filters.find { it.key == filter.first }?.interpritation != null) }

                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                isChecked.value = !isChecked.value
                                                applyFilterLogic(
                                                    filterKey,
                                                    filterText,
                                                    listingData
                                                )

                                                isRefreshing.value = true
                                            }
                                    ) {
                                        RadioButton(
                                            isChecked.value,
                                            {
                                                isChecked.value = !isChecked.value
                                                applyFilterLogic(
                                                    filterKey,
                                                    filterText,
                                                    listingData
                                                )

                                                isRefreshing.value = true
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colors.inactiveBottomNavIconColor,
                                                unselectedColor = colors.black
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(dimens.smallPadding))
                                        Text(
                                            filterText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
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
                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(strings.regionParameterName),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(dimens.smallPadding)
                        )

                        getDropdownMenu(
                            selectedText = regionSelected.value ?: stringResource(strings.chooseAction),
                            selects = regionsOptions.map { it.name.toString() },
                            onItemClick = { newRegion ->
                                listingData.filters.find { it.key == "region" }?.value =
                                    regionsOptions.find { it.name == newRegion }?.code.toString()
                                listingData.filters.find { it.key == "region" }?.interpritation =
                                    newRegion

                                regionSelected.value = newRegion

                                isRefreshing.value = true
                            },
                            onClearItem = {
                                listingData.filters.find { it.key == "region" }?.interpritation =
                                    null
                                regionSelected.value = null
                                isRefreshing.value = true
                            }
                        )
                    }
                }
                item {
                    // Price Filter
                    PriceFilter(listingData) {
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

                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(dimens.smallPadding)
                                        )

                                        getDropdownMenu(
                                            selectedText = timeNewSelected.value ?: stringResource(strings.chooseAction),
                                            selects = timeOptions,
                                            onItemClick = { time ->
                                                listingData.filters.find { it.key == "new" }?.value =
                                                    timeFilterMap.find { it.second == time }?.first
                                                        ?: ""
                                                listingData.filters.find { it.key == "new" }?.interpritation =
                                                    "$title $time"

                                                timeNewSelected.value = time

                                                isRefreshing.value = true
                                            },
                                            onClearItem = {
                                                listingData.filters.find { it.key == "new" }?.value =
                                                    ""
                                                listingData.filters.find { it.key == "new" }?.interpritation =
                                                    null

                                                timeNewSelected.value = null

                                                isRefreshing.value = true
                                            }
                                        )
                                    }
                                }

                                item {
                                    val title =
                                        stringResource(strings.newOffersWithoutRelistedFor)
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(dimens.smallPadding)
                                        )

                                        getDropdownMenu(
                                            selectedText = timeNewWithoutRelistedSelected.value ?: stringResource(strings.chooseAction),
                                            selects = timeOptions,
                                            onItemClick = { time ->
                                                listingData.filters.find { it.key == "new_without_relisted" }?.value =
                                                    timeFilterMap.find { it.second == time }?.first
                                                        ?: ""
                                                listingData.filters.find { it.key == "new_without_relisted" }?.interpritation =
                                                    "$title $time"

                                                timeNewWithoutRelistedSelected.value = time

                                                isRefreshing.value = true
                                            },
                                            onClearItem = {
                                                listingData.filters.find { it.key == "new_without_relisted" }?.value =
                                                    ""
                                                listingData.filters.find { it.key == "new_without_relisted" }?.interpritation =
                                                    null

                                                timeNewWithoutRelistedSelected.value = null

                                                isRefreshing.value = true
                                            }
                                        )
                                    }
                                }

                                item {
                                    val title = stringResource(strings.endingWith)
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(dimens.smallPadding)
                                        )

                                        getDropdownMenu(
                                            selectedText = timeEndingSelected.value ?: stringResource(strings.chooseAction),
                                            selects = timeOptions,
                                            onItemClick = { time ->
                                                listingData.filters.find { it.key == "ending" }?.value =
                                                    timeFilterMap.find { it.second == time }?.first
                                                        ?: ""
                                                listingData.filters.find { it.key == "ending" }?.interpritation =
                                                    "$title $time"

                                                timeEndingSelected.value = time

                                                isRefreshing.value = true
                                            },
                                            onClearItem = {
                                                listingData.filters.find { it.key == "ending" }?.value =
                                                    ""
                                                listingData.filters.find { it.key == "ending" }?.interpritation =
                                                    null

                                                timeEndingSelected.value = null

                                                isRefreshing.value = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        AcceptedPageButton(
            strings.actionAcceptFilters,
            Modifier.wrapContentWidth().padding(dimens.smallPadding)
                .align(Alignment.BottomCenter)
        ) {
            onClosed()
        }

        Spacer(modifier = Modifier.height(dimens.smallSpacer))
    }

}

fun applyFilterLogic(filterKey: String, filterName: String, listingData: LD) {
    val filters = listingData.filters
    if (filters.isNotEmpty()) {
        when (filterKey) {
            "buynow" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = filterKey
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = ""
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "auction" -> {
                filters.find { filter -> filter.key == "sale_type" }?.value = filterKey
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = ""
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "starting_price" -> {
                filters.find { filter -> filter.key == "starting_price" }?.value = "1"
                filters.find { filter -> filter.key == "starting_price" }?.interpritation =
                    filterName

                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "discount_price" -> {

                filters.find { filter -> filter.key == "discount_price" }?.value = "0"
                filters.find { filter -> filter.key == "discount_price" }?.interpritation =
                    filterName

                filters.find { filter -> filter.key == "sale_type" }?.value = ""
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = null
                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "price_proposal" }?.value = ""
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation = null
            }

            "price_proposal" -> {
                filters.find { filter -> filter.key == "price_proposal" }?.value = "enabled"
                filters.find { filter -> filter.key == "price_proposal" }?.interpritation =
                    filterName

                filters.find { filter -> filter.key == "sale_type" }?.value = "buynow"
                filters.find { filter -> filter.key == "sale_type" }?.interpritation = ""

                filters.find { filter -> filter.key == "starting_price" }?.value = ""
                filters.find { filter -> filter.key == "starting_price" }?.interpritation = null
                filters.find { filter -> filter.key == "discount_price" }?.value = ""
                filters.find { filter -> filter.key == "discount_price" }?.interpritation = null
            }

            "with_video" -> {
                if (filters.find { filter -> filter.key == "with_video" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "with_video" }?.value = filterKey
                    filters.find { filter -> filter.key == "with_video" }?.interpritation =
                        filterName
                } else {
                    filters.find { filter -> filter.key == "with_video" }?.value = ""
                    filters.find { filter -> filter.key == "with_video" }?.interpritation = null
                }
            }

            "with_safe_deal" -> {
                if (filters.find { filter -> filter.key == "with_safe_deal" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "with_safe_deal" }?.value = filterKey
                    filters.find { filter -> filter.key == "with_safe_deal" }?.interpritation =
                        filterName
                } else {
                    filters.find { filter -> filter.key == "with_safe_deal" }?.value = ""
                    filters.find { filter -> filter.key == "with_safe_deal" }?.interpritation = null
                }
            }

            "promo_main_page" -> {
                if (filters.find { filter -> filter.key == "promo_main_page" }?.value != filterKey) {
                    filters.find { filter -> filter.key == "promo_main_page" }?.value = filterKey
                    filters.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                        filterName
                } else {
                    filters.find { filter -> filter.key == "promo_main_page" }?.value = ""
                    filters.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                        null
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

fun checkActiveSaleType(listFilters: List<Pair<String, String>>, listingData: LD): String? {
    val filters = listingData.filters
    var res : String? = null

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
    return res
}

fun checkActiveTimeFilter(listingData: LD): String? {
    val filters = listingData.filters
    var res : String? = null

    filters.find { it.key == "new" }?.interpritation?.let {
        res = it
    }
    filters.find { it.key == "new_without_relisted" }?.interpritation?.let {
        res = it
    }
    filters.find { it.key == "ending" }?.interpritation?.let {
        res = it
    }
    return res
}
