package market.engine.presentation.listing

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
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.NavigationItem
import market.engine.widgets.texts.TitleText
import market.engine.widgets.badges.getBadgedBox
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingAppBar(
    title : String,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onBeakClick: () -> Unit,
) {
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.searchIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null,
            onClick = { onSearchClick() }
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
           TitleText(title){
               onBeakClick()
           }
        },
        navigationIcon = {
            IconButton(
                modifier = modifier,
                onClick = {
                    onBeakClick()
                }
            ){
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(strings.menuTitle),
                    modifier = modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        IconButton(
                            modifier = modifier.size(dimens.smallIconSize),
                            onClick = { item.onClick() }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                }
            }
        }
    )
}
