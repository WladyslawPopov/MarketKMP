package market.engine.widgets.items

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun ActiveFilterListingItem(
    item : FilterListingBtnItem,
){
    FilterChip(
        modifier = Modifier,
        selected = false,
        onClick = item.itemClick,
        label = {
            Text(
                item.text,
                style = MaterialTheme.typography.labelSmall,
                color = colors.black
            )
        },
        trailingIcon = {
            SmallIconButton(
                drawables.cancelIcon,
                color = colors.black,
                modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                modifier = Modifier.size(dimens.smallIconSize)
            ) {
                item.removeFilter()
            }
        },
        border = null,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.white,
            labelColor = colors.black,
            selectedContainerColor = colors.rippleColor,
            selectedLabelColor = colors.black
        )
    )
}
