package market.engine.presentation.favorites

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.NavigationItem
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.widgets.texts.TitleText
import market.engine.widgets.badges.getBadgedBox
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesAppBar(
    title : String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
           TitleText(title)
        },
        actions = {
//            Row(
//                modifier = modifier.padding(end = dimens.smallPadding),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                listItems.forEachIndexed{ index, item ->
//                    if(item.isVisible){
//                        var modIB = modifier
//                        if(item.badgeCount != null){
//                            val dynamicFontSize = (30 + (item.badgeCount / 10)).coerceAtMost(35).dp
//                            modIB = modifier.size(dimens.smallIconSize + dynamicFontSize)
//                        }
//                        IconButton(
//                            modifier = modIB,
//                            onClick = { item.onClick() }
//                        ) {
//                            getBadgedBox(modifier = modifier, item)
//                        }
//                    }
//                }
//            }
        }
    )
}
