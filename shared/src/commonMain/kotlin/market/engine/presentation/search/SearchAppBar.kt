package market.engine.presentation.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.SD

import market.engine.core.items.NavigationItem
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier,
    searchString: String,
    searchData: State<SD>,
    onSearchClick: () -> Unit,
    onUpdateHistory: (String) -> Unit,
    onBeakClick: () -> Unit,
) {
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big
    val focusRequester = remember { FocusRequester() }
    val searchItem = NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.searchIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null
        )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
           SearchTextField(
               modifier,searchString,focusRequester,onUpdateHistory,onBeakClick
           )
        },
        navigationIcon = {
            NavigationArrowButton {
                onBeakClick()
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(searchItem.isVisible){
                    var modIB = modifier
                    if(searchItem.badgeCount != null){
                        val dynamicFontSize = (30 + (searchItem.badgeCount / 10)).coerceAtMost(35).dp
                        modIB = modifier.size(dimens.smallIconSize + dynamicFontSize)
                    }
                    IconButton(
                        modifier = modIB,
                        onClick = {
                            searchData.value.fromSearch = true
                            onSearchClick()
                        }
                    ) {
                        getBadgedBox(modifier = modifier, searchItem)
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
