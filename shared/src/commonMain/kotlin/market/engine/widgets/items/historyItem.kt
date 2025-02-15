package market.engine.widgets.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.shared.SearchHistory
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun historyItem(
    history: SearchHistory,
    onItemClick: (String) -> Unit,
    onSearchClick: (String) -> Unit,
){
    Row(
        modifier = Modifier
            .clickable {
                onSearchClick(history.query)
            }
            .background(colors.white, MaterialTheme.shapes.small)
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
            drawables.searchIcon,
            colors.steelBlue,
        ){
            onItemClick(history.query)
        }
    }
}
