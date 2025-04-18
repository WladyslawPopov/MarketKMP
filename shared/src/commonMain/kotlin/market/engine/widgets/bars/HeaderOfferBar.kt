package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.dropdown_menu.getOfferOperations
import org.jetbrains.compose.resources.painterResource


@Composable
fun HeaderOfferBar(
    offer: Offer,
    isSelected: Boolean = false,
    onUpdateTrigger: Int,
    baseViewModel: BaseViewModel,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    onUpdateOfferItem : (Offer) -> Unit,
    goToCreateOffer : (CreateOfferType) -> Unit,
    goToDynamicSettings : (String, Long?) -> Unit = {_, _ ->},
    goToProposals : (ProposalType) -> Unit = {},
) {
    val isOpenPopup = remember { mutableStateOf(false) }

    if(onUpdateTrigger < 0) return

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.wrapContentSize()
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (onSelectionChange != null) {
               ThemeCheckBox(
                   isSelected = isSelected,
                   onSelectionChange = onSelectionChange,
                   modifier = Modifier.size(dimens.smallIconSize)
               )
               Spacer(modifier = Modifier.width(dimens.mediumSpacer))
            }

            // Favorites Icon and Count
            Icon(
                painter = painterResource(drawables.favoritesIcon),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            Text(
                text = offer.watchersCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.width(dimens.smallPadding))

            // Views Icon and Count
            Icon(
                painter = painterResource(drawables.eyeOpen),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            Text(
                text = offer.viewsCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Column {
            SmallIconButton(
                drawables.menuIcon,
                colors.black,
                modifierIconSize = Modifier.size(dimens.smallIconSize),
                modifier = Modifier.size(dimens.smallIconSize),
            ) {
                isOpenPopup.value = !isOpenPopup.value
            }

            if (isOpenPopup.value) {
                getOfferOperations(
                    offer = offer,
                    baseViewModel = baseViewModel,
                    onUpdateMenuItem = { offer ->
                        onUpdateOfferItem(offer)
                        isOpenPopup.value = false
                    },
                    goToCreateOffer = goToCreateOffer,
                    onClose = {
                        isOpenPopup.value = false
                    },
                    goToProposals = {
                        goToProposals(it)
                    },
                    goToDynamicSettings = { type, id ->
                        goToDynamicSettings(type, id)
                    }
                )
            }
        }
    }
}
