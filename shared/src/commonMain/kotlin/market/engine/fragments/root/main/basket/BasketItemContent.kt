package market.engine.fragments.root.main.basket

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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.User
import market.engine.core.utils.printLogD
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.items.offer_Items.OrderOfferItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.UserRow
import org.jetbrains.compose.resources.stringResource


@Composable
fun BasketItemContent(
    item: Pair<User?, List<Offer?>>,
    goToUser : (Long) -> Unit,
    goToOffer: (Long) -> Unit,
    goToCreateOrder: (Pair<Long, List<SelectedBasketItem>>) -> Unit,
    changeQuantity: (Long, Int) -> Unit,
    deleteOffer: (Long) -> Unit,
    addOfferToFavorites: (Offer,(Boolean)->Unit) -> Unit,
    clearUserOffers: (List<Long>) -> Unit
) {
    printLogD("Recomposition", "BasketItemContent for user: ${item.first?.id}")
    val user =  item.first
    val maxNotExpandedItems =  2

    val maxItems = remember { mutableStateOf(maxNotExpandedItems) }

    val clickExpand = {
        maxItems.value = if (maxItems.value == maxNotExpandedItems) item.second.size else maxNotExpandedItems
    }

    val selectedOffers = remember { mutableStateOf(emptyList<SelectedBasketItem>()) }
    val bodes =  item.second

    if (user != null) {
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
                        isSelected = selectedOffers.value.size == bodes.size,
                        onSelectionChange = remember {
                            { checked ->
                                selectedOffers.value = if (checked) {
                                    bodes.filter { it?.safeDeal == true }.mapNotNull { it }.map { offer ->
                                        SelectedBasketItem(
                                            offerId = offer.id,
                                            pricePerItem = offer.currentPricePerItem?.toDouble()
                                                ?: 0.0,
                                            selectedQuantity = 1
                                        )
                                    }
                                } else {
                                    emptyList()
                                }
                            }
                        },

                        modifier = Modifier
                    )

                    UserRow(
                        user = user,
                        modifier = Modifier.clickable {
                            goToUser(user.id)
                        }
                    )
                }

                AnimatedVisibility (selectedOffers.value.isNotEmpty(),
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    SmallIconButton(
                        drawables.deleteIcon,
                        color = colors.negativeRed,
                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                        modifier = Modifier
                    ) {
                        clearUserOffers(selectedOffers.value.map { it.offerId })
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
                items(
                    bodes.size,
                    key = { index -> bodes[index]?.id ?: index }
                ) { index ->
                    val offer = bodes[index]
                    if (index < maxItems.value && offer != null){
                        val isChecked = mutableStateOf(selectedOffers.value.find { it.offerId == offer.id } != null)
                        val selectedQuantity = mutableStateOf(offer.quantity)

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
                                    isSelected = isChecked.value,
                                    isEnable = offer.safeDeal,
                                    onSelectionChange = remember { { checked ->
                                        isChecked.value = checked
                                        if (isChecked.value) {
                                            selectedOffers.value = buildList {
                                                addAll(selectedOffers.value)
                                                add(
                                                    SelectedBasketItem(
                                                        offerId = offer.id,
                                                        pricePerItem = offer.currentPricePerItem?.toDouble() ?: 0.0,
                                                        selectedQuantity = if (selectedQuantity.value > 0)
                                                            selectedQuantity.value
                                                        else 1
                                                    )
                                                )
                                            }
                                        } else {
                                            selectedOffers.value = buildList {
                                                addAll(selectedOffers.value)
                                                remove(
                                                    SelectedBasketItem(
                                                        offerId = offer.id,
                                                        pricePerItem = offer.currentPricePerItem?.toDouble() ?: 0.0,
                                                        selectedQuantity = if (selectedQuantity.value > 0)
                                                            selectedQuantity.value
                                                        else 1
                                                    )
                                                )
                                            }
                                        }
                                    } },
                                    modifier = Modifier
                                )

                                OrderOfferItem(
                                    offer = offer,
                                    selectedQuantity = null,
                                    addToFavorites = remember{
                                        { onFinished ->
                                            addOfferToFavorites(offer) {
                                                onFinished(it)
                                            }
                                        }
                                    },
                                    goToOffer =  remember {{
                                        goToOffer(offer.id)
                                    }}
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

                                val currentItemPrice = remember { (offer.currentPricePerItem?.toDouble() ?: 0.0) * selectedQuantity.value }

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

                                        selectedOffers.value = selectedOffers.value.map {
                                            if (it.offerId == offer.id) {
                                                it.copy(selectedQuantity = selectedQuantity.value)
                                            } else it
                                        }.toMutableList()
                                        offer.quantity = selectedQuantity.value
                                        changeQuantity(offer.id, selectedQuantity.value)
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
                                        selectedOffers.value = selectedOffers.value.map {
                                            if (it.offerId == offer.id) {
                                                it.copy(selectedQuantity = selectedQuantity.value)
                                            } else it
                                        }.toMutableList()
                                        offer.quantity = selectedQuantity.value
                                        changeQuantity(offer.id, selectedQuantity.value)
                                    }
                                }

                                SmallIconButton(
                                    drawables.deleteIcon,
                                    color = colors.negativeRed,
                                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                                ){
                                    deleteOffer(offer.id)
                                }
                            }
                        }
                    }
                }
            }

            if (bodes.size > maxNotExpandedItems) {
                Row(
                    modifier = Modifier
                        .background(colors.primaryColor.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            clickExpand()
                        }.fillMaxWidth()
                        .padding(dimens.smallSpacer),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallIconButton(
                        if (maxItems.value == maxNotExpandedItems) drawables.iconArrowDown else drawables.iconArrowUp,
                        color = colors.inactiveBottomNavIconColor,
                        modifierIconSize = Modifier.size(dimens.mediumIconSize),
                    ) {
                        clickExpand()
                    }
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
                    selectedOffers.value.forEach {
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
                strings.actionBuy,
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .fillMaxWidth(),
                enabled = selectedOffers.value.isNotEmpty()
            ) {
                goToCreateOrder(Pair(user.id, selectedOffers.value))
            }
        }
    }
}
