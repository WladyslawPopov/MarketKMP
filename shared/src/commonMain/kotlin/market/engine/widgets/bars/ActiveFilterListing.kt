package market.engine.widgets.bars

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun ActiveFilterListing(
    text : String,
    removeFilter : () -> Unit,
    itemClick : () -> Unit
){
    FilterChip(
        modifier = Modifier,
        selected = false,
        onClick = itemClick,
        label = {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        trailingIcon = {
            SmallIconButton(
                drawables.cancelIcon,
                color = colors.black,
                modifierIconSize = Modifier.size(dimens.smallIconSize),
                modifier = Modifier.size(dimens.smallIconSize)
            ) {
                removeFilter()
            }
        },
        border = null,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.white,
            labelColor = colors.black,
            selectedContainerColor = colors.selected,
            selectedLabelColor = colors.black
        )
    )
}
