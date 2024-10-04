package market.engine.presentation.category

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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalObjects.searchData
import market.engine.core.items.NavigationItem
import market.engine.widgets.common.TitleText
import market.engine.widgets.common.getBadgedBox
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAppBar(
    title : String,
    isShowNav : MutableState<Boolean>,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onBeakClick: () -> Unit,
) {
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.searchIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.cancelIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null,
            isVisible = false
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
           TitleText(title){
               onSearchClick()
           }
        },
        navigationIcon = {
            isShowNav.value = searchData.searchCategoryID != 1L

            if (isShowNav.value) {
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
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        var modIB = modifier
                        if(item.badgeCount != null){
                            val dynamicFontSize = (30 + (item.badgeCount / 10)).coerceAtMost(35).dp
                            modIB = modifier.size(dimens.smallIconSize + dynamicFontSize)
                        }
                        IconButton(
                            modifier = modIB,
                            onClick = {
                                onSearchClick()
                            }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                }
            }
        }
    )
}
