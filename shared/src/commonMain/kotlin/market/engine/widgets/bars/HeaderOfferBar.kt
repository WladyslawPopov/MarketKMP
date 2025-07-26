package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.dropdown_menu.PopUpMenu
import org.jetbrains.compose.resources.painterResource


@Composable
fun HeaderOfferBar(
    offer: OfferItem,
    defOptions: List<MenuItem>,
    selected : Boolean = false,
    onSelected : ((Long) -> Unit)? = null
) {
    val isOpenPopup = remember { mutableStateOf(false) }

    val menuList = remember(defOptions) {
        mutableStateOf(defOptions)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            if (onSelected != null) {
               ThemeCheckBox(
                   isSelected = selected,
                   onSelectionChange = {
                       onSelected(offer.id)
                   },
                   modifier = Modifier.size(dimens.smallIconSize)
               )
            }

            // Favorites Icon and Count
            Icon(
                painter = painterResource(drawables.favoritesIcon),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )

            Text(
                text = offer.watchersCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            // Views Icon and Count
            Icon(
                painter = painterResource(drawables.eyeOpen),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )

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
                isOpenPopup.value = true
            }

            PopUpMenu(
                openPopup = isOpenPopup.value,
                menuList = menuList.value,
                onClosed = { isOpenPopup.value = false }
            )
        }
    }
}
