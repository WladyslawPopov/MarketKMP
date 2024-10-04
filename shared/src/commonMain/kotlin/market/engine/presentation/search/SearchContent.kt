package market.engine.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalObjects.searchData
import market.engine.widgets.SmallCancelBtn
import market.engine.widgets.exceptions.onError
import market.engine.presentation.root.BaseContent
import market.engine.shared.SearchHistory
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchContent(
    component: SearchComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value

    val isLoading = model.isLoading.collectAsState()
    val isError = model.isError.collectAsState()
    val history = model.history.collectAsState()
    val searchString = model.searchString.collectAsState()

    val selectedUser = remember { mutableStateOf(false) }
    val selectedUserFinished = remember { mutableStateOf(false) }
    val selectedCategory = remember { mutableStateOf(searchData.searchCategoryName) }

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(model.isError.value) { } }
    }else{
        null
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        error = error,
        topBar = {
            SearchAppBar(
                modifier,
                searchString,
                onSearchClick = {
                    component.goToListing()
                },
                onUpdateHistory = {
                    component.updateHistory(it)
                }
            ) {
                component.onCloseClicked()
            }
        },
        onRefresh = { component.updateHistory("") }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            FiltersSearchBar(
                modifier = modifier,
                selectedUser = selectedUser,
                selectedUserFinished = selectedUserFinished,
                selectedCategory = selectedCategory,
                searchString = searchString,
                goToCategory = { component.goToCategory() }
            )

            HistoryLayout(
                historyItems = history.value,
                modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
                onItemClick = { component.updateHistory(it) },
                onClearHistory = { component.deleteHistory() },
                onDeleteItem = { component.deleteItemHistory(it) },
                goToListing = { component.goToListing() }
            )
        }
    }
}

@Composable
fun HistoryLayout(
    historyItems: List<SearchHistory>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    goToListing: () -> Unit
) {
    if (historyItems.isEmpty()) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimens.smallPadding),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = stringResource(strings.searchHistory),
            color = colors.black,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = dimens.extraSmallPadding)
                .align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                onClearHistory()
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text =  stringResource(strings.clear),
                color = colors.solidGreen,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(50.dp, 250.dp)
            .wrapContentHeight()
            .background(color = colors.primaryColor),
        contentPadding = PaddingValues(dimens.extraSmallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        horizontalAlignment = Alignment.Start
    ) {
        // List Items
        items(historyItems, key = { it.id }) { historyItem ->
            HistoryItem(historyItem, onItemClick, onDeleteItem, goToListing)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(historyItem: SearchHistory, onUpdateSearch: (String) -> Unit, onDeleteItem: (Long) -> Unit, goToListing: () -> Unit) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteItem(historyItem.id)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
       state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection

            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> colors.transparent
                SwipeToDismissBoxValue.Settled -> colors.transparent
                SwipeToDismissBoxValue.EndToStart -> colors.errorLayoutBackground
                else -> colors.transparent
            }
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(start = dimens.largePadding),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        painterResource(drawables.cancelIcon),
                        contentDescription = null,
                        tint = colors.inactiveBottomNavIconColor,
                    )
                }
            }
        },
    ){
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(color = colors.white)
                .wrapContentHeight()
                .clickable {
                    onUpdateSearch(historyItem.query)
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = historyItem.query,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = dimens.mediumPadding)
                )

                IconButton(
                    onClick = {
                        searchData.searchString = historyItem.query
                        searchData.fromSearch = true
                        goToListing()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ){
                    Icon(
                        painter = painterResource(drawables.searchIcon),
                        contentDescription = stringResource(strings.searchTitle),
                        modifier = Modifier.size(dimens.smallIconSize),
                        tint = colors.black
                    )
                }
            }
        }
    }

}

@Composable
fun FiltersSearchBar(
    modifier: Modifier = Modifier,
    selectedUser: MutableState<Boolean>,
    selectedUserFinished: MutableState<Boolean>,
    searchString: State<String>,
    selectedCategory: MutableState<String?>,
    goToCategory: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.primaryColor)
            .padding(vertical = dimens.smallPadding, horizontal = dimens.smallPadding)
            .wrapContentHeight()
    ) {
        Column {
            Row {
                TextButton(
                    onClick = {
                        searchData.fromSearch = true
                        searchData.searchString = searchString.value
                        goToCategory()
                    },
                    colors = colors.simpleButtonColors
                ) {
                    Text(
                        text = selectedCategory.value ?: stringResource(strings.categoryMain),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                    )
                }
                // Filter button
                IconButton(
                    onClick = { goToCategory() },
                    modifier = modifier
                        .padding(end = dimens.smallPadding),
                ) {
                    Icon(
                        painter = painterResource(drawables.filterIcon),
                        contentDescription = stringResource(strings.parameters),
                        modifier = Modifier.size(dimens.smallIconSize),
                        tint = colors.inactiveBottomNavIconColor
                    )
                }
            }
            Row{
                TextButton(
                    onClick = { selectedUser.value = !selectedUser.value },
                    colors = if (!selectedUser.value) colors.simpleButtonColors else colors.themeButtonColors
                ) {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(strings.searchUserStringChoice),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 150.dp)
                                .padding(end = 2.dp)
                        )
                        if (searchData.searchUsersLots != null) {
                            SmallCancelBtn {

                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(dimens.smallSpacer))
                TextButton(
                    onClick = { selectedUserFinished.value = !selectedUserFinished.value },
                    colors = if (!selectedUserFinished.value) colors.simpleButtonColors else colors.themeButtonColors
                ) {
                    Text(
                        text = stringResource(strings.searchUserFinishedStringChoice),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
