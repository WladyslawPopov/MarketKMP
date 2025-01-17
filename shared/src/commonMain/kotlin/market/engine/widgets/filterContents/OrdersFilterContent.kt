package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealType
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.textFields.TextFieldWithState
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderFilterContent(
    isRefreshing: MutableState<Boolean>,
    filters: ArrayList<Filter>,
    typeFilters: DealType,
    onClose : () -> Unit,
) {
    val listingData by remember { mutableStateOf(filters) }

    val focusManager: FocusManager = LocalFocusManager.current


    val sellerLoginTextState = remember { mutableStateOf(filters.find { it.key == "seller_login" }?.value ?: "") }
    val sellerIdTextState = remember { mutableStateOf(filters.find { it.key == "seller_id" }?.value ?: "") }

    val buyerIdTextState = remember { mutableStateOf(filters.find { it.key == "buyer_id" }?.value ?: "") }
    val buyerLoginTextState = remember { mutableStateOf(filters.find { it.key == "buyer_login" }?.value ?: "") }

    val idOrderTextState = remember { mutableStateOf(filters.find { it.key == "id"}?.value ?: "") }

    val idOfferTextState = remember { mutableStateOf(filters.find { it.key == "id"}?.value ?: "") }
    val nameOfferTextState = remember { mutableStateOf(filters.find { it.key == "search"}?.value ?: "") }

    val fromThisDateTextState = remember { mutableStateOf(filters.find { it.key == "buyer_id" }?.value ?: "") }
    val toThisDateTextState = remember { mutableStateOf(filters.find { it.key == "buyer_login" }?.value ?: "") }


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
                        DealFilters.clearTypeFilter(typeFilters)
                        listingData.addAll(DealFilters.addByTypeFilter(typeFilters))
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

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.padding(bottom = 60.dp, top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                item {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sellerId = stringResource(strings.sellerIdParameterName)
                        val sellerLogin = stringResource(strings.sellerLoginParameterName)

                        if (filters.find { it.key == "seller_id" }?.value != null) {
                            TextFieldWithState(
                                label = sellerId,
                                textState = sellerIdTextState,
                                onTextChange = { text ->
                                    if (sellerIdTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "seller_id" }?.apply {
                                            value = text
                                            interpritation = "$sellerLogin: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "seller_id" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    sellerIdTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }

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
                    }
                }
                item {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val buyerId = stringResource(strings.buyerIdParameterName)
                        val buyerLogin = stringResource(strings.buyerLoginParameterName)

                        if (filters.find { it.key == "buyer_id" }?.value != null) {
                            TextFieldWithState(
                                label = buyerId,
                                textState = buyerIdTextState,
                                onTextChange = { text ->
                                    if (buyerIdTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "buyer_id" }?.apply {
                                            value = text
                                            interpritation = "$buyerLogin: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "buyer_id" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    buyerIdTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }

                        if (filters.find { it.key == "buyer_login" }?.value != null) {
                            TextFieldWithState(
                                label = buyerLogin,
                                textState = buyerLoginTextState,
                                onTextChange = { text ->
                                    if (buyerLoginTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "buyer_login" }?.apply {
                                            value = text
                                            interpritation = "$buyerLogin: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "buyer_login" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    buyerLoginTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val offerId = stringResource(strings.offerIdParameterName)
                        val orderId = stringResource(strings.orderIdParameterName)

                        if (filters.find { it.key == "offer_id" }?.value != null) {
                            TextFieldWithState(
                                label = offerId,
                                textState = idOfferTextState,
                                onTextChange = { text ->
                                    if (idOfferTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "offer_id" }?.apply {
                                            value = text
                                            interpritation = "$offerId: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "offer_id" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    buyerIdTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }

                        if (filters.find { it.key == "id" }?.value != null) {
                            TextFieldWithState(
                                label = orderId,
                                textState = idOrderTextState,
                                onTextChange = { text ->
                                    if (idOrderTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "id" }?.apply {
                                            value = text
                                            interpritation = "$orderId: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "id" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    idOrderTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val offerTitle = stringResource(strings.searchOfferNameParameterName)

                        if (filters.find { it.key == "search" }?.value != null) {
                            TextFieldWithState(
                                label = offerTitle,
                                textState = nameOfferTextState,
                                onTextChange = { text ->
                                    if (idOfferTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "search" }?.apply {
                                            value = text
                                            interpritation = "$offerTitle: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "search" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    buyerIdTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f),
                                leadingIcon = {
                                    SmallIconButton(
                                        drawables.searchIcon,
                                        colors.black,
                                        modifier = Modifier.size(dimens.smallIconSize),
                                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                                    ){

                                    }
                                }
                            )
                        }
                    }
                }
                item {
                    DynamicLabel(
                        stringResource(strings.dateCreatedLabel),
                        false,
                    )

                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val from = stringResource(strings.fromAboutParameterName)
                        val to = stringResource(strings.toAboutParameterName)

                        if (filters.find { it.key == "created_ts" && it.operation == "gte" }?.value != null) {
                            TextFieldWithState(
                                label = from,
                                textState = fromThisDateTextState,
                                readOnly = true,
                                onTextChange = { text ->
                                    if (fromThisDateTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "created_ts" && filter.operation == "gte" }?.apply {
                                            value = text
                                            interpritation = "$from: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "created_ts" && it.operation == "gte" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    fromThisDateTextState.value = text
                                    isRefreshing.value = true
                                },
                                leadingIcon = {
                                    SmallIconButton(
                                        drawables.calendarIcon,
                                        colors.black,
                                        modifier = Modifier.size(dimens.smallIconSize),
                                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                                    ){

                                    }
                                },
                                modifier = Modifier.clickable {

                                }.widthIn(max = 250.dp).weight(1f)
                            )
                        }

                        if (filters.find { it.key == "created_ts" && it.operation == "lte" }?.value != null) {
                            TextFieldWithState(
                                label = to,
                                textState = toThisDateTextState,
                                readOnly = true,
                                onTextChange = { text ->
                                    if (toThisDateTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "created_ts" && filter.operation == "lte"}?.apply {
                                            value = text
                                            interpritation = "$from: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "created_ts" && it.operation == "lte" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    toThisDateTextState.value = text
                                    isRefreshing.value = true
                                },
                                leadingIcon = {
                                    SmallIconButton(
                                        drawables.calendarIcon,
                                        colors.black,
                                        modifier = Modifier.size(dimens.smallIconSize),
                                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                                    ){

                                    }
                                },
                                modifier = Modifier.clickable {

                                }.widthIn(max = 250.dp).weight(1f)
                            )
                        }
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
            filters.clear()
            filters.addAll(listingData)
            onClose()
        }
        Spacer(modifier = Modifier.height(dimens.mediumSpacer))
    }
}

