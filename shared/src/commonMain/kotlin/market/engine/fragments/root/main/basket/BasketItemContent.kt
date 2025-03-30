package market.engine.fragments.root.main.basket

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
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
    baseViewModel: BaseViewModel,
    clearUserOffers: (List<Long>) -> Unit
) {
    val user = item.first
    val maxNotExpandedItems = 2

    val maxItems = remember { mutableStateOf(maxNotExpandedItems) }

    val clickExpand = {
        maxItems.value = if (maxItems.value == maxNotExpandedItems) item.second.size else maxNotExpandedItems
    }

    if (user != null) {
        val selectedOffers = remember { mutableStateOf(emptyList<SelectedBasketItem>()) }
        val bodes = item.second

        Column(
            modifier = Modifier
                .background(colors.white, MaterialTheme.shapes.small)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThemeCheckBox(
                        isSelected = selectedOffers.value.size == bodes.size,
                        onSelectionChange = { checked ->
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
                        },

                        modifier = Modifier
                    )

                    UserRow(
                        user = user,
                        modifier = Modifier.clickable {
                            goToUser(user.id)
                        }.padding(dimens.smallPadding)
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

            Spacer(
                Modifier.fillMaxWidth(0.98f).align(Alignment.CenterHorizontally)
                .background(colors.primaryColor)
                .height(1.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .animateContentSize()
            ) {
                bodes.forEachIndexed { index,  body ->
                    if (index < maxItems.value){
                        BasketOfferItem(
                            offer = body,
                            selectedOffers = selectedOffers,
                            goToOffer,
                            changeQuantity,
                            deleteOffer = deleteOffer,
                            baseViewModel
                        )
                    }
                }
            }

            if (bodes.size > maxNotExpandedItems) {
                Row(
                    modifier = Modifier.clickable {
                        clickExpand()
                    }.fillMaxWidth().padding(dimens.smallPadding),
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

            Spacer(modifier = Modifier.height(dimens.smallSpacer))
            Spacer(
                Modifier.fillMaxWidth(0.98f).align(Alignment.CenterHorizontally)
                    .background(colors.primaryColor)
                    .height(1.dp)
            )

            Column{
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimens.smallPadding),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(strings.totalLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.black,
                    )
                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

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
}
