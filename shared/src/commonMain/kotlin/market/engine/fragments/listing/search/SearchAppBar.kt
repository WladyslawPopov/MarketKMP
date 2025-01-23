package market.engine.fragments.listing.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.textFields.SearchTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    isShowNavigation: Boolean,
    searchString: MutableState<String>,
    focusRequester: FocusRequester,
    onSearchClick: () -> Unit,
    onUpdateHistory: (String) -> Unit,
    onBeakClick: () -> Unit
) {
    val searchItem = NavigationItem(
            title = strings.searchTitle,
            icon = drawables.searchIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null
        )

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
           SearchTextField(
               searchString,focusRequester,onUpdateHistory,onSearchClick,
           )
        },
        navigationIcon = {
            if (isShowNavigation) {
                NavigationArrowButton {
                    onBeakClick()
                }
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(searchItem.isVisible){
                    SmallIconButton(
                        icon = searchItem.icon,
                        color = searchItem.tint
                    ){
                        onSearchClick()
                    }
                }
            }
        }
    )
}
