package market.engine.widgets.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.shared.SearchHistory
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun historyItem(
    history: SearchHistory,
    onItemClick: (String) -> Unit,
    onSearchClick: (String) -> Unit,
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color = colors.white)
            .wrapContentHeight()
            .clickable {
                onItemClick(history.query)
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                text = history.query,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = dimens.mediumPadding)
            )

            IconButton(
                onClick = {
                    onSearchClick(history.query)
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ){
                Icon(
                    painter = painterResource(drawables.searchIcon),
                    contentDescription = stringResource(strings.searchTitle),
                    modifier = Modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
            }
        }
    }
}
