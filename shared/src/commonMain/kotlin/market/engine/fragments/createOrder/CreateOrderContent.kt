package market.engine.fragments.createOrder

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseContent
import market.engine.widgets.rows.DeliveryCardsContent
import market.engine.widgets.rows.DeliveryCardsRow
import market.engine.widgets.rows.UserSimpleRow
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateOrderContent(
    component: CreateOrderComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOrderViewModel

    val createOrderResponse = viewModel.responseCreateOrder.collectAsState()
    val offers = viewModel.responseGetOffers.collectAsState()
    val deliveryCards = viewModel.responseGetLoadCards.collectAsState()
    val additionalFields = viewModel.responseGetAdditionalData.collectAsState()

    val activeCard = remember { mutableStateOf(1L) }

    val focusManager = LocalFocusManager.current

    val basketItem = model.value.basketItem

    val refresh = {
        viewModel.loadDeliveryCards()
        viewModel.getOffers(basketItem.second.map { it.offerId })
        viewModel.getAdditionalFields(
            basketItem.first,
            basketItem.second.map { it.offerId },
            basketItem.second.map { it.selectedQuantity }
        )
    }

    val isLoading = viewModel.isShowProgress.collectAsState()
    val error : (@Composable () -> Unit)? = null

    val state = rememberScrollState()

    LaunchedEffect(deliveryCards.value) {
        if(deliveryCards.value.isNotEmpty()){
            activeCard.value = deliveryCards.value.find { it.isDefault }?.id ?: 1L
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
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(state).pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            Spacer(modifier = Modifier.height(dimens.smallSpacer))
            // header
            Column(
                modifier = Modifier
                    .background(colors.white, MaterialTheme.shapes.medium)
                    .fillMaxWidth(),
            ) {
                //user header
                offers.value.firstOrNull()?.sellerData?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DynamicLabel(
                            stringResource(strings.sellerLabel),
                            false,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.width(dimens.smallSpacer))
                        UserSimpleRow(it, modifier = Modifier.clickable {
                            component.goToSeller(it.id)
                        })
                    }

                }

                Spacer(modifier = Modifier
                    .background(colors.primaryColor)
                    .height(1.dp)
                    .fillMaxWidth(0.98f)
                    .align(Alignment.CenterHorizontally)
                )

                // offers
                offers.value.forEachIndexed { index, offer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DynamicLabel(
                            "${index+1})",
                            false,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(dimens.smallSpacer))
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

                Spacer(modifier = Modifier
                    .background(colors.primaryColor)
                    .height(1.dp)
                    .fillMaxWidth(0.98f)
                    .align(Alignment.CenterHorizontally)
                )

                //total sum
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimens.mediumPadding),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(strings.totalLabel),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.black,
                    )
                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

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
                        color = colors.titleTextColor,
                    )
                }
            }

            // delivery cards
            if (activeCard.value != 1L) {
                DeliveryCardsContent(
                    activeCard.value,
                    deliveryCards.value,
                    setActiveCard = {

                    },
                    addNewCard = {

                    }
                )
            }
            // additional fields

            //create order button

        }
    }
}

fun createJsonBody(
    fields: List<Fields>,
) : JsonObject {
    return buildJsonObject {
        fields.forEach { data ->
            when (data.key) {
                else -> {
                    put(data.key ?: "", data.data!!)
                }
            }
        }
    }
}





