package market.engine.widgets.filterContents.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
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
import market.engine.core.data.filtersObjects.DealFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealType
import market.engine.core.utils.convertDateWithMinutes
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.DateBtn
import market.engine.widgets.dialogs.DateDialog
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.TextFieldWithState
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderFilterContent(
    initialFilters: List<Filter>,
    typeFilters: DealType,
    modifier: Modifier = Modifier,
    onClose: (newFilters : List<Filter>) -> Unit,
) {
    var filters by remember { mutableStateOf(initialFilters.map { it.copy() }) }

    val focusManager: FocusManager = LocalFocusManager.current

    val checkSize: () -> Boolean = {
        filters.any { it.interpretation?.isNotBlank() == true }
    }

    val isShowClear = remember { mutableStateOf(checkSize()) }


    AnimatedVisibility(
        visible = true,
        enter = expandVertically(),
        exit = shrinkVertically(),
    )
    {
        EdgeToEdgeScaffold(
            modifier = modifier
                .fillMaxSize()
                .padding(dimens.smallPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            isLoading = false,
            topBar = {
                FilterContentHeaderBar(
                    title = stringResource(strings.filter),
                    isShowClearBtn = isShowClear.value,
                    onClear = {
                        filters = DealFilters.getByTypeFilter(typeFilters)
                        isShowClear.value = checkSize()
                        onClose(filters)
                    },
                    onClosed = {
                        onClose(filters)
                    }
                )
            },
        ) { contentPadding ->
            Box(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
                LazyColumnWithScrollBars(
                    listModifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                )
                {
                    item {
                        Row(
                            modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sellerId = stringResource(strings.sellerIdParameterName)
                            val sellerLogin = stringResource(strings.sellerLoginParameterName)
                            val sellerLoginTextState =
                                remember(filters) {
                                    mutableStateOf(filters.find { it.key == "seller_login" }?.value ?: "")
                                }
                            val sellerIdTextState =
                                remember(filters) {
                                    mutableStateOf(filters.find { it.key == "seller_id" }?.value ?: "")
                                }

                            if (filters.find { it.key == "seller_id" }?.value != null) {
                                TextFieldWithState(
                                    label = sellerId,
                                    textState = sellerIdTextState,
                                    onTextChange = { text ->
                                        if (sellerIdTextState.value.isNotBlank()) {
                                            filters.find { filter -> filter.key == "seller_id" }
                                                ?.apply {
                                                    value = text
                                                    interpretation = "$sellerId: $text"
                                                }
                                        } else {
                                            filters.find { it.key == "seller_id" }.let {
                                                it?.value = ""
                                                it?.interpretation = null
                                            }
                                        }
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
                                            filters.find { filter -> filter.key == "seller_login" }
                                                ?.apply {
                                                    value = text
                                                    interpretation = "$sellerLogin: $text"
                                                }
                                        } else {
                                            filters.find { it.key == "seller_login" }.let {
                                                it?.value = ""
                                                it?.interpretation = null
                                            }
                                        }
                                        isShowClear.value = checkSize()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val buyerId = stringResource(strings.buyerIdParameterName)
                            val buyerLogin = stringResource(strings.buyerLoginParameterName)

                            val buyerIdTextState =
                                remember(filters) {
                                    mutableStateOf(filters.find { it.key == "buyer_id" }?.value ?: "")
                                }
                            val buyerLoginTextState =
                                remember(filters) {
                                    mutableStateOf(filters.find { it.key == "buyer_login" }?.value ?: "")
                                }

                            if (filters.find { it.key == "buyer_id" }?.value != null) {
                                TextFieldWithState(
                                    label = buyerId,
                                    textState = buyerIdTextState,
                                    onTextChange = { text ->
                                        if (buyerIdTextState.value.isNotBlank()) {
                                            filters.find { filter -> filter.key == "buyer_id" }
                                                ?.apply {
                                                    value = text
                                                    interpretation = "$buyerId: $text"
                                                }
                                        } else {
                                            filters.find { it.key == "buyer_id" }.let {
                                                it?.value = ""
                                                it?.interpretation = null
                                            }
                                        }
                                        
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
                                            filters.find { filter -> filter.key == "buyer_login" }
                                                ?.apply {
                                                    value = text
                                                    interpretation = "$buyerLogin: $text"
                                                }
                                        } else {
                                            filters.find { it.key == "buyer_login" }.let {
                                                it?.value = ""
                                                it?.interpretation = null
                                            }
                                        }

                                        isShowClear.value = checkSize()
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val offerId = stringResource(strings.offerIdParameterName)
                            val orderId = stringResource(strings.orderIdParameterName)
                            val idOrderTextState = remember(filters) {
                                mutableStateOf(filters.find { it.key == "id" }?.value ?: "")
                            }

                            val idOfferTextState =
                                remember(filters) {
                                    mutableStateOf(filters.find { it.key == "offer_id" }?.value ?: "")
                                }

                            if (filters.find { it.key == "offer_id" }?.value != null) {
                                TextFieldWithState(
                                    label = offerId,
                                    textState = idOfferTextState,
                                    onTextChange = { text ->
                                        if (idOfferTextState.value.isNotBlank()) {
                                            filters.find { filter -> filter.key == "offer_id" }
                                                ?.apply {
                                                    value = text
                                                    interpretation = "$offerId: $text"
                                                }
                                        } else {
                                            filters.find { it.key == "offer_id" }.let {
                                                it?.value = ""
                                                it?.interpretation = null
                                            }
                                        }
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
                            modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val offerTitle = stringResource(strings.searchOfferNameParameterName)
                            val nameOfferTextState =
                                remember(filters) {
                                    mutableStateOf(filters.find { it.key == "search" }?.value ?: "")
                                }

                            if (filters.find { it.key == "search" }?.value != null) {
                                TextFieldWithState(
                                    label = offerTitle,
                                    textState = nameOfferTextState,
                                    onTextChange = { text ->
                                        if (nameOfferTextState.value.isNotBlank()) {
                                            filters.find { filter -> filter.key == "search" }
                                                ?.apply {
                                                    value = text
                                                    interpretation = "$offerTitle: $text"
                                                }
                                        } else {
                                            filters.find { it.key == "search" }.let {
                                                it?.value = ""
                                                it?.interpretation = null
                                            }
                                        }
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
                        val from = stringResource(strings.fromAboutTimeLabel)
                        val to = stringResource(strings.toAboutTimeLabel)

                        val showDateDialog: MutableState<String?> = remember { mutableStateOf(null) }

                        val fromThisDateTextState = remember(filters) {
                            mutableStateOf(
                                filters.find { it.key == "created_ts" && it.operation == "gte" }?.interpretation
                                    ?: from
                            )
                        }
                        val toThisDateTextState = remember(filters) {
                            mutableStateOf(
                                filters.find { it.key == "created_ts" && it.operation == "lte" }?.interpretation
                                    ?: to
                            )
                        }

                        Column(
                            modifier = Modifier.widthIn(min = 300.dp, max = 500.dp)
                                .padding(dimens.mediumPadding),
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            horizontalAlignment = Alignment.Start
                        )
                        {
                            DynamicLabel(
                                stringResource(strings.dateCreatedLabel),
                                false,
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (filters.find { it.key == "created_ts" && it.operation == "gte" }?.value != null) {
                                    DateBtn(
                                        fromThisDateTextState.value,
                                        Modifier.weight(1f)
                                    ) {
                                        showDateDialog.value = "from"
                                    }
                                }

                                if (filters.find { it.key == "created_ts" && it.operation == "lte" }?.value != null) {
                                    DateBtn(
                                        toThisDateTextState.value,
                                        Modifier.weight(1f)
                                    ) {
                                        showDateDialog.value = "to"
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
                                if (showDateDialog.value == "from") {
                                    filters = filters.map {
                                        if (it.key == "created_ts" && it.operation == "gte") {
                                            it.copy(
                                                value = futureTimeInSeconds.toString(),
                                                interpretation = "$from: ${futureTimeInSeconds.convertDateWithMinutes()}"
                                            )
                                        } else it.copy()
                                    }
                                } else {
                                    filters = filters.map {
                                        if (it.key == "created_ts" && it.operation == "lte") {
                                            it.copy(
                                                value = futureTimeInSeconds.toString(),
                                                interpretation = "$from: ${futureTimeInSeconds.convertDateWithMinutes()}"
                                            )
                                        } else it.copy()
                                    }
                                }

                                isShowClear.value = checkSize()
                                showDateDialog.value = null
                            }
                        )
                    }
                }

                AcceptedPageButton(
                    stringResource(strings.actionAcceptFilters),
                    Modifier.align(Alignment.BottomCenter)
                ) {
                    onClose(filters)
                }
            }
        }
    }
}

