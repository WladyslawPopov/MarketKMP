package market.engine.widgets.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.SearchHistoryItem
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun historyItem(
    history: SearchHistoryItem,
    onItemClick: (SearchHistoryItem) -> Unit,
    onSearchClick: (SearchHistoryItem) -> Unit,
){
    Row(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .clickable {
                onSearchClick(history)
            }
            .fillMaxWidth().padding(start = dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)

    ) {
        if(history.isUsersSearch) {
            Icon(
                painterResource(drawables.vectorManSubscriptionIcon),
                contentDescription = "",
                tint = colors.steelBlue,
                modifier = Modifier.size(dimens.extraSmallIconSize)
            )
        }

        if(history.isFinished) {
            Icon(
                painterResource(drawables.historyIcon),
                contentDescription = "",
                tint = colors.steelBlue,
                modifier = Modifier.size(dimens.extraSmallIconSize)
            )
        }

        Text(
            text = history.query,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black,
            modifier = Modifier.weight(1f)
        )

        SmallIconButton(
            iconVector = Icons.Sharp.Edit,
            color = colors.steelBlue,
        ){
            onItemClick(history)
        }
    }
}
