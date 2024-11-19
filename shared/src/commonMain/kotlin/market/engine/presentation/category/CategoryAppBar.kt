package market.engine.presentation.category

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.baseFilters.SD

import market.engine.core.items.NavigationItem
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TitleText
import market.engine.widgets.badges.getBadgedBox
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAppBar(
    isShowNav : MutableState<Boolean>,
    modifier: Modifier = Modifier,
    title: String,
    searchData: SD,
    onSearchClick: () -> Unit,
    onClearSearchClick: () -> Unit,
    onBeakClick: () -> Unit,
) {
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.resetLabel),
            icon = drawables.cancelIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null,
            isVisible = searchData.searchCategoryID != 1L,
            onClick = { onClearSearchClick() }
        ),
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
               onSearchClick()
           }
        },
        navigationIcon = {
            isShowNav.value = searchData.searchCategoryID != 1L

            if (isShowNav.value) {
                NavigationArrowButton {
                    onBeakClick()
                }
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ i, item ->
                    if(item.isVisible){
                        IconButton(
                            modifier = modifier.size(dimens.mediumIconSize),
                            onClick = {
                                item.onClick()
                            }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                    Spacer(modifier = Modifier.padding(dimens.smallPadding))
                }
            }
        }
    )
}
