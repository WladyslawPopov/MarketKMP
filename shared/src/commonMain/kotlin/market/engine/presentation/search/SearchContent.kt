package market.engine.presentation.search

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.baseFilters.SD
import market.engine.core.constants.ThemeResources.dimens
import market.engine.presentation.base.BaseContent
import market.engine.widgets.exceptions.onError

@Composable
fun SearchContent(
    component: SearchComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val searchViewModel = model.searchViewModel

    val searchData = searchViewModel.searchData.subscribeAsState()
    val isLoading = searchViewModel.isShowProgress.collectAsState()
    val isError = searchViewModel.errorMessage.collectAsState()
    val history = searchViewModel.responseHistory.collectAsState()

    val selectedUser = remember { mutableStateOf(searchData.value.userSearch) }
    val selectedUserFinished = remember { mutableStateOf(searchData.value.searchFinished) }
    val selectedCategory = remember { mutableStateOf(searchData.value.searchCategoryName) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var searchString by remember { mutableStateOf(TextFieldValue(searchData.value.searchString ?: "")) }


    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(isError.value) { } }
    }else{
        null
    }

    BaseContent(
        error = error,
        isLoading = isLoading.value,
        noFound = null,
        toastItem = searchViewModel.toastItem,
        onRefresh = {
            searchString = searchString.copy("")
            component.updateHistory(searchString.text)
        },
        topBar = {
            SearchAppBar(
                modifier,
                searchString,
                focusRequester,
                onSearchClick = {
                    getSearchFilters(searchData, searchString.text)
                    component.goToListing()
                },
                onUpdateHistory = {
                    searchString = searchString.copy(it)
                    component.updateHistory(it)
                }
            ) {
                getSearchFilters(searchData, searchString.text)
                component.onCloseClicked()
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimens.mediumPadding)
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
                goToCategory = {
                    getSearchFilters(searchData, searchString.text)
                    component.goToCategory()
                },
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
                    getSearchFilters(searchData, it)
                    component.goToListing()
                }
            )
        }
    }
}

fun getSearchFilters(searchData: State<SD>, it: String) {
    if (searchData.value.userSearch && searchData.value.userLogin == null){
        if (it != "") {
            searchData.value.searchString = null
            searchData.value.userLogin = it
            searchData.value.isRefreshing = true
        } else {
            searchData.value.searchString = null
            searchData.value.userSearch = false
            searchData.value.searchFinished = false
        }
    }else{
        searchData.value.searchString = it
        searchData.value.isRefreshing = true
    }
}
