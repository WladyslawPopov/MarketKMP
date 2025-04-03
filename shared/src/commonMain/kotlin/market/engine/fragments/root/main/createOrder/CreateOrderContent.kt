package market.engine.fragments.root.main.createOrder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.delay
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.fragments.base.BackHandler
import market.engine.fragments.root.dynamicSettings.contents.DeliveryCardsContent
import market.engine.fragments.base.onError
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
    val selectDeliveryMethod = rememberUpdatedState(viewModel.selectDeliveryMethod.value)
    val selectDealType = rememberUpdatedState(viewModel.selectDealType.value)
    val selectPaymentType = rememberUpdatedState(viewModel.selectPaymentType.value)

    val offers = viewModel.responseGetOffers.collectAsState()
    val additionalFields = viewModel.responseGetAdditionalData.collectAsState()
    val createOrderResponse = viewModel.responseCreateOrder.collectAsState()

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    val basketItem = model.value.basketItem

    val deliveryCards = remember {
        viewModel.responseGetLoadCards
    }

    val deliveryFields = remember {
        viewModel.deliveryFields
    }

    BackHandler(model.value.backHandler){
        component.onBackClicked()
    }

    val refresh = {
        viewModel.onError(ServerErrorException())
        viewModel.updateDeliveryFields()
        viewModel.getOffers(basketItem.second.map { it.offerId })
        viewModel.getAdditionalFields(
            basketItem.first,
            basketItem.second.map { it.offerId },
            basketItem.second.map { it.selectedQuantity }
        ){
           component.onBackClicked()
        }
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        {
            onError(err) {
                refresh()
            }
        }
    } else {
        null
    }

    val state = rememberLazyListState()

    LaunchedEffect(createOrderResponse.value){
        if (createOrderResponse.value?.status == "operation_success"){
            delay(2000)
            component.goToMyOrders()
        }
    }

    BaseContent(
        topBar = {
            CreateOrderAppBar(
                onBackClick = {
                    component.onBackClicked()
                }
            )
        },
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()

    ) {
        LazyColumnWithScrollBars(
            state = state,
            modifierList = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }.padding(dimens.smallPadding),
        ) {
            // header
            item {
                Column(
                    modifier = Modifier
                        .background(colors.white, MaterialTheme.shapes.medium)
                        .fillMaxWidth()
                        .padding(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                ) {
                    //user header
                    offers.value.firstOrNull()?.sellerData?.let {
                        UserRow(
                            it,
                            modifier = Modifier.clickable {
                                component.goToSeller(it.id)
                            }.align(Alignment.CenterHorizontally)
                                .padding(dimens.smallPadding)
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .background(colors.primaryColor)
                            .height(1.dp)
                            .fillMaxWidth(0.98f)
                            .align(Alignment.CenterHorizontally)
                    )

                    // offers
                    offers.value.forEachIndexed { index, offer ->
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
                            CreateOrderOfferItem(
                                offer,
                                basketItem.second.find {
                                    it.offerId == offer.id
                                }?.selectedQuantity ?: 1
                            ) {
                                component.goToOffer(it)
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .background(colors.primaryColor)
                            .height(1.dp)
                            .fillMaxWidth(0.98f)
                            .align(Alignment.CenterHorizontally)
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
                    deliveryCards.value,
                    deliveryFields.value,
                    viewModel,
                    setUpNewFields = { cf ->
                        deliveryFields.value = cf
                    },
                    onError = {
                        deliveryFields.value = it
                    }
                ) {
                    refresh()
                }
            }
            item {
                // additional fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (additionalFields.value?.deliveryMethods != null) {
                        SeparatorLabel(
                            stringResource(strings.deliveryMethodLabel),
                        )

                        getDropdownMenu(
                            selectedText = additionalFields.value?.deliveryMethods?.find {
                                selectDeliveryMethod.value == it.code
                            }?.name ?: "",
                            selectedTextDef = additionalFields.value?.deliveryMethods?.firstOrNull()?.name
                                ?: "",
                            selects = additionalFields.value?.deliveryMethods?.map { it.name ?: "" }
                                ?: emptyList(),
                            onItemClick = { select ->
                                viewModel.selectDeliveryMethod.value =
                                    additionalFields.value?.deliveryMethods?.find { it.name == select }?.code
                                        ?: 0
                            },
                            onClearItem = null,
                            modifier = Modifier.fillMaxWidth(if (isBigScreen) 0.4f else 0.9f)
                        )
                    }
                    if (additionalFields.value?.dealTypes != null) {
                        SeparatorLabel(
                            stringResource(strings.dealTypeLabel),
                        )
                        getDropdownMenu(
                            selectedText = additionalFields.value?.dealTypes?.find {
                                selectDealType.value == it.code
                            }?.name ?: "",
                            selectedTextDef = additionalFields.value?.dealTypes?.firstOrNull()?.name
                                ?: "",
                            selects = additionalFields.value?.dealTypes?.map { it.name ?: "" }
                                ?: emptyList(),
                            onItemClick = { select ->
                                viewModel.selectDealType.value =
                                    additionalFields.value?.dealTypes?.find { it.name == select }?.code
                                        ?: 0
                            },
                            onClearItem = null,
                            modifier = Modifier.fillMaxWidth(if (isBigScreen) 0.4f else 0.9f)
                        )
                    }
                    if (additionalFields.value?.paymentMethods != null) {
                        SeparatorLabel(
                            stringResource(strings.paymentMethodLabel),
                        )
                        getDropdownMenu(
                            selectedText = additionalFields.value?.paymentMethods?.find {
                                selectPaymentType.value == it.code
                            }?.name ?: "",
                            selectedTextDef = additionalFields.value?.paymentMethods?.firstOrNull()?.name
                                ?: "",
                            selects = additionalFields.value?.paymentMethods?.map { it.name ?: "" }
                                ?: emptyList(),
                            onItemClick = { select ->
                                viewModel.selectPaymentType.value =
                                    additionalFields.value?.paymentMethods?.find { it.name == select }?.code
                                        ?: 0
                            },
                            onClearItem = null,
                            modifier = Modifier.fillMaxWidth(if (isBigScreen) 0.4f else 0.9f)
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
                ) {
                    AcceptedPageButton(
                        strings.actionComplete,
                        modifier = Modifier.fillMaxWidth(if (isBigScreen) 0.4f else 0.9f)
                            .padding(dimens.mediumPadding),
                        enabled = !isLoading.value
                    ) {
                        viewModel.postPage(deliveryFields.value, basketItem)
                    }
                }
            }
        }
    }
}
