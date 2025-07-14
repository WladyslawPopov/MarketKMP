package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun PagingCounterBar(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier,
    showUpButton: Boolean,
    showDownButton: Boolean,
    onClick: () -> Unit,
) {
    if( currentPage != 1 || currentPage != totalPages)
    {
        Card(
            modifier = modifier,
            colors = colors.cardColors,
            onClick = {
                onClick()
            }
        )
        {
            Row(
                modifier = Modifier
                    .background(colors.grayLayout)
                    .padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            )
            {
                Text(
                    text = "$currentPage",
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    text = "/",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.titleTextColor
                )

                Text(
                    text = "$totalPages",
                    style = MaterialTheme.typography.labelSmall
                )

                if (showUpButton || showDownButton) {
                    SmallIconButton(
                        if (showDownButton) drawables.iconArrowDown else drawables.iconArrowUp,
                        color = colors.black,
                        modifier = Modifier
                            .background(colors.white.copy(0.7f), CircleShape)
                            .size(dimens.smallIconSize),
                        modifierIconSize = Modifier.size(dimens.extraSmallIconSize)
                    ){
                        onClick()
                    }
                }
            }
        }
    }
}
