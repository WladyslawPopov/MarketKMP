package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.MutableState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import market.engine.core.baseFilters.SD
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.types.LotsType
import market.engine.presentation.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferFilterContent(
    isRefreshing: MutableState<Boolean>,
    filters: LD,
    baseViewModel: BaseViewModel,
    typeFilters: LotsType,
    onClose :  () -> Unit,
) {
    val listingData by remember { mutableStateOf(filters.filters) }

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

    var isExpanded1 by remember { mutableStateOf(listingData.find { it.key == "state" }?.value != "") }
    var isExpanded2 by remember {
        mutableStateOf(listingData.find { it.key in (listOf("with_sales", "without_sales")) }?.value != "")
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    val searchData = remember { mutableStateOf(
        SD(
            searchCategoryID = listingData.find { it.key == "category" }?.value?.toLongOrNull() ?: 1L,
            searchCategoryName = listingData.find { it.key == "category" }?.interpritation,
            searchParentID = listingData.find { it.key == "category" }?.value?.toLongOrNull() ?: 1L,
            searchIsLeaf = listingData.find { it.key == "category" }?.operation?.toBoolean() ?: false
        )
    ) }

    val defCat = stringResource(strings.categoryMain)

    val selectedCategory = remember { mutableStateOf(searchData.value.searchCategoryName ?: defCat) }

    val selectedType = remember { mutableStateOf(listingData.find { it.key == "sale_type" }?.interpritation ?: offersType[0].second) }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            baseViewModel.setLoading(true)
            baseViewModel.getCategories(searchData.value, LD(),true)

            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed) {
        if (scaffoldState.bottomSheetState.isCollapsed) {
            if (searchData.value.searchCategoryID != 1L) {
                listingData.find { it.key == "category" }?.value =
                    searchData.value.searchCategoryID.toString()
                listingData.find { it.key == "category" }?.interpritation =
                    searchData.value.searchCategoryName
                listingData.find { it.key == "category" }?.operation =
                    searchData.value.searchIsLeaf.toString()
            }else{
                listingData.find { it.key == "category" }?.value = ""
                listingData.find { it.key == "category" }?.interpritation = null
                listingData.find { it.key == "category" }?.operation = null
            }
            selectedCategory.value = searchData.value.searchCategoryName?: defCat
            isRefreshing.value = true
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
            CategoryContent(
                baseViewModel = baseViewModel,
                searchData = searchData.value,
                goListing = {
                    openBottomSheet.value = false
                },
                isFilters = true
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
            contentAlignment = Alignment.TopCenter
        ) {
            //Header Filters
            Row(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.TopCenter)
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

                if (isRefreshing.value || listingData.find { it.interpritation != null && it.interpritation != "" && it.key !in listOf("state", "with_sales", "without_sales") } != null) {
                    Button(
                        onClick = {
                            listingData.clear()
                            OfferFilters.clearTypeFilter(typeFilters)
                            listingData.addAll(OfferFilters.addByTypeFilter(typeFilters))
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
                    modifier = Modifier.padding(bottom = 60.dp, top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                                            listingData.find { it.key == "state" }?.value
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
                                                        listingData.find { it.key == "state" }?.value = filterKey
                                                        selectedFilterKey.value = filterKey
                                                    }
                                            ) {
                                                RadioButton(
                                                    isChecked,
                                                    {
                                                        listingData.find { it.key == "state" }?.value = filterKey
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

                                            val isChecked = remember { mutableStateOf( listingData.find { it.key == filterKey }?.interpritation != null) }
                                            val isChecked1 = remember { mutableStateOf( listingData.find { it.key == filterKey1 }?.interpritation != null) }

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
                                                            listingData.find { it.key == filterKey1 }?.interpritation = null

                                                            listingData.find { it.key == filterKey }?.interpritation =
                                                                filterText
                                                        }else {
                                                            listingData.find { it.key == filterKey }?.interpritation =
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
                                                            listingData.find { it.key == filterKey1 }?.interpritation = null

                                                            listingData.find { it.key == filterKey }?.interpritation =
                                                                filterText
                                                        }else {
                                                            listingData.find { it.key == filterKey }?.interpritation =
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
                                                            listingData.find { it.key == filterKey }?.interpritation = null
                                                            listingData.find { it.key == filterKey1 }?.interpritation =
                                                                filterText1
                                                        }else {
                                                            listingData.find { it.key == filterKey1 }?.interpritation =
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
                                                            listingData.find { it.key == filterKey }?.interpritation = null
                                                            listingData.find { it.key == filterKey1 }?.interpritation =
                                                                filterText1
                                                        }else {
                                                            listingData.find { it.key == filterKey1 }?.interpritation =
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
                            selectedCategory,
                            isRefreshing,
                            filters,
                            searchData.value,
                            openBottomSheet,
                        )
                    }

                    item {
                        val title = stringResource(strings.saleTypeParameterName)

                        Column(
                            modifier = Modifier.padding(dimens.mediumPadding)
                        ){
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            getDropdownMenu(
                                selectedType.value,
                                offersType[0].second,
                                offersTypeFilterMap,
                                onItemClick = { type ->
                                    offersType.find { it.second == type }?.let { pair ->
                                        listingData.find { it.key == "sale_type" }?.value =
                                            pair.first
                                        listingData.find { it.key == "sale_type" }?.interpritation =
                                            pair.second
                                        selectedType.value = pair.second
                                    }

                                    isRefreshing.value = true
                                },
                                onClearItem = {
                                    listingData.find { it.key == "sale_type" }?.value =
                                        ""
                                    listingData.find { it.key == "sale_type" }?.interpritation =
                                        null
                                    selectedType.value = offersType[0].second
                                    isRefreshing.value = true
                                }
                            )
                        }
                    }
                }
            }
            AcceptedPageButton(
                strings.actionAcceptFilters,
                Modifier.align(Alignment.BottomCenter)
                    .wrapContentWidth()
                    .padding(dimens.mediumPadding)
            ){
                filters.filters = listingData
                onClose()
            }
            Spacer(modifier = Modifier.height(dimens.mediumSpacer))
        }
    }
}

@Composable
fun InputsOfferFilterContent(
    activeCategory: MutableState<String>,
    isRefreshing: MutableState<Boolean>,
    listingData: LD,
    searchData: SD,
    openBottomSheet: MutableState<Boolean>,
) {
    val filters = listingData.filters
    val defCat = stringResource(strings.categoryMain)
    val idTextState = remember { mutableStateOf(filters.find { it.key == "id"}?.value ?: "") }
    val nameTextState = remember { mutableStateOf(filters.find { it.key == "search"}?.value ?: "") }
    val sellerLoginTextState = remember { mutableStateOf(filters.find { it.key == "seller_login" }?.value ?: "") }

    Column(
        modifier = Modifier.padding(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val offerId = stringResource(strings.offerIdParameterName)
            TextFieldWithState(
                label = offerId,
                textState = idTextState,
                onTextChange = { text ->
                    if (idTextState.value.isNotBlank()) {
                        filters.find { it.key == "id"}?.apply {
                            value = text
                            interpritation = "$offerId: $text"
                        }
                    }else{
                        filters.find { it.key == "id" }.let {
                            it?.value = ""
                            it?.interpritation = null
                        }
                    }

                    idTextState.value = text
                    isRefreshing.value = true
                },
                modifier = Modifier.widthIn(max = 250.dp).weight(1f),
                isNumber = true
            )

            val offerName = stringResource(strings.offerNameParameterName)
            TextFieldWithState(
                label = offerName,
                textState = nameTextState,
                onTextChange = { text ->
                    if (nameTextState.value.isNotBlank()) {
                        filters.find { filter -> filter.key == "search"}?.apply {
                            value = text
                            interpritation = "$offerName: $text"
                        }
                    }else{
                        filters.find { it.key == "search" }.let {
                            it?.value = ""
                            it?.interpritation = null
                        }
                    }
                    nameTextState.value = text
                    isRefreshing.value = true
                },
                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
            )
        }

        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sellerLogin = stringResource(strings.sellerLoginParameterName)
            if (filters.find { it.key == "seller_login" }?.value != null) {
                TextFieldWithState(
                    label = sellerLogin,
                    textState = sellerLoginTextState,
                    onTextChange = { text ->
                        if (sellerLoginTextState.value.isNotBlank()) {
                            filters.find { filter -> filter.key == "seller_login" }?.apply {
                                value = text
                                interpritation = "$sellerLogin: $text"
                            }
                        } else {
                            filters.find { it.key == "seller_login" }.let {
                                it?.value = ""
                                it?.interpritation = null
                            }
                        }
                        sellerLoginTextState.value = text
                        isRefreshing.value = true
                    },
                    modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(dimens.smallPadding)
            ){
                FilterButton(
                    modifier = Modifier.padding(dimens.smallPadding),
                    activeCategory.value,
                    color = if(searchData.searchCategoryID == 1L)
                        colors.simpleButtonColors else colors.themeButtonColors,
                    onClick = {
                        openBottomSheet.value = !openBottomSheet.value
                    },
                    onCancelClick = {
                        if (searchData.searchCategoryID != 1L) {
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifier = Modifier.size(dimens.extraSmallIconSize),
                                modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                            ) {
                                isRefreshing.value = true
                                filters.find { it.key == "category" }?.value = ""
                                filters.find { it.key == "category" }?.interpritation = null
                                searchData.clearCategory()
                                activeCategory.value = defCat
                            }
                        }
                    }
                )
            }
        }
    }
}

