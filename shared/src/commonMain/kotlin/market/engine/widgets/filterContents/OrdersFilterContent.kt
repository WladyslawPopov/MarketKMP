package market.engine.widgets.filterContents

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.DateBtn
import market.engine.widgets.dialogs.DateDialog
import market.engine.widgets.rows.FilterContentHeaderRow
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
    val focusManager: FocusManager = LocalFocusManager.current

    val checkSize: () -> Boolean = {
        filters.any { it.interpretation?.isNotBlank() == true }
    }

    val isShowClear = remember { mutableStateOf(checkSize()) }

    val sellerLoginTextState = remember { mutableStateOf(filters.find { it.key == "seller_login" }?.value ?: "") }
    val sellerIdTextState = remember { mutableStateOf(filters.find { it.key == "seller_id" }?.value ?: "") }

    val buyerIdTextState = remember { mutableStateOf(filters.find { it.key == "buyer_id" }?.value ?: "") }
    val buyerLoginTextState = remember { mutableStateOf(filters.find { it.key == "buyer_login" }?.value ?: "") }

    val idOrderTextState = remember { mutableStateOf(filters.find { it.key == "id"}?.value ?: "") }

    val idOfferTextState = remember { mutableStateOf(filters.find { it.key == "offer_id"}?.value ?: "") }
    val nameOfferTextState = remember { mutableStateOf(filters.find { it.key == "search"}?.value ?: "") }

    val from = stringResource(strings.fromAboutTimeLabel)
    val to = stringResource(strings.toAboutTimeLabel)

    val showDateDialog : MutableState<String?> = remember { mutableStateOf(null) }

    val fromThisDateTextState = remember { mutableStateOf(filters.find { it.key == "created_ts" && it.operation == "gte" }?.interpretation ?: from) }
    val toThisDateTextState = remember { mutableStateOf(filters.find { it.key == "created_ts" && it.operation == "lte" }?.interpretation ?: to) }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }.padding(dimens.smallPadding).animateContentSize(),
    ) {
        //Header Filters
        FilterContentHeaderRow(
            title = stringResource(strings.filter),
            isShowClearBtn = isShowClear.value,
            onClear = {
                DealFilters.clearTypeFilter(typeFilters)
                filters.clear()
                filters.addAll(DealFilters.getByTypeFilter(typeFilters))
                isRefreshing.value = true
                isShowClear.value = checkSize()
                onClose()
            },
            onClosed = {
                onClose()
            }
        )

        LazyColumn(
            modifier = Modifier.padding(bottom = 60.dp, top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
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
                                        interpretation = "$sellerId: $text"
                                    }
                                } else {
                                    filters.find { it.key == "seller_id" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                sellerIdTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            isNumber = true,
                            modifier = Modifier.weight(1f)
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
                                        interpretation = "$sellerLogin: $text"
                                    }
                                } else {
                                    filters.find { it.key == "seller_login" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                sellerLoginTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
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
                                        interpretation = "$buyerId: $text"
                                    }
                                } else {
                                    filters.find { it.key == "buyer_id" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                buyerIdTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            isNumber = true,
                            modifier = Modifier.weight(1f)
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
                                        interpretation = "$buyerLogin: $text"
                                    }
                                } else {
                                    filters.find { it.key == "buyer_login" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                buyerLoginTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
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
                                        interpretation = "$offerId: $text"
                                    }
                                } else {
                                    filters.find { it.key == "offer_id" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                idOfferTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            isNumber = true,
                            modifier = Modifier.weight(1f)
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
                                        interpretation = "$orderId: $text"
                                    }
                                } else {
                                    filters.find { it.key == "id" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            isNumber = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val offerTitle = stringResource(strings.searchOfferNameParameterName)

                    if (filters.find { it.key == "search" }?.value != null) {
                        TextFieldWithState(
                            label = offerTitle,
                            textState = nameOfferTextState,
                            onTextChange = { text ->
                                if (nameOfferTextState.value.isNotBlank()) {
                                    filters.find { filter -> filter.key == "search" }?.apply {
                                        value = text
                                        interpretation = "$offerTitle: $text"
                                    }
                                } else {
                                    filters.find { it.key == "search" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    painterResource(drawables.searchIcon),
                                    "",
                                    tint = colors.black,
                                    modifier = Modifier.size(dimens.smallIconSize)
                                )
                            }
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    DynamicLabel(
                        stringResource(strings.dateCreatedLabel),
                        false,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (filters.find { it.key == "created_ts" && it.operation == "gte" }?.value != null) {
                            DateBtn(
                                fromThisDateTextState.value,
                                Modifier.weight(1f)
                            ){
                                showDateDialog.value = "from"
                            }
                        }

                        if (filters.find { it.key == "created_ts" && it.operation == "lte" }?.value != null) {
                            DateBtn(
                                toThisDateTextState.value,
                                Modifier.weight(1f)
                            ){
                                showDateDialog.value = "to"
                            }
                        }
                    }
                }
            }
        }

        DateDialog(
            showDialog = showDateDialog.value != null,
            onDismiss = {
                showDateDialog.value = null
            },
            onSucceed = { futureTimeInSeconds ->
                if (showDateDialog.value == "from"){
                    fromThisDateTextState.value = "$from: ${futureTimeInSeconds.toString().convertDateWithMinutes()}"
                    filters.find { it.key == "created_ts" && it.operation == "gte" }?.value = futureTimeInSeconds.toString()
                    filters.find { it.key == "created_ts" && it.operation == "gte" }?.interpretation = fromThisDateTextState.value

                }else{
                    toThisDateTextState.value = "$to: ${futureTimeInSeconds.toString().convertDateWithMinutes()}"
                    filters.find { it.key == "created_ts" && it.operation == "lte" }?.value = futureTimeInSeconds.toString()
                    filters.find { it.key == "created_ts" && it.operation == "lte" }?.interpretation = toThisDateTextState.value
                }

                isRefreshing.value = true
                isShowClear.value = checkSize()
                showDateDialog.value = null
            }
        )

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

