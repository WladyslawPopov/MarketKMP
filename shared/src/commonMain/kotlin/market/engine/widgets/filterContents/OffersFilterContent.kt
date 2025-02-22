package market.engine.widgets.filterContents

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.MutableState
import market.engine.core.data.baseFilters.LD
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.checkboxs.RadioOptionRow
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferFilterContent(
    isRefreshing: MutableState<Boolean>,
    listingData: ArrayList<Filter>,
    baseViewModel: BaseViewModel,
    typeFilters: LotsType,
    onClose: () -> Unit,
) {
    val focusManager: FocusManager = LocalFocusManager.current

    val checkSize: () -> Boolean = {
        listingData.any { it.interpretation?.isNotBlank() == true }
    }

    val isShowClear = remember { mutableStateOf(checkSize()) }

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
        mutableStateOf(
            listingData.find { it.key == "with_sales" }?.interpretation != null ||
                listingData.find { it.key == "without_sales" }?.interpretation != null
        )
    }

    val setNewType: (String, String) -> Unit = { type, choice->
        when(type){
            "with_sales" ->{
                listingData.find { it.key == "with_sales" }?.value = choice
                listingData.find { it.key == "with_sales" }?.interpretation = myOfferExpandChoice.find { it.first == choice }?.second
                listingData.find { it.key == "without_sales" }?.value = ""
                listingData.find { it.key == "without_sales" }?.interpretation = null
            }
            "without_sales" ->{
                listingData.find { it.key == "without_sales" }?.value = choice
                listingData.find { it.key == "without_sales" }?.interpretation = myOfferExpandChoice.find { it.first == choice }?.second
                listingData.find { it.key == "with_sales" }?.value = ""
                listingData.find { it.key == "with_sales" }?.interpretation = null
            }
            "clear" ->{
                listingData.find { it.key == "with_sales" }?.value = ""
                listingData.find { it.key == "with_sales" }?.interpretation = null
                listingData.find { it.key == "without_sales" }?.value = ""
                listingData.find { it.key == "without_sales" }?.interpretation = null
            }
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    val defCat = stringResource(strings.categoryMain)

    val selectedCategory = remember { mutableStateOf(listingData.find { it.key == "category" }?.interpretation ?: defCat) }
    val selectedCategoryID = remember { mutableStateOf(listingData.find { it.key == "category" }?.value?.toLongOrNull() ?: 1L) }
    val selectedCategoryParentID = remember { mutableStateOf(listingData.find { it.key == "category" }?.value?.toLongOrNull()) }
    val selectedCategoryIsLeaf = remember { mutableStateOf(listingData.find { it.key == "category" }?.operation?.toBoolean() ?: false) }
    val selectedType = remember { mutableStateOf(listingData.find { it.key == "sale_type" }?.interpretation ?: offersType[0].second) }
    val isRefreshingFromCategories = remember { mutableStateOf(false) }


    val selectedFilterKey = remember {
        mutableStateOf(
            listingData.find { it.key == "state" }?.value
        )
    }

    val expandChoice = remember {
        mutableStateOf(
            myOfferExpandChoice.find { f->
                listingData.find { it.key == f.first && it.interpretation != null } != null
            }?.first
        )
    }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            val sd = SD(
                searchCategoryID = selectedCategoryID.value,
                searchCategoryName = selectedCategory.value,
                searchParentID = selectedCategoryParentID.value,
                searchIsLeaf = selectedCategoryIsLeaf.value
            )
            baseViewModel.setLoading(true)
            baseViewModel.getCategories(sd, LD(),true)

            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed) {
        if (scaffoldState.bottomSheetState.isCollapsed) {
            if (selectedCategoryID.value != 1L) {
                listingData.find { it.key == "category" }?.value =
                    selectedCategoryID.value.toString()
                listingData.find { it.key == "category" }?.interpretation =
                    selectedCategory.value
                listingData.find { it.key == "category" }?.operation =
                    selectedCategoryIsLeaf.value.toString()
            }else{
                listingData.find { it.key == "category" }?.value = ""
                listingData.find { it.key == "category" }?.interpretation = null
                listingData.find { it.key == "category" }?.operation = null
            }
            selectedCategory.value = selectedCategory.value
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
                searchCategoryId = selectedCategoryID,
                searchCategoryName = selectedCategory,
                searchParentID = selectedCategoryParentID,
                searchIsLeaf = selectedCategoryIsLeaf,
                isRefreshingFromFilters = isRefreshingFromCategories,
                isFilters = true,
            ){
                isRefreshing.value = true
                openBottomSheet.value = false
            }
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }.padding(dimens.smallPadding).animateContentSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            //Header Filters
            FilterContentHeaderBar(
                title = stringResource(strings.filter),
                isShowClearBtn = isShowClear.value,
                onClear = {
                    OfferFilters.clearTypeFilter(typeFilters)
                    listingData.clear()
                    listingData.addAll(OfferFilters.getByTypeFilter(typeFilters))
                    isRefreshing.value = true
                    isShowClear.value = false
                    onClose()
                },
                onClosed = {
                    onClose()
                }
            )

            LazyColumn(
                modifier = Modifier.padding(bottom = 60.dp, top = 60.dp),
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //Expands
                when(typeFilters){
                    LotsType.FAVORITES -> {
                        item {
                            ExpandableSection(
                                title = stringResource(strings.offersState),
                                isExpanded = isExpanded1,
                                onExpandChange = { isExpanded1 = !isExpanded1 },
                                content = {
                                    Column {
                                        favExpandChoice.forEach { pair ->
                                            RadioOptionRow(
                                                pair,
                                                selectedFilterKey.value
                                            ){ _, choice ->
                                                listingData.find { it.key == "state" }?.value = choice
                                                selectedFilterKey.value = choice
                                                isShowClear.value = checkSize()
                                                isRefreshing.value = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    LotsType.MYLOT_UNACTIVE -> {
                        item {
                            ExpandableSection(
                                title = stringResource(strings.offersState),
                                isExpanded = isExpanded2,
                                onExpandChange = { isExpanded2 = !isExpanded2 },
                                content = {
                                    Column {
                                        myOfferExpandChoice.forEach { pair ->
                                            RadioOptionRow(
                                                pair,
                                                expandChoice.value
                                            ){ isChecked, choice ->
                                                if (isChecked) {
                                                    setNewType("clear", choice)
                                                    expandChoice.value = null
                                                }else{
                                                    expandChoice.value = choice
                                                    setNewType(choice, choice)
                                                }

                                                isShowClear.value = checkSize()
                                                isRefreshing.value = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    else -> {}
                }

                item {
                    InputsOfferFilterContent(
                        listingData,
                        selectedCategory,
                        selectedCategoryID,
                        selectedCategoryParentID,
                        openBottomSheet,
                    ){
                        isRefreshing.value = true
                        isShowClear.value = checkSize()
                    }
                }

                item {
                    val title = stringResource(strings.saleTypeParameterName)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ){
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )

                        getDropdownMenu(
                            selectedType.value,
                            offersType[0].second,
                            offersTypeFilterMap,
                            onItemClick = { type ->
                                offersType.find { it.second == type }?.let { pair ->
                                    listingData.find { it.key == "sale_type" }?.value =
                                        pair.first
                                    listingData.find { it.key == "sale_type" }?.interpretation =
                                        pair.second
                                    selectedType.value = pair.second
                                }

                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            onClearItem = {
                                listingData.find { it.key == "sale_type" }?.value =
                                    ""
                                listingData.find { it.key == "sale_type" }?.interpretation =
                                    null
                                selectedType.value = offersType[0].second
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            }
                        )
                    }
                }
            }

            AcceptedPageButton(
                strings.actionAcceptFilters,
                Modifier.align(Alignment.BottomCenter)
                    .wrapContentWidth()
                    .padding(dimens.mediumPadding)
            ){
                onClose()
            }
        }
    }
}

@Composable
fun InputsOfferFilterContent(
    filters: List<Filter>,
    activeCategory: MutableState<String>,
    selectedCategoryID: MutableState<Long>,
    selectedParentId : MutableState<Long?>,
    openBottomSheet: MutableState<Boolean>,
    onFiltersUpdated: () -> Unit,
) {
    val defCat = stringResource(strings.categoryMain)
    val idTextState = remember { mutableStateOf(filters.find { it.key == "id"}?.value ?: "") }
    val nameTextState = remember { mutableStateOf(filters.find { it.key == "search"}?.value ?: "") }
    val sellerLoginTextState = remember { mutableStateOf(filters.find { it.key == "seller_login" }?.value ?: "") }

    val clear = remember {
        {
            filters.find { it.key == "category" }?.value = ""
            filters.find { it.key == "category" }?.interpretation = null
            activeCategory.value = defCat
            selectedCategoryID.value = 1L
            selectedParentId.value = null
            onFiltersUpdated()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
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
                            interpretation = "$offerId: $text"
                        }
                    }else{
                        filters.find { it.key == "id" }.let {
                            it?.value = ""
                            it?.interpretation = null
                        }
                    }

                    idTextState.value = text
                    onFiltersUpdated()
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
                        filters.find { filter -> filter.key == "search"}?.apply {
                            value = text
                            interpretation = "$offerName: $text"
                        }
                    }else{
                        filters.find { it.key == "search" }.let {
                            it?.value = ""
                            it?.interpretation = null
                        }
                    }
                    nameTextState.value = text
                    onFiltersUpdated()
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
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
                                    interpretation = "$sellerLogin: $text"
                                }
                            } else {
                                filters.find { it.key == "seller_login" }.let {
                                    it?.value = ""
                                    it?.interpretation = null
                                }
                            }
                            sellerLoginTextState.value = text
                            onFiltersUpdated()
                        },
                        modifier = Modifier
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                FilterButton(
                    activeCategory.value,
                    color = if (selectedCategoryID.value == 1L)
                        colors.simpleButtonColors else colors.themeButtonColors,
                    onClick = {
                        openBottomSheet.value = !openBottomSheet.value
                    },
                    onCancelClick = if(selectedCategoryID.value != 1L){
                       clear
                    }else{
                        null
                    }
                )
            }
        }
    }
}



