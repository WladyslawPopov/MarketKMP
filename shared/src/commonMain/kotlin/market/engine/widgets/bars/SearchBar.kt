package market.engine.widgets.bars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchBar(onSearchClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(0.7f)
            .padding(dimens.mediumPadding),
        colors = colors.cardColors,
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimens.smallElevation
        ),
        onClick = {
            onSearchClick()
        }
    ) {
        Row(
            Modifier.padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(strings.searchTitle),
                style = MaterialTheme.typography.labelMedium,
                color = colors.grayText
            )

            Icon(
                painterResource(drawables.searchClassicIcon),
                contentDescription = stringResource(strings.searchTitle),
                tint = colors.black,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
