package market.engine.fragments.root.main.listing.search

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
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.widgets.filterContents.CategoryContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchContent(
    focusRequester : FocusRequester,
    searchString : MutableState<String>,
    selectedCategory : MutableState<String>,
    selectedCategoryID : MutableState<Long>,
    selectedCategoryParentID : MutableState<Long?>,
    selectedCategoryIsLeaf : MutableState<Boolean>,
    selectedUser : MutableState<Boolean>,
    selectedUserLogin : MutableState<String?>,
    selectedUserFinished : MutableState<Boolean>,
    openBottomSheet : MutableState<Boolean>,
    closeSearch : () -> Unit,
    goToListing : () -> Unit,
) {
    val searchViewModel: SearchViewModel = koinViewModel()
    val isErrorSearch = searchViewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    val history = searchViewModel.responseHistory.collectAsState()

    val errorSearch: (@Composable () -> Unit)? = if (isErrorSearch.value.humanMessage != "") {
        { onError(isErrorSearch) { } }
    } else {
        null
    }

    val scaffoldState = rememberBottomSheetScaffoldState()


    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    val searchData = SD(
        searchString = searchString.value,
        searchCategoryID = selectedCategoryID.value,
        userSearch = selectedUser.value,
        userLogin = selectedUserLogin.value,
        searchFinished = selectedUserFinished.value,
        searchCategoryName = selectedCategory.value,
        searchParentID = selectedCategoryParentID.value,
        searchIsLeaf = selectedCategoryIsLeaf.value
    )

    LaunchedEffect(openBottomSheet.value) {
        if (openBottomSheet.value) {
            searchViewModel.getCategories(searchData, LD(), true)
            focusManager.clearFocus()
            scaffoldState.bottomSheetState.expand()
        } else {
            searchViewModel.activeFiltersType.value = ""
            scaffoldState.bottomSheetState.collapse()
            selectedCategory.value = selectedCategory.value
        }
    }

    BaseContent(
        error = errorSearch,
        noFound = null,
        toastItem = searchViewModel.toastItem,
        topBar = {
            SearchAppBar(
                searchString,
                focusRequester,
                onSearchClick = {
                    searchViewModel.searchAnalytic(searchData)
                    searchViewModel.addHistory(searchString.value)
                    goToListing()
                },
                onUpdateHistory = {
                    searchViewModel.getHistory(searchString.value)
                },
                onBeakClick = {
                    if (openBottomSheet.value){
                        openBottomSheet.value = false
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
                    baseViewModel = searchViewModel,
                    complete = {
                        openBottomSheet.value = false
                    },
                    isFilters = true,
                    searchData = searchData,
                    listingData = LD(),
                    searchCategoryId = selectedCategoryID,
                    searchCategoryName = selectedCategory,
                    searchParentID = selectedCategoryParentID,
                    searchIsLeaf = selectedCategoryIsLeaf,
                    isRefreshingFromFilters = isRefreshingFromFilters
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }.padding(dimens.smallPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FiltersSearchBar(
                    selectedCategory = selectedCategory,
                    selectedCategoryID = selectedCategoryID,
                    selectedUser = selectedUser,
                    selectedUserLogin = selectedUserLogin,
                    selectedUserFinished = selectedUserFinished,
                    modifier = Modifier.padding(dimens.extraSmallPadding),
                    goToCategory = {
                        searchViewModel.activeFiltersType.value = "categories"
                        openBottomSheet.value = true
                    },
                )

                HistoryLayout(
                    historyItems = history.value,
                    modifier = Modifier.clip(MaterialTheme.shapes.medium).fillMaxWidth(),
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
                        searchViewModel.searchAnalytic(searchData)
                        goToListing()
                    }
                )
            }
        }
    }
}
