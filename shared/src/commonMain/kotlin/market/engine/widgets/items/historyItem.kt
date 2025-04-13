package market.engine.widgets.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.SearchHistoryItem
import market.engine.widgets.buttons.SmallIconButton

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
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Text(
            text = history.query,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        SmallIconButton(
            iconVector = Icons.Sharp.Edit,
            color = colors.steelBlue,
        ){
            onItemClick(history)
        }
    }
}
