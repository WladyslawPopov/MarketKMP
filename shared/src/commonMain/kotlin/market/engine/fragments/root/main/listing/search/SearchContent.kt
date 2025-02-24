package market.engine.fragments.root.main.listing.search

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.root.main.listing.ListingViewModel
import market.engine.widgets.filterContents.CategoryContent

@Composable
fun SearchContent(
    searchData: SD,
    bottomSheetState: BottomSheetState,
    focusRequester : FocusRequester,
    searchViewModel : ListingViewModel,
    closeSearch : () -> Unit,
    goToListing : () -> Unit,
) {
    val isErrorSearch = searchViewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    val history = searchViewModel.responseHistory.collectAsState()

    val searchString = remember { mutableStateOf(searchData.searchString) }
    val selectedUser = remember { mutableStateOf(searchData.userSearch) }
    val selectedUserLogin = remember { mutableStateOf(searchData.userLogin) }
    val selectedUserFinished = remember { mutableStateOf(searchData.searchFinished) }

    val categoryName = remember { mutableStateOf(searchData.searchCategoryName) }
    val categoryId = remember { mutableStateOf(searchData.searchCategoryID) }
    val refreshFromCategory = remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openCategory = remember { mutableStateOf(false) }

    val errorSearch: (@Composable () -> Unit)? = if (isErrorSearch.value.humanMessage != "") {
        { onError(isErrorSearch) { } }
    } else {
        null
    }

    val sd = remember {
        mutableStateOf(
            SD(
                searchString = searchString.value,
                userSearch = selectedUser.value,
                userLogin = selectedUserLogin.value,
                searchFinished = selectedUserFinished.value,
                searchCategoryID = categoryId.value,
            )
        )
    }

    val getSearchFilters = {
        if (selectedUser.value && selectedUserLogin.value == null){
            if (searchString.value != "") {
                searchData.isRefreshing = true
                searchData.userLogin = searchString.value
                searchData.userSearch = selectedUser.value
                searchString.value = ""
            }
        }else{
            if (searchData.userLogin != selectedUserLogin.value){
                searchData.userLogin = selectedUserLogin.value
                searchData.isRefreshing = true
            }

            if (searchData.userSearch != selectedUser.value){
                searchData.userSearch = selectedUser.value
                searchData.isRefreshing = true
            }
        }

        if (searchData.searchString != searchString.value) {
            searchData.searchString = searchString.value
            searchData.isRefreshing = true
        }

        if (searchData.searchFinished != selectedUserFinished.value){
            searchData.searchFinished = selectedUserFinished.value
            searchData.isRefreshing = true
        }

        if (searchData.searchCategoryID != categoryId.value){
            searchData.searchCategoryID = categoryId.value
            searchData.searchCategoryName = categoryName.value
            searchData.isRefreshing = true
        }

        searchViewModel.searchAnalytic(searchData)
    }

    LaunchedEffect(bottomSheetState.isExpanded){
        if (bottomSheetState.isExpanded){
            searchString.value = searchData.searchString
            selectedUser.value = searchData.userSearch
            selectedUserLogin.value = searchData.userLogin
            selectedUserFinished.value = searchData.searchFinished
            categoryId.value = searchData.searchCategoryID
            categoryName.value = searchData.searchCategoryName
        }
    }

    LaunchedEffect(openCategory.value){
        if (openCategory.value){
            scaffoldState.bottomSheetState.expand()
            focusManager.clearFocus()
        }else{
            scaffoldState.bottomSheetState.collapse()
            if (refreshFromCategory.value){
                categoryId.value = sd.value.searchCategoryID
                categoryName.value = sd.value.searchCategoryName
                refreshFromCategory.value = false
            }
        }
    }

    BaseContent(
        error = errorSearch,
        noFound = null,
        toastItem = searchViewModel.toastItem,
        topBar = {
            SearchAppBar(
                searchString = searchString,
                focusRequester,
                onSearchClick = {
                    getSearchFilters()
                    searchViewModel.addHistory(searchString.value)
                    goToListing()
                },
                onUpdateHistory = {
                    searchViewModel.getHistory(searchString.value)
                },
                onBeakClick = {
                    if (scaffoldState.bottomSheetState.isExpanded){
                        searchViewModel.activeFiltersType.value = ""
                    }else {
                        closeSearch()
                    }
                }
            )
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
                    searchData = sd.value,
                    baseViewModel = searchViewModel,
                    isRefresh = refreshFromCategory,
                    isFilters = true,
                ){
                    openCategory.value = false
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    },
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.fillMaxWidth().padding(dimens.smallSpacer))

                FiltersSearchBar(
                    selectedCategory = categoryName,
                    selectedCategoryID = categoryId,
                    selectedUser = selectedUser,
                    selectedUserLogin = selectedUserLogin,
                    selectedUserFinished = selectedUserFinished,
                    goToCategory = {
                        sd.value = SD(
                            searchString = searchString.value,
                            userSearch = selectedUser.value,
                            userLogin = selectedUserLogin.value,
                            searchFinished = selectedUserFinished.value,
                            searchCategoryID = categoryId.value,
                        )

                        openCategory.value = true
                    },
                )

                HistoryLayout(
                    historyItems = history.value,
                    modifier = Modifier.padding(horizontal = dimens.smallPadding),
                    onItemClick = {
                        searchString.value = it
                    },
                    onClearHistory = {
                        searchViewModel.deleteHistory()
                    },
                    onDeleteItem = {
                        searchViewModel.deleteItemHistory(it)
                        searchViewModel.getHistory(searchString.value)
                    },
                    goToListing = {
                        searchString.value = it
                        getSearchFilters()
                        goToListing()
                    }
                )
            }
        }
    }
}
