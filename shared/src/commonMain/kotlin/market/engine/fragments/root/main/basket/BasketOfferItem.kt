package market.engine.fragments.root.main.basket

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.utils.getOfferImagePreview
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BasketOfferItem(
    offer: Offer?,
    selectedOffers: MutableState<List<SelectedBasketItem>>,
    goToOffer: (Long) -> Unit,
    changeQuantity: (Long, Int) -> Unit,
    deleteOffer: (Long) -> Unit,
    baseViewModel: BaseViewModel
) {
    if (offer == null) return

    val isChecked = remember { mutableStateOf(selectedOffers.value.find { it.offerId == offer.id } != null) }

    val selectedQuantity = remember { mutableStateOf(offer.quantity) }

    val isFavorites = remember { mutableStateOf(offer.isWatchedByMe) }

    LaunchedEffect(selectedOffers){
        snapshotFlow {
            selectedOffers.value.find { it.offerId == offer.id }
        }.collect{
            isChecked.value = it != null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding),
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
                onSelectionChange = { checked ->
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
                },
                modifier = Modifier
            )

            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        goToOffer(offer.id)
                    }.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
            ) {
                Box(
                    modifier = Modifier.padding(dimens.extraSmallPadding),
                    contentAlignment = Alignment.TopStart
                ) {
                    LoadImage(
                        url = offer.getOfferImagePreview(),
                        size = if(isBigScreen.value) 120.dp else 90.dp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TitleText(
                            text = offer.title ?: "",
                            modifier = Modifier.weight(1f)
                        )

                        SmallIconButton(
                            icon = if (isFavorites.value) drawables.favoritesIconSelected
                            else drawables.favoritesIcon,
                            color = colors.inactiveBottomNavIconColor,
                            modifierIconSize = Modifier.size(dimens.smallIconSize),
                            modifier = Modifier.align(Alignment.Top)
                        ){
                            baseViewModel.addToFavorites(offer.parseToOfferItem()){
                                offer.isWatchedByMe = it
                                isFavorites.value = offer.isWatchedByMe
                            }
                        }
                    }


                    val location = buildString {
                        offer.freeLocation?.let { append(it) }
                        offer.region?.name?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }

                    if (location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(drawables.locationIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize),
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.black
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(drawables.iconCountBoxes),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.smallIconSize),
                        )
                        Text(
                            text = stringResource(strings.inStockLabel) + " " + offer.currentQuantity,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.black
                        )
                    }

                    val priceText = buildAnnotatedString {
                        append(offer.currentPricePerItem ?: "")
                        append(" ${stringResource(strings.currencySign)}")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.priceOfOneOfferLabel),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black,
                        )
                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.black,
                        )
                    }
                }
            }
        }

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

            val currentItemPrice = (offer.currentPricePerItem?.toDouble() ?: 0.0) * selectedQuantity.value

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
