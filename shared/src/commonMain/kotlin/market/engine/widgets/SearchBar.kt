package market.engine.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchBar(modifier: Modifier = Modifier, onSearchClick: () -> Unit) {

    val windowClass = getWindowSizeClass()
    val isBigWindow = windowClass == WindowSizeClass.Big

    Box(
        modifier = modifier
            .sizeIn(
                minWidth = if(isBigWindow) 350.dp else 200.dp,
                minHeight = if(isBigWindow) 50.dp else 50.dp,
                maxWidth = if(isBigWindow) 500.dp else 300.dp,
                maxHeight = if(isBigWindow) 70.dp else 70.dp
            )
            .padding(dimens.mediumPadding)
            .background(colors.white, shape = RoundedCornerShape(dimens.largeCornerRadius))
            .clip(RoundedCornerShape(dimens.largeCornerRadius))
            .clickable { onSearchClick() }
            .padding(horizontal = dimens.mediumPadding, vertical = dimens.smallPadding)
    ) {
        Row(
            modifier = modifier.matchParentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically).weight(0.8f),
                text = stringResource(strings.searchTitle),
                color = colors.black
            )
            Icon(
                modifier = Modifier.weight(0.1f),
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(strings.searchTitle),
                tint = colors.textA0AE,
            )
        }
    }
}
