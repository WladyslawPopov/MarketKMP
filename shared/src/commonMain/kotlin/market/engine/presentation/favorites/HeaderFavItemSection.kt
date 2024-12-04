package market.engine.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.buttons.SmallImageButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun HeaderFavItemSection(
    offer: Offer,
    isSelected: Boolean,
    onMenuClick: () -> Unit,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelectionChange(it) },
            modifier = Modifier.size(dimens.smallIconSize),
            colors = CheckboxDefaults.colors(
                checkedColor = colors.inactiveBottomNavIconColor,
                uncheckedColor = colors.textA0AE,
                checkmarkColor = colors.alwaysWhite
            )
        )

        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Spacer(modifier = Modifier.width(dimens.mediumPadding))

            AnimatedVisibility (!isSelected) {
                SmallImageButton(
                    drawables.menuIcon,
                    modifier = Modifier.size(dimens.smallIconSize),
                    modifierIconSize = Modifier.size(dimens.smallIconSize)
                ){
                    onMenuClick()
                }
            }
        }
    }
}
