package market.engine.presentation.listing.search

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import market.engine.common.AnalyticsFactory
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.presentation.base.BaseContent
import market.engine.widgets.exceptions.onError
import market.engine.widgets.filterContents.CategoryContent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchContent(
    searchData : SD,
    focusRequester : FocusRequester,
    searchString : MutableState<TextFieldValue>,
    selectedCategory : MutableState<String>,
    selectedUser : MutableState<Boolean>,
    selectedUserLogin : MutableState<String?>,
    selectedUserFinished : MutableState<Boolean>,
    onClose : () -> Unit,
    goToListing : () -> Unit,
) {
    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    val searchViewModel : SearchViewModel = koinViewModel()

    val isLoadingSearch = searchViewModel.isShowProgress.collectAsState()
    val isErrorSearch = searchViewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    val history = searchViewModel.responseHistory.collectAsState()

    val defCat = stringResource(strings.categoryMain)

    val errorSearch : (@Composable () -> Unit)? = if (isErrorSearch.value.humanMessage != "") {
        { onError(isErrorSearch.value) { } }
    }else{
        null
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            searchViewModel.setLoading(true)
            searchViewModel.getCategories(searchData, LD(), true)
            focusManager.clearFocus()
            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
            selectedCategory.value = searchData.searchCategoryName ?: defCat
        }
    }

    BaseContent(
        error = errorSearch,
        isLoading = isLoadingSearch.value,
        noFound = null,
        toastItem = searchViewModel.toastItem,
        onRefresh = {
            searchViewModel.getHistory(searchString.value.text)
        },
        topBar = {
            SearchAppBar(
                searchString.value,
                focusRequester,
                onSearchClick = {
                    getSearchFilters(searchData, searchString.value.text)
                    if (searchData.isRefreshing) {
                        searchViewModel.addHistory(searchData)
                    }
                    goToListing()
                },
                onUpdateHistory = {
                    searchString.value = searchString.value.copy(it)
                    searchViewModel.getHistory(searchString.value.text)
                }
            ) {
                onClose()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            sheetContentColor = colors.primaryColor,
            sheetBackgroundColor = colors.primaryColor,
            contentColor = colors.primaryColor,
            backgroundColor = colors.primaryColor,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = false,
            sheetContent = {
                CategoryContent(
                    baseViewModel = searchViewModel,
                    searchData = searchData,
                    goListing = {
                        openBottomSheet.value = false
                    },
                    isFilters = true
                )
            },
        ) {
            Column(
                modifier = Modifier
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
                    selectedCategory = selectedCategory,
                    selectedUser = selectedUser,
                    selectedUserLogin = selectedUserLogin,
                    selectedUserFinished = selectedUserFinished,
                    modifier = Modifier,
                    searchData = searchData,
                    goToCategory = {
                        openBottomSheet.value = !openBottomSheet.value
                    },
                )

                HistoryLayout(
                    historyItems = history.value,
                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
                    onItemClick = {
                        searchString.value = searchString.value.copy(it)
                        getSearchFilters(searchData, it)
                    },
                    onClearHistory = {
                        searchViewModel.deleteHistory()
                    },
                    onDeleteItem = {
                        searchViewModel.deleteItemHistory(it)
                    },
                    goToListing = {
                        getSearchFilters(searchData, it)

                        if (searchData.isRefreshing) {
                            val event = mapOf(
                                "search_query" to searchData.searchString,
                                "visitor_id" to UserData.login,
                                "search_cat_id" to searchData.searchCategoryID,
                                "user_search" to searchData.userSearch,
                                "user_search_login" to searchData.userLogin,
                                "user_search_id" to searchData.userID
                            )
                            analyticsHelper.reportEvent("search_for_item", event)
                        }

                        goToListing()
                    }
                )
            }
        }
    }
}

fun getSearchFilters(searchData: SD, it: String) {
    if (searchData.userSearch && searchData.userLogin == null){
        if (it != "") {
            searchData.searchString = null
            searchData.userLogin = it
            searchData.isRefreshing = true
        } else {
            searchData.searchString = null
            searchData.userSearch = false
            searchData.searchFinished = false
        }
    }else{
        searchData.searchString = it
        searchData.isRefreshing = true
    }
}


