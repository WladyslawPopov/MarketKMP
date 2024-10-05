package market.engine.presentation.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.SD
import market.engine.core.types.CategoryScreenType
import market.engine.widgets.exceptions.onError
import market.engine.presentation.base.BaseContent
import market.engine.shared.SearchHistory
import market.engine.widgets.buttons.ActiveStringButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.StringButton
import market.engine.widgets.exceptions.dismissBackground
import market.engine.widgets.items.historyItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchContent(
    component: SearchComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.searchData.collectAsState()
    val modelState = component.model.subscribeAsState()
    val model = modelState.value

    val isLoading = model.isLoading.collectAsState()
    val isError = model.isError.collectAsState()
    val history = model.history.collectAsState()

    val selectedUser = remember { mutableStateOf(searchData.value.searchChoice == "user_search") }
    val selectedUserFinished = remember { mutableStateOf(searchData.value.searchFinished) }
    val selectedCategory = remember { mutableStateOf(searchData.value.searchCategoryName) }

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(model.isError.value) { } }
    }else{
        null
    }

    var searchString by remember {
        mutableStateOf(searchData.value.searchString ?: "")
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        error = error,
        topBar = {
            SearchAppBar(
                modifier,
                searchString,
                searchData,
                onSearchClick = {
                    component.goToListing()
                },
                onUpdateHistory = {
                    searchString = it
                    component.updateHistory(it)
                }
            ) {
                component.onCloseClicked(CategoryScreenType.CATEGORY)
            }
        },
        onRefresh = {
            searchString = ""
            component.updateHistory("")
        }
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
                searchData = searchData,
                goToCategory = { component.onCloseClicked(CategoryScreenType.CATEGORY) }
            )

            HistoryLayout(
                historyItems = history.value,
                modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
                onItemClick = {
                    searchString = it
                    component.updateHistory(it)
                },
                onClearHistory = { component.deleteHistory() },
                onDeleteItem = { component.deleteItemHistory(it) },
                goToListing = {
                    searchData.value.searchString = it
                    component.goToListing()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryLayout(
    historyItems: List<SearchHistory>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    goToListing: (String) -> Unit
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

        StringButton(
            stringResource(strings.clear),
            colors.solidGreen,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            onClearHistory()
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
                    dismissBackground(dismissState)
                },
            ){
                historyItem(historyItem, onItemClick, goToListing)
            }
        }
    }
}




@Composable
fun FiltersSearchBar(
    modifier: Modifier = Modifier,
    selectedUser: MutableState<Boolean>,
    selectedUserFinished: MutableState<Boolean>,
    searchData: State<SD>,
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
                ActiveStringButton(
                    text = selectedCategory.value ?: stringResource(strings.categoryMain),
                    color =  colors.simpleButtonColors,
                    onClick = {
                        searchData.value.fromSearch = true
                        goToCategory()
                    }
                )

                SmallIconButton(
                    icon = drawables.filterIcon,
                    contentDescription = stringResource(strings.parameters),
                    color = colors.inactiveBottomNavIconColor
                ){
                    goToCategory()
                }
            }
            Row {

                ActiveStringButton(
                    stringResource(strings.searchUserStringChoice),
                    if (!selectedUser.value) colors.simpleButtonColors else colors.themeButtonColors,
                    { selectedUser.value = !selectedUser.value },
                    {
                        if (searchData.value.searchUsersLots != null) {
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifier = modifier,
                            ){

                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                ActiveStringButton(
                    text = stringResource(strings.searchUserFinishedStringChoice),
                    color = if (!selectedUser.value) colors.simpleButtonColors else colors.themeButtonColors,
                    onClick = { selectedUserFinished.value = !selectedUserFinished.value  },
                )
            }
        }
    }
}
