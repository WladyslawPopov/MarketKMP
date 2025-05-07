package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchBar(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier.padding(dimens.mediumPadding).zIndex(5f)
    ) {
        Row(
            modifier = Modifier
                .background(colors.white, shape = MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onSearchClick() }
                .fillMaxWidth(0.7f)
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
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
