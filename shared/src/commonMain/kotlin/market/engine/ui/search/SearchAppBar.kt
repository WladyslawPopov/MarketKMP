package market.engine.ui.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.globalObjects.searchData
import market.engine.business.items.NavigationItem
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
import market.engine.widgets.SmallCancelBtn
import market.engine.widgets.common.TitleText
import market.engine.widgets.common.getBadgedBox
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier,
    searchString: State<String> = rememberUpdatedState(""),
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
            TextField(
                value = searchString.value,
                onValueChange = {
                    onUpdateHistory(it)
                },
                placeholder = {
                    Text(
                        text = stringResource(strings.selectSearchTitle),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier = modifier.clip(MaterialTheme.shapes.small)
                    .wrapContentSize()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onBeakClick()
                    }
                ),
                trailingIcon = {
                    if (searchString.value != "") {
                        SmallCancelBtn {
                            onUpdateHistory(it)
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                colors =  TextFieldDefaults.colors(
                    focusedTextColor = colors.black,
                    unfocusedTextColor = colors.black,

                    focusedContainerColor = colors.white,
                    unfocusedContainerColor = colors.white,

                    focusedIndicatorColor = colors.transparent,
                    unfocusedIndicatorColor = colors.transparent,
                    disabledIndicatorColor = colors.transparent,
                    errorIndicatorColor = colors.transparent,

                    focusedPlaceholderColor = colors.steelBlue,
                    unfocusedPlaceholderColor = colors.steelBlue,
                    disabledPlaceholderColor = colors.transparent
                ),

            )
        },
        navigationIcon = {
            if (!showNavigationRail) {
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
                if(searchItem.isVisible){
                    var modIB = modifier
                    if(searchItem.badgeCount != null){
                        val dynamicFontSize = (30 + (searchItem.badgeCount / 10)).coerceAtMost(35).dp
                        modIB = modifier.size(dimens.smallIconSize + dynamicFontSize)
                    }
                    IconButton(
                        modifier = modIB,
                        onClick = {
                            searchData.searchString = searchString.value
                            searchData.fromSearch = true

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
