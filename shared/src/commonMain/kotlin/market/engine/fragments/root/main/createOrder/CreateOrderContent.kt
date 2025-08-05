package market.engine.fragments.root.main.createOrder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.widgets.filterContents.deliveryCardsContents.DeliveryCardsContent
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.items.offer_Items.OrderOfferItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.UserRow
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateOrderContent(
    component: CreateOrderComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOrderViewModel

    val uiState by viewModel.createOrderState.collectAsState()

    val selectDeliveryMethod = uiState.selectDeliveryMethod
    val selectDealType = uiState.selectDealType
    val selectPaymentType = uiState.selectPaymentType

    val offers = uiState.responseGetOffers
    val additionalFields = uiState.responseGetAdditionalData
    val basketItem = model.value.basketItem

    val appBarData = uiState.appBarData

    val isLoading by viewModel.isShowProgress.collectAsState()
    val err by viewModel.errorMessage.collectAsState()
    val toastItem by viewModel.toastItem.collectAsState()

    val focusManager = LocalFocusManager.current

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage.isNotBlank()) {
            {
                OnError(err) {
                    viewModel.refreshPage()
                }
            }
        } else {
            null
        }
    }

    val state = rememberLazyScrollState(viewModel)

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = appBarData
            ){
                val title = stringResource(strings.createNewOrderTitle)
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        onRefresh = {
            viewModel.refreshPage()
        },
        error = error,
        noFound = null,
        isLoading = isLoading,
        toastItem = toastItem,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }.fillMaxSize()

    ) { contentPadding ->
        LazyColumnWithScrollBars(
            state = state.scrollState,
            contentPadding = contentPadding
        ) {
            // header
            item {
                Column(
                    modifier = Modifier
                        .background(colors.white, MaterialTheme.shapes.small)
                        .fillMaxWidth()
                        .padding(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    //user header
                    offers.firstOrNull()?.seller?.let {
                        UserRow(
                            it,
                            modifier = Modifier.clickable {
                                component.goToSeller(it.id)
                            }.align(Alignment.CenterHorizontally)
                                .padding(dimens.smallPadding)
                        )
                    }

                    HorizontalDivider(
                        Modifier.fillMaxWidth(),
                        1.dp,
                        colors.primaryColor
                    )

                    //offers
                    LazyColumnWithScrollBars(
                        heightMod = Modifier.heightIn(max = 2000.dp),
                    ) {
                        items(
                            offers.size,
                            key = { index -> offers[index].id }
                        )
                        { index ->
                            val offer = offers[index]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DynamicLabel(
                                    "${index + 1})",
                                    false,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                OrderOfferItem(
                                    offer,
                                    basketItem.second.find {
                                        it.offerId == offer.id
                                    }?.selectedQuantity ?: 1,
                                    addToFavorites = {
                                        viewModel.addToFavorites(offer)
                                    },
                                    goToOffer = {
                                        component.goToOffer(it)
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = colors.primaryColor
                    )

                    //total sum
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.smallPadding,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(strings.totalLabel),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.black,
                        )

                        val totalPriceText = buildAnnotatedString {
                            var total = 0.0
                            basketItem.second.forEach {
                                total += it.pricePerItem * it.selectedQuantity
                            }
                            append(total.toString())
                            append(" ${stringResource(strings.currencySign)}")
                        }
                        Text(
                            text = totalPriceText,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.priceTextColor,
                        )
                    }
                }
            }

            item {
                // delivery cards
                DeliveryCardsContent(
                    component.additionalModels.value.deliveryCardsViewModel
                ) {
                    viewModel.refreshPage()
                }
            }

            item {
                // additional fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
                )
                {
                    if (additionalFields?.deliveryMethods != null) {
                        SeparatorLabel(
                            stringResource(strings.deliveryMethodLabel),
                        )

                        getDropdownMenu(
                            selectedText = additionalFields.deliveryMethods.find {
                                selectDeliveryMethod == it.code
                            }?.name ?: "",
                            selectedTextDef = additionalFields.deliveryMethods.firstOrNull()?.name
                                ?: "",
                            selects = additionalFields.deliveryMethods.map { it.name ?: "" },
                            onItemClick = { select ->
                                viewModel.changeDeliveryMethod(
                                    additionalFields.deliveryMethods.find { it.name == select }?.code ?: 0
                                )
                            },
                            onClearItem = null,
                            modifier = Modifier.fillMaxWidth(if (isBigScreen.value) 0.4f else 0.9f)
                        )
                    }
                    if (additionalFields?.dealTypes != null) {
                        SeparatorLabel(
                            stringResource(strings.dealTypeLabel),
                        )
                        getDropdownMenu(
                            selectedText = additionalFields.dealTypes.find {
                                selectDealType == it.code
                            }?.name ?: "",
                            selectedTextDef = additionalFields.dealTypes.firstOrNull()?.name
                                ?: "",
                            selects = additionalFields.dealTypes.map { it.name ?: "" },
                            onItemClick = { select ->
                                viewModel.changeDealType(
                                    additionalFields.dealTypes.find { it.name == select }?.code ?: 0
                                )
                            },
                            onClearItem = null,
                            modifier = Modifier.fillMaxWidth(if (isBigScreen.value) 0.4f else 0.9f)
                        )
                    }
                    if (additionalFields?.paymentMethods != null) {
                        SeparatorLabel(
                            stringResource(strings.paymentMethodLabel),
                        )
                        getDropdownMenu(
                            selectedText = additionalFields.paymentMethods.find {
                                selectPaymentType == it.code
                            }?.name ?: "",
                            selectedTextDef = additionalFields.paymentMethods.firstOrNull()?.name
                                ?: "",
                            selects = additionalFields.paymentMethods.map { it.name ?: "" },
                            onItemClick = { select ->
                                viewModel.changePaymentType(
                                    additionalFields.paymentMethods.find { it.name == select }?.code ?: 0
                                )
                            },
                            onClearItem = null,
                            modifier = Modifier.fillMaxWidth(if (isBigScreen.value) 0.4f else 0.9f)
                        )
                    }
                }
            }

            item {
                //create order button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    AcceptedPageButton(
                        stringResource(strings.actionComplete),
                        modifier = Modifier.fillMaxWidth(if (isBigScreen.value) 0.4f else 1f)
                            .padding(dimens.mediumPadding),
                        enabled = !isLoading
                    ) {
                       viewModel.acceptButton(basketItem)
                    }
                }
            }
        }
    }
}
