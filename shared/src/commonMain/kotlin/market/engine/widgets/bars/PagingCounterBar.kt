package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.widgets.buttons.getFloatAnyButton

@Composable
fun PagingCounterBar(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier,
    showUpButton: Boolean,
    showDownButton: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = currentPage != 1 || currentPage != totalPages,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ){
        Card(
            onClick = {
                onClick()
            }
        ) {
            Row(
                modifier = Modifier.wrapContentWidth()
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

                getFloatAnyButton(
                    showDownButton || showUpButton,
                    drawable = if (showDownButton) drawables.iconArrowDown else drawables.iconArrowUp,
                    modifier.size(dimens.mediumIconSize)
                ) {
                    onClick()
                }
            }
        }
    }
}
