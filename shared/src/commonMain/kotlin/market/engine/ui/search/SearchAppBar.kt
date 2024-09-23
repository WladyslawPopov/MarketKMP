package market.engine.ui.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import market.engine.widgets.common.TitleText
import market.engine.widgets.common.getBadgedBox
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    modifier: Modifier = Modifier,
    onBeakClick: () -> Unit,
) {
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.searchIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null
        ),
    )

    val searchString = remember {
        mutableStateOf(searchData.searchString ?: "")
    }

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextField(
                value = searchString.value,
                onValueChange = {
                    searchString.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(strings.selectSearchTitle),
                        style = MaterialTheme.typography.labelSmall)
                },
                modifier = modifier.clip(MaterialTheme.shapes.small).height(48.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onBeakClick()
                    }
                ),
                trailingIcon = {
                    if (searchString.value != "") {
                        IconButton(
                            modifier = modifier.padding(dimens.smallPadding),
                            onClick = {
                                searchString.value = ""
                            }
                        ) {
                            Icon(
                                painterResource(drawables.cancelIcon),
                                contentDescription = stringResource(strings.actionClose),
                                modifier = modifier.size(dimens.extraSmallIconSize),
                                tint = colors.steelBlue
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodySmall,
                colors =  TextFieldDefaults.colors(
                    focusedTextColor = colors.black,
                    unfocusedTextColor = colors.black,

                    focusedContainerColor = colors.lightGray,
                    unfocusedContainerColor = colors.lightGray,

                    focusedIndicatorColor = colors.transparent,
                    unfocusedIndicatorColor = colors.transparent,
                    disabledIndicatorColor = colors.transparent,
                    errorIndicatorColor = colors.transparent,

                    focusedPlaceholderColor = colors.transparent,
                    unfocusedPlaceholderColor = colors.steelBlue,
                    disabledPlaceholderColor = colors.steelBlue
                )
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
