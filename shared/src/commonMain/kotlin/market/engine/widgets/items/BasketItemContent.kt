package market.engine.widgets.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.data.constants.minExpandedElement
import market.engine.core.data.events.BasketEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.BasketGroupUiState
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.items.offer_Items.OrderOfferItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.UserRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun BasketItemContent(
    state: BasketGroupUiState,
    events: BasketEvents,
) {
    val user = remember(state.user) { state.user }
    val bodes = remember(state.offersInGroup) { state.offersInGroup }

    Column(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                ThemeCheckBox(
                    isSelected = state.isAllSelected,
                    onSelectionChange = { checked ->
                        events.onSelectAll(user.id, bodes, checked)
                    },
                    modifier = Modifier
                )

                UserRow(
                    user = user,
                    modifier = Modifier.clickable {
                        events.onGoToUser(user.id)
                    }
                )
            }

            AnimatedVisibility (state.selectedOffersCount > 0,
                enter = fadeIn(), exit = fadeOut()
            ) {
                SmallIconButton(
                    drawables.deleteIcon,
                    color = colors.negativeRed,
                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                    modifier = Modifier
                ) {
                    events.onDeleteOffersRequest(state.selectedOffers.map { it.offerId })
                }
            }
        }

        Divider(
            color = colors.primaryColor,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        //offers list
        LazyColumnWithScrollBars(
            heightMod = Modifier.heightIn(min = 150.dp, max = 2000.dp),
        ) {
            val itemsToShow = bodes.take(state.showItemsCount)
            items(
                itemsToShow.size,
                key = { index -> bodes[index]?.id ?: index }
            ) { index ->
                val offer = bodes[index]
                if (offer == null) return@items
                val selectedQuantity = remember(offer.quantity) { mutableStateOf(offer.quantity) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeCheckBox(
                            isSelected = state.selectedOffers.find { it.offerId == offer.id } != null,
                            isEnable = offer.safeDeal,
                            onSelectionChange ={ checked ->
                                events.onOfferSelected(
                                    user.id,
                                    SelectedBasketItem(
                                        offerId = offer.id,
                                        pricePerItem = offer.price.toDouble(),
                                        selectedQuantity = if (selectedQuantity.value > 0)
                                            selectedQuantity.value
                                        else 1
                                    ), checked
                                )
                            },
                            modifier = Modifier
                        )

                        OrderOfferItem(
                            offer = offer,
                            selectedQuantity = null,
                            addToFavorites = { onFinished ->
                                    events.onAddToFavorites(offer) {
                                        onFinished(it)
                                    }
                                },
                            goToOffer = {
                                events.onGoToOffer(offer.id)
                            }
                        )
                    }

                    // Price, quantity and action buttons
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.totalLabel),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.black,
                        )

                        val currentItemPrice = remember(selectedQuantity.value) {
                            offer.price.toDouble() * selectedQuantity.value
                        }

                        Text(
                            text = buildAnnotatedString {
                                append(currentItemPrice.toString())
                                append(" ${stringResource(strings.currencySign)}")
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.black,
                        )

                        // Minus button
                        SmallIconButton(
                            drawables.minusIcon,
                            color = if (selectedQuantity.value > 1) colors.actionTextColor else colors.grayText,
                            modifierIconSize = Modifier.size(dimens.smallIconSize),
                            modifier = Modifier.size(dimens.smallIconSize)
                        ){
                            if (selectedQuantity.value > 1) {
                                selectedQuantity.value--
                                events.onQuantityChanged(
                                    offer.id,
                                    selectedQuantity.value
                                ){
                                    offer.quantity = it
                                }
                            }
                        }

                        Text(
                            text = selectedQuantity.value.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.titleTextColor,
                        )

                        // Plus button
                        SmallIconButton(
                            drawables.plusIcon,
                            color = if (selectedQuantity.value < offer.currentQuantity) colors.actionTextColor else colors.grayText,
                            modifierIconSize = Modifier.size(dimens.smallIconSize),
                            modifier = Modifier.size(dimens.smallIconSize)
                        ){
                            if (selectedQuantity.value < offer.currentQuantity) {
                                selectedQuantity.value++
                                events.onQuantityChanged(
                                    offer.id,
                                    selectedQuantity.value
                                ){
                                    offer.quantity = it
                                }
                            }
                        }

                        SmallIconButton(
                            drawables.deleteIcon,
                            color = colors.negativeRed,
                            modifierIconSize = Modifier.size(dimens.smallIconSize),
                        ){
                            events.onDeleteOffersRequest(listOf(offer.id))
                        }
                    }
                }
            }
        }

        if (bodes.size > minExpandedElement) {
            Row(
                modifier = Modifier
                    .background(colors.primaryColor.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        events.onExpandClicked(user.id, bodes.size)
                    }.fillMaxWidth()
                    .padding(dimens.smallSpacer),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(if (bodes.size > state.showItemsCount) drawables.iconArrowDown else drawables.iconArrowUp),
                    contentDescription = null,
                    tint = colors.inactiveBottomNavIconColor,
                    modifier = Modifier.size(dimens.mediumIconSize),
                )
            }
        }

        Divider(
            color = colors.primaryColor,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(strings.totalLabel),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.black,
            )

            val totalPriceText = buildAnnotatedString {
                var total = 0.0
                state.selectedOffers.forEach {
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

        AcceptedPageButton(
            stringResource(strings.actionBuy),
            modifier = Modifier
                .padding(dimens.smallPadding)
                .fillMaxWidth(),
            enabled = state.selectedOffers.isNotEmpty()
        ) {
            events.onCreateOrder(user.id, state.selectedOffers)
        }
    }
}
