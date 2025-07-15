package market.engine.widgets.badges

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.tooltip.TooltipState
import market.engine.widgets.tooltip.tooltip
import org.jetbrains.compose.resources.painterResource

@Composable
fun BadgedButton(
    item: NavigationItem,
    selected: Boolean = false,
    colorBackground: Color = colors.transparent,
    tooltipState: TooltipState? = null,
) {
    val modifier = if(tooltipState != null) {
        Modifier
            .tooltip(
                state = tooltipState,
                data = item.tooltipData,
                initialVisibility = true
            )
            .clip(CircleShape)
            .clickable { item.onClick() }
            .size(dimens.mediumIconSize)
    }else{
        Modifier
            .clip(CircleShape)
            .clickable { item.onClick() }
            .size(dimens.mediumIconSize)
    }

    BadgedBox(
        badge = {
            getBadge(item.badgeCount, item.hasNews)
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(colorBackground, MaterialTheme.shapes.medium)
                .padding(dimens.smallSpacer)
                .clickable { item.onClick() }
        ) {
            if (item.imageString != null) {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    LoadImage(
                        url = item.imageString,
                        isShowLoading = false,
                        isShowEmpty = false,
                        modifier = Modifier.size(dimens.mediumIconSize),
                        contentScale = ContentScale.FillBounds
                    )
                }
            } else {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    if (item.icon != null) {
                        Icon(
                            painter = painterResource(item.icon!!),
                            contentDescription = item.title,
                            tint = if (!selected) item.tint else item.tintSelected,
                            modifier = Modifier.size(dimens.smallIconSize)
                        )
                    }
                    if (item.image != null) {
                        Image(
                            painter = painterResource(item.image!!),
                            contentDescription = item.title,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.size(dimens.mediumIconSize)
                        )
                    }
                }
            }
        }
    }
}
