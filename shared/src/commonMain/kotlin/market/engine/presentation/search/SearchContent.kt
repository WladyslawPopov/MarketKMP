package market.engine.presentation.search

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.types.CategoryScreenType
import market.engine.widgets.exceptions.onError
import market.engine.presentation.base.BaseContent

@Composable
fun SearchContent(
    component: SearchComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.globalData.listingData.searchData.subscribeAsState()
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

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var searchString by remember { mutableStateOf(TextFieldValue(searchData.value.searchString ?: "")) }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        error = error,
        topBar = {
            SearchAppBar(
                modifier,
                searchString,
                focusRequester,
                onSearchClick = {
                    searchData.value.fromSearch = true
                    searchData.value.searchString = searchString.text
                    component.goToListing()
                },
                onUpdateHistory = {
                    searchString = searchString.copy(it)
                    component.updateHistory(it)
                }
            ) {
                component.onCloseClicked(CategoryScreenType.CATEGORY)
            }
        },
        onRefresh = {
            searchString = searchString.copy("")
            component.updateHistory("")
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            FiltersSearchBar(
                modifier = modifier,
                selectedUser = selectedUser,
                selectedUserFinished = selectedUserFinished,
                selectedCategory = selectedCategory,
                searchData = searchData,
                goToCategory = { component.goToCategory() }
            )

            HistoryLayout(
                historyItems = history.value,
                modifier = modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
                onItemClick = {
                    searchString = searchString.copy(it)
                    component.updateHistory(it)
                },
                onClearHistory = { component.deleteHistory() },
                onDeleteItem = { component.deleteItemHistory(it) },
                goToListing = {
                    searchData.value.fromSearch = true
                    searchData.value.searchString = it
                    component.goToListing()
                }
            )
        }
    }
}




