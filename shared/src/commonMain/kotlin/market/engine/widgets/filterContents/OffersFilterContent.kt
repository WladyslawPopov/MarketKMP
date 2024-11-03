package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import market.engine.core.baseFilters.LD
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import market.engine.core.baseFilters.Filter
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.types.LotsType
import market.engine.presentation.listing.checkActiveSaleType
import market.engine.widgets.buttons.ExpandableSection
import market.engine.widgets.items.PriceFilter
import market.engine.widgets.lists.getDropdownMenu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OffersFilterContent(
    isRefreshing: MutableState<Boolean>,
    listingData: State<LD>,
    sheetState: BottomSheetScaffoldState,
    typeFilters: LotsType,
    scope: CoroutineScope,
) {
    val favExpandChoice = listOf(
        "0" to stringResource(strings.activeFilterOffers),
        "1" to stringResource(strings.inactiveFilterOffers)
    )
    val myOfferExpandChoice = listOf(
        "with_sales" to stringResource(strings.withSalesFilterOffers),
        "without_sales" to stringResource(strings.withoutSalesFilterOffers)
    )

    val offersTypeFilterMap = listOf(
        stringResource(strings.noTradeTypeFilterOffers),
        stringResource(strings.simpleAuction),
        stringResource(strings.buyNow),
        stringResource(strings.auctionWithBuyNow)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                        scope.launch {
                            sheetState.bottomSheetState.collapse()
                        }
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

            if (isRefreshing.value || listingData.value.filters?.find { it.interpritation != null } != null) {
                Button(
                    onClick = {
                        isRefreshing.value = true
                        listingData.value.filters = EmptyFilters.getEmpty()
                        scope.launch {
                            sheetState.bottomSheetState.collapse()
                        }
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
            visible = sheetState.bottomSheetState.isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            var isExpanded1 by remember { mutableStateOf(listingData.value.filters?.find { it.key == "state" }?.value != "") }
            var isExpanded2 by remember {
                mutableStateOf(
                    checkMyOfferFilters(
                        myOfferExpandChoice,
                        listingData
                    ) != null
                )
            }

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
                                        listingData.value.filters?.find { it.key == "state" }?.value
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
                                                    if (isChecked) {
                                                        listingData.value.filters?.find { it.key == "state" }?.value =
                                                            filterKey
                                                    }

                                                    isRefreshing.value =
                                                        listingData.value.filters?.find { it.interpritation != null } != null
                                                }
                                        ) {
                                            RadioButton(
                                                isChecked,
                                                {
                                                    if (isChecked) {
                                                        listingData.value.filters?.find { it.key == "state" }?.value =
                                                            filterKey
                                                    }

                                                    isRefreshing.value =
                                                        listingData.value.filters?.find { it.interpritation != null } != null
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
                }else{
                    item {
                        //MyOffers Filters
                        ExpandableSection(
                            title = stringResource(strings.offersState),
                            isExpanded = isExpanded2,
                            onExpandChange = { isExpanded2 = !isExpanded2 },
                            content = {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 500.dp)
                                ) {
                                    items(myOfferExpandChoice) { filter ->
                                        val (filterKey, filterText) = filter

                                        val isChecked =
                                            remember { mutableStateOf(listingData.value.filters?.find { it.key == filter.first }?.interpritation != null) }

                                        Row(
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(MaterialTheme.shapes.medium)
                                                .clickable {
                                                    isChecked.value = !isChecked.value

                                                    isRefreshing.value =
                                                        listingData.value.filters?.find { it.interpritation != null } != null
                                                }
                                        ) {
                                            RadioButton(
                                                isChecked.value,
                                                {
                                                    isChecked.value = !isChecked.value
                                                    isRefreshing.value =
                                                        listingData.value.filters?.find { it.interpritation != null } != null
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
                }

                item {
                    OffersFilterContent(
                        isRefreshing,
                        listingData,
                        checkFilterClear = {
                            isRefreshing.value =
                                listingData.value.filters?.find { it.interpritation != null } != null
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
                        offersTypeFilterMap[0],
                        offersTypeFilterMap,
                        onItemClick = { time ->

                        },
                        onClearItem = {

                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                .align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        sheetState.bottomSheetState.collapse()
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

@Composable
fun OffersFilterContent(
    isRefreshing: MutableState<Boolean>,
    listingData: State<LD>,
    checkFilterClear: () -> Unit
) {
    val idTextState = remember { mutableStateOf(listingData.value.filters?.find { it.key == "id"}?.value ?: "") }
    val nameTextState = remember { mutableStateOf(listingData.value.filters?.find { it.key == "search"}?.value ?: "") }
    val sellerLoginTextState = remember { mutableStateOf(listingData.value.filters?.find { it.key == "seller_login" }?.value ?: "") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TextFieldWithState(
            label = "Lot ID",
            hint = "Enter Lot ID",
            textState = idTextState,
            onTextChange = { text ->
                if (text.isNotBlank()) {
                  idTextState.value = text
                }
                checkFilterClear()
            }
        )

        TextFieldWithState(
            label = "Lot Name",
            hint = "Enter Lot Name",
            textState = nameTextState,
            onTextChange = { text ->
                if (text.isNotBlank()) {

                } else {
                }
                checkFilterClear()
            }
        )

        TextFieldWithState(
            label = "Seller Login",
            hint = "Enter Seller Login",
            textState = sellerLoginTextState,
            onTextChange = { text ->
                if (text.isNotBlank()) {

                } else {

                }
                checkFilterClear()
            }
        )
    }
}

@Composable
fun TextFieldWithState(
    label: String,
    hint: String,
    textState: MutableState<String>,
    onTextChange: (String) -> Unit
) {
    TextField(
        value = textState.value,
        onValueChange = {
            textState.value = it
            onTextChange(it)
        },
        label = { Text(label) },
        placeholder = { Text(hint) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}


fun checkMyOfferFilters(listFilters: List<Pair<String, String>>, listingData: State<LD>): String? {
    val filters = listingData.value.filters
    var res : String? = null
    if (filters != null) {
        listFilters.forEach { filter ->
            if (filters.find { it.key == filter.first }?.interpritation != null){
                res =  filter.first
            }
        }
    }
    return res
}
