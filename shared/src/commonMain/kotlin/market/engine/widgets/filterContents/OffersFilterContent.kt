package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import market.engine.core.baseFilters.LD
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Icon
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
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
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.types.LotsType
import market.engine.widgets.buttons.ActiveStringButton
import market.engine.widgets.buttons.ExpandableSection
import market.engine.widgets.lists.getDropdownMenu
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferFilterContent(
    isRefreshing: MutableState<Boolean>,
    filters: State<LD>,
    typeFilters: LotsType,
    onClose :  () -> Unit,
) {
    val listingData by remember { mutableStateOf(filters.value.filters) }

    val focusManager: FocusManager = LocalFocusManager.current

    val favExpandChoice = listOf(
        "0" to stringResource(strings.activeFilterOffers),
        "1" to stringResource(strings.inactiveFilterOffers)
    )
    val myOfferExpandChoice = listOf(
        "with_sales" to stringResource(strings.withSalesFilterOffers),
        "without_sales" to stringResource(strings.withoutSalesFilterOffers)
    )

    val offersType = listOf(
        "" to stringResource(strings.noTradeTypeFilterOffers),
        "auction" to stringResource(strings.simpleAuction),
        "buynow" to stringResource(strings.buyNow),
        "auctionwithbuynow" to stringResource(strings.auctionWithBuyNow)
    )

    val offersTypeFilterMap = listOf(
        stringResource(strings.simpleAuction),
        stringResource(strings.buyNow),
        stringResource(strings.auctionWithBuyNow)
    )

    var isExpanded1 by remember { mutableStateOf(listingData?.find { it.key == "state" }?.value != "") }
    var isExpanded2 by remember {
        mutableStateOf(listingData?.find { it.key in (listOf("with_sales", "without_sales")) }?.value != "")
    }

    val cat = remember { mutableStateOf(listingData?.find { it.key == "category" })}
    val defCat = stringResource(strings.offersCategoryParameterName)
    val activeCategory = remember {
        mutableStateOf(if(cat.value?.interpritation != null && cat.value?.interpritation != "") cat.value?.interpritation ?: defCat else defCat)
    }

    LaunchedEffect(Unit){
        cat.value = listingData?.find { it.key == "category" }
        activeCategory.value = if(cat.value?.interpritation != null && cat.value?.interpritation != "") cat.value?.interpritation ?: defCat else defCat
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

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
            CategoryFilter(
                filters, scaffoldState, isRefreshing
            ){
                openBottomSheet.value = false
            }
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
        ) {
            //Header Filters
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            onClose()
                        },
                        content = {
                            Icon(
                                painterResource(drawables.closeBtn),
                                tint = colors.black,
                                contentDescription = stringResource(strings.actionClose)
                            )
                        },
                    )

                    Text(
                        stringResource(strings.filter),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(dimens.smallPadding)
                    )
                }

                if (isRefreshing.value || listingData?.find { it.interpritation != null && it.interpritation != "" && it.key !in listOf("state", "with_sales", "without_sales") } != null) {
                    Button(
                        onClick = {
                            listingData?.clear()
                            OfferFilters.clearTypeFilter(typeFilters)
                            listingData?.addAll(OfferFilters.addByTypeFilter(typeFilters))
                            isRefreshing.value = true
                            onClose()
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

            //Expands
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.padding(bottom = 60.dp, top = 60.dp)
                ) {
                    if (typeFilters == LotsType.FAVORITES) {
                        item {
                            //Fav Filters
                            ExpandableSection(
                                title = stringResource(strings.offersState),
                                isExpanded = isExpanded1,
                                onExpandChange = { isExpanded1 = !isExpanded1 },
                                content = {
                                    val selectedFilterKey = remember {
                                        mutableStateOf(
                                            listingData?.find { it.key == "state" }?.value
                                        )
                                    }

                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 500.dp)
                                    ) {
                                        items(favExpandChoice) { filter ->
                                            val (filterKey, filterText) = filter
                                            val isChecked = selectedFilterKey.value == filterKey

                                            Row(
                                                horizontalArrangement = Arrangement.Start,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .clickable {
                                                        listingData?.find { it.key == "state" }?.value = filterKey
                                                        selectedFilterKey.value = filterKey
                                                    }
                                            ) {
                                                RadioButton(
                                                    isChecked,
                                                    {
                                                        listingData?.find { it.key == "state" }?.value = filterKey
                                                        selectedFilterKey.value = filterKey
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
                    } else {
                        if (typeFilters == LotsType.MYLOT_UNACTIVE) {
                            item {
                                //MyOffers Filters
                                ExpandableSection(
                                    title = stringResource(strings.offersState),
                                    isExpanded = isExpanded2,
                                    onExpandChange = { isExpanded2 = !isExpanded2 },
                                    content = {
                                        Column(
                                            modifier = Modifier.heightIn(max = 500.dp)
                                        ) {

                                            val filter = myOfferExpandChoice[0]
                                            val (filterKey, filterText) = filter

                                            val filter1 = myOfferExpandChoice[1]
                                            val (filterKey1, filterText1) = filter1

                                            val isChecked = remember { mutableStateOf( listingData?.find { it.key == filterKey }?.interpritation != null) }
                                            val isChecked1 = remember { mutableStateOf( listingData?.find { it.key == filterKey1 }?.interpritation != null) }

                                            Row(
                                                horizontalArrangement = Arrangement.Start,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .clickable {
                                                        isChecked.value = !isChecked.value
                                                        if (isChecked.value) {
                                                            isChecked1.value = false
                                                            listingData?.find { it.key == filterKey1 }?.interpritation = null

                                                            listingData?.find { it.key == filterKey }?.interpritation =
                                                                filterText
                                                        }else {
                                                            listingData?.find { it.key == filterKey }?.interpritation =
                                                                null
                                                        }

                                                        isRefreshing.value = true
                                                    }
                                            ) {
                                                RadioButton(
                                                    isChecked.value,
                                                    {
                                                        isChecked.value = !isChecked.value
                                                        if (isChecked.value) {
                                                            isChecked1.value = false
                                                            listingData?.find { it.key == filterKey1 }?.interpritation = null

                                                            listingData?.find { it.key == filterKey }?.interpritation =
                                                                filterText
                                                        }else {
                                                            listingData?.find { it.key == filterKey }?.interpritation =
                                                                null
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


                                            Row(
                                                horizontalArrangement = Arrangement.Start,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .clickable {
                                                        isChecked1.value = !isChecked1.value
                                                        if (isChecked1.value) {
                                                            isChecked.value = false
                                                            listingData?.find { it.key == filterKey }?.interpritation = null
                                                            listingData?.find { it.key == filterKey1 }?.interpritation =
                                                                filterText1
                                                        }else {
                                                            listingData?.find { it.key == filterKey1 }?.interpritation =
                                                                null
                                                        }

                                                        isRefreshing.value = true
                                                    }
                                            ) {
                                                RadioButton(
                                                    isChecked1.value,
                                                    {
                                                        isChecked1.value = !isChecked1.value
                                                        if (isChecked1.value) {
                                                            isChecked.value = false
                                                            listingData?.find { it.key == filterKey }?.interpritation = null
                                                            listingData?.find { it.key == filterKey1 }?.interpritation =
                                                                filterText1
                                                        }else {
                                                            listingData?.find { it.key == filterKey1 }?.interpritation =
                                                                null
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
                                                    filterText1,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                                            }

                                        }
                                    }
                                )
                            }
                        }
                    }

                    item {
                        InputsOfferFilterContent(
                            isRefreshing,
                            filters,
                        )
                    }

                    item {
                        val title = stringResource(strings.saleTypeParameterName)

                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding),
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            getDropdownMenu(
                                listingData?.find { it.key == "sale_type" }?.interpritation,
                                offersType[0].second,
                                offersTypeFilterMap,
                                onItemClick = { type ->
                                    offersType.find { it.second == type }?.let { pair ->
                                        listingData?.find { it.key == "sale_type" }?.value =
                                            pair.first
                                        listingData?.find { it.key == "sale_type" }?.interpritation =
                                            pair.second
                                    }
                                    isRefreshing.value = true
                                },
                                onClearItem = {
                                    listingData?.find { it.key == "sale_type" }?.value =
                                        ""
                                    listingData?.find { it.key == "sale_type" }?.interpritation =
                                        null
                                    isRefreshing.value = true
                                }
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding)

                        ) {
                            ActiveStringButton(
                                activeCategory.value,
                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                color = if (defCat == activeCategory.value) colors.simpleButtonColors else colors.themeButtonColors,
                                onClick = {
                                    openBottomSheet.value = true
                                },
                                onCancelClick = {
                                    val name = listingData?.find { it.key == "category" }?.interpritation
                                    if (!name.isNullOrEmpty()) {
                                        IconButton(
                                            onClick = {
                                                isRefreshing.value = true
                                                listingData?.find { it.key == "category" }?.value =
                                                    ""
                                                listingData?.find { it.key == "category" }?.interpritation =
                                                    null
                                                activeCategory.value = if(cat.value?.interpritation != null && cat.value?.interpritation != "") cat.value?.interpritation ?: defCat else defCat
                                            },
                                            content = {
                                                Icon(
                                                    painterResource(drawables.cancelIcon),
                                                    tint = colors.black,
                                                    contentDescription = stringResource(strings.actionClose)
                                                )
                                            },
                                            modifier = Modifier.size(dimens.smallIconSize)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                    .align(Alignment.BottomCenter)
            ) {
                Button(
                    onClick = {
                        filters.value.filters = listingData
                        isRefreshing.value = true
                        onClose()
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

@Composable
fun InputsOfferFilterContent(
    isRefreshing: MutableState<Boolean>,
    listingData: State<LD>
) {
    val filters = listingData.value.filters

    val idTextState = remember { mutableStateOf(filters?.find { it.key == "id"}?.value ?: "") }
    val nameTextState = remember { mutableStateOf(filters?.find { it.key == "search"}?.value ?: "") }
    val sellerLoginTextState = remember { mutableStateOf(filters?.find { it.key == "seller_login" }?.value) }

    Column(
        modifier = Modifier.padding(dimens.mediumPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val offerId = stringResource(strings.offerIdParameterName)
            TextFieldWithState(
                label = offerId,
                textState = idTextState,
                onTextChange = { text ->
                    if (idTextState.value.isNotBlank()) {
                        filters?.find { it.key == "id"}?.apply {
                            value = text
                            interpritation = "$offerId: $text"
                        }
                    }else{
                        filters?.find { it.key == "id" }.let {
                            it?.value = ""
                            it?.interpritation = null
                        }
                    }

                    idTextState.value = text
                    isRefreshing.value = true
                },
                modifier = Modifier.weight(1f),
                isNumber = true
            )

            val offerName = stringResource(strings.offerNameParameterName)
            TextFieldWithState(
                label = offerName,
                textState = nameTextState,
                onTextChange = { text ->
                    if (nameTextState.value.isNotBlank()) {

                        filters?.find { filter -> filter.key == "search"}?.apply {
                            value = text
                            interpritation = "$offerName: $text"
                        }
                    }else{
                        filters?.find { it.key == "search" }.let {
                            it?.value = ""
                            it?.interpritation = null
                        }
                    }
                    nameTextState.value = text
                    isRefreshing.value = true
                },
                modifier = Modifier.weight(1f),
            )
        }

        val sellerLogin = stringResource(strings.sellerLoginParameterName)
        if (sellerLoginTextState.value != null) {
            @Suppress("UNCHECKED_CAST")
            TextFieldWithState(
                label = sellerLogin,
                textState = (sellerLoginTextState as MutableState<String>),
                onTextChange = { text ->
                    if (sellerLoginTextState.value?.isNotBlank() == true) {
                        filters?.find { filter -> filter.key == "seller_login" }?.apply {
                            value = text
                            interpritation = "$sellerLogin: $text"
                        }
                    }else{
                        filters?.find { it.key == "seller_login" }.let {
                            it?.value = ""
                            it?.interpritation = null
                        }
                    }
                    sellerLoginTextState.value = text
                    isRefreshing.value = true
                },
                modifier = Modifier.wrapContentWidth(),
            )
        }
    }
}

