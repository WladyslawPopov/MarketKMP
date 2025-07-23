package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.states.CategoryState
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.checkboxs.RadioOptionRow
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.stringResource


@Composable
fun OfferFilterContent(
    initialFilters: List<Filter>,
    filtersCategoryState: CategoryState,
    typeFilters: LotsType?,
    modifier: Modifier = Modifier,
    onClose: (newFilters : List<Filter>) -> Unit,
) {
    var listingData by remember { mutableStateOf(initialFilters.map { it.copy() }) }

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
                listingData = listingData.map { filter ->
                    when(filter.key){
                        "with_sales" -> filter.copy(
                            value = choice,
                            interpretation = myOfferExpandChoice.find { it.first == choice }?.second
                        )
                        "without_sales" -> filter.copy(value = "", interpretation = null)
                        else -> filter.copy()
                    }
                }
            }
            "without_sales" ->{
                listingData = listingData.map { filter ->
                    when(filter.key){
                        "without_sales" -> filter.copy(
                            value = choice,
                            interpretation = myOfferExpandChoice.find { it.first == choice }?.second
                        )
                        "with_sales" -> filter.copy(value = "", interpretation = null)
                        else -> filter.copy()
                    }
                }
            }
            "clear" ->{
                listingData = listingData.map {
                    when(it.key){
                        "without_sales" -> it.copy(value = "", interpretation = null)
                        "with_sales" -> it.copy(value = "", interpretation = null)
                        else -> it.copy()
                    }
                }
            }
        }
    }

    val openCategory = remember { mutableStateOf(filtersCategoryState.openCategory) }

    val viewModel = filtersCategoryState.categoryViewModel

    val defCat = stringResource(strings.categoryMain)

    AnimatedVisibility(
        visible = true,
        enter = expandVertically(),
        exit = shrinkVertically(),
    )
    {
        EdgeToEdgeScaffold(
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            topBar = {
                FilterContentHeaderBar(
                    title = stringResource(strings.filter),
                    isShowClearBtn = isShowClear.value,
                    onClear = {
                        listingData = OfferFilters.getByTypeFilter(typeFilters)
                        isShowClear.value = false
                        onClose(listingData)
                    },
                    onClosed = {
                        onClose(listingData)
                    }
                )
            }
        ) { padding ->
            CustomBottomSheet(
                initValue = openCategory.value,
                contentPadding = padding,
                onClosed = {
                    openCategory.value = false
                },
                sheetContent = {
                    CategoryContent(
                        viewModel = viewModel,
                        onClose = {
                            openCategory.value = false
                        },
                        onCompleted = {
                            val sd = viewModel.searchData.value
                            if (sd.searchCategoryID != 1L) {
                                listingData = listingData.map {
                                    if(it.key == "category"){
                                        it.copy(
                                            value = sd.searchCategoryID.toString(),
                                            interpretation = sd.searchCategoryName,
                                            operation =  sd.searchIsLeaf.toString()
                                        )
                                    }else{
                                        it.copy()
                                    }
                                }
                            }
                            openCategory.value = false
                        }
                    )
                }
            ){
                Box(
                    Modifier.padding(padding),
                )
                {
                    LazyColumnWithScrollBars(
                        modifierList = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = dimens.extraLargePadding)
                    )
                    {
                        //Expands
                        when (typeFilters) {
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
                                                        listingData.find { it.key == "state" }?.value
                                                    ) { _, choice ->
                                                        listingData = listingData.map {
                                                            if(it.key == "state") it.copy(value = choice) else it.copy()
                                                        }
                                                        isShowClear.value = checkSize()
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            LotsType.MY_LOT_INACTIVE -> {
                                item {
                                    ExpandableSection(
                                        title = stringResource(strings.offersState),
                                        isExpanded = isExpanded2,
                                        onExpandChange = { isExpanded2 = !isExpanded2 },
                                        content = {
                                            Column {
                                                val choice = remember(listingData) {
                                                    myOfferExpandChoice.find { f ->
                                                        listingData.find { it.key == f.first && it.interpretation != null } != null
                                                    }?.first
                                                }

                                                myOfferExpandChoice.forEach { pair ->
                                                    RadioOptionRow(
                                                        pair,
                                                        choice
                                                    ) { isChecked, choice ->
                                                        if (isChecked) {
                                                            setNewType("clear", choice)
                                                        } else {
                                                            setNewType(choice, choice)
                                                        }
                                                        isShowClear.value = checkSize()
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
                            Column(
                                modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            )
                            {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    val idTextState = remember(listingData) {
                                        mutableStateOf(listingData.find { it.key == "id"}?.value ?: "")
                                    }
                                    val nameTextState = remember(listingData) {
                                        mutableStateOf(listingData.find { it.key == "search"}?.value ?: "")
                                    }
                                    val offerId = stringResource(strings.offerIdParameterName)
                                    val offerName = stringResource(strings.offerNameParameterName)

                                    TextFieldWithState(
                                        label = offerId,
                                        textState = idTextState,
                                        onTextChange = { text ->
                                            listingData = if (idTextState.value.isNotBlank()) {
                                                listingData.map {
                                                    if(it.key == "id") it.copy(
                                                        value = text,
                                                        interpretation = "$offerId: $text"
                                                    ) else it.copy()
                                                }
                                            }else{
                                                listingData.map {
                                                    if(it.key == "id") it.copy(
                                                        value = "",
                                                        interpretation = null
                                                    ) else it.copy()
                                                }
                                            }
                                            isShowClear.value = checkSize()
                                        },
                                        modifier = Modifier.weight(1f),
                                        isNumber = true
                                    )

                                    TextFieldWithState(
                                        label = offerName,
                                        textState = nameTextState,
                                        onTextChange = { text ->
                                            listingData = if (nameTextState.value.isNotBlank()) {
                                                listingData.map {
                                                    if(it.key == "search") it.copy(
                                                        value = text,
                                                        interpretation = "$offerName: $text"
                                                    ) else it.copy()
                                                }
                                            }else{
                                                listingData.map {
                                                    if(it.key == "search") it.copy(
                                                        value = "",
                                                        interpretation = null
                                                    ) else it.copy()
                                                }
                                            }
                                            isShowClear.value = checkSize()
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    {
                                        val sellerLoginTextState = remember(listingData) {
                                            mutableStateOf(listingData.find { it.key == "seller_login" }?.value ?: "")
                                        }
                                        val sellerLogin = stringResource(strings.sellerLoginParameterName)

                                        if (listingData.find { it.key == "seller_login" }?.value != null) {
                                            TextFieldWithState(
                                                label = sellerLogin,
                                                textState = sellerLoginTextState,
                                                onTextChange = { text ->
                                                    listingData = if (sellerLoginTextState.value.isNotBlank()) {
                                                        listingData.map {
                                                            if(it.key == "seller_login") it.copy(
                                                                value = text,
                                                                interpretation = "$sellerLogin: $text"
                                                            ) else it.copy()
                                                        }
                                                    }else{
                                                        listingData.map {
                                                            if(it.key == "seller_login") it.copy(
                                                                value = "",
                                                                interpretation = null
                                                            ) else it.copy()
                                                        }
                                                    }
                                                    isShowClear.value = checkSize()
                                                },
                                                modifier = Modifier
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    {
                                        val selectedCategoryID = remember(listingData) {
                                            listingData.find { it.key == "category" }?.value?.toLongOrNull() ?: 1L
                                        }
                                        val selectedCategoryName = remember(listingData) {
                                            listingData.find { it.key == "category" }?.interpretation ?: defCat
                                        }

                                        FilterButton(
                                            selectedCategoryName,
                                            color = if (selectedCategoryID == 1L)
                                                colors.simpleButtonColors else colors.themeButtonColors,
                                            onClick = {
                                                openCategory.value = true
                                            },
                                            onCancelClick = if(selectedCategoryID != 1L){
                                                {
                                                    listingData = listingData.map {
                                                        if (it.key == "category") {
                                                            it.copy(
                                                                value = "",
                                                                interpretation = null,
                                                                operation = null
                                                            )
                                                        } else {
                                                            it.copy()
                                                        }
                                                    }
                                                    filtersCategoryState.categoryViewModel.resetToRoot()
                                                }
                                            }else{
                                                null
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            val title = stringResource(strings.saleTypeParameterName)

                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            )
                            {
                                val selectedType = remember(listingData) {
                                    listingData.find { it.key == "sale_type" }
                                        ?.interpretation ?: offersType[0].second
                                }

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                getDropdownMenu(
                                    selectedType,
                                    offersType[0].second,
                                    offersTypeFilterMap,
                                    modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                                    onItemClick = { type ->
                                        offersType.find { it.second == type }?.let { pair ->
                                            listingData = listingData.map {
                                                if(it.key == "sale_type") it.copy(
                                                    value = pair.first,
                                                    interpretation = pair.second
                                                ) else it.copy()
                                            }
                                        }
                                        
                                        isShowClear.value = checkSize()
                                    },
                                    onClearItem = {
                                        listingData = listingData.map {
                                            if(it.key == "sale_type") it.copy(
                                                value = "",
                                                interpretation = null
                                            ) else it.copy()
                                        }
                                        
                                        isShowClear.value = checkSize()
                                    }
                                )
                            }
                        }
                    }

                    AcceptedPageButton(
                        stringResource(strings.actionAcceptFilters),
                        Modifier.align(Alignment.BottomCenter)
                    ) {
                        onClose(listingData)
                    }
                }
            }
        }
    }
}
