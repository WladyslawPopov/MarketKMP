package market.engine.fragments.root.main.listing.search

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.root.main.listing.ListingViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.filterContents.CategoryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    openSearch: MutableState<Boolean>,
    openSearchCategory: MutableState<Boolean>,
    searchData: SD,
    searchViewModel : ListingViewModel,
    catBack : MutableState<Boolean>,
    closeSearch : () -> Unit,
    goToListing : () -> Unit,
) {
    val isErrorSearch = searchViewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    val history = searchViewModel.responseHistory.collectAsState()

    val searchStringTextField = remember { mutableStateOf(TextFieldValue(searchData.searchString)) }
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
        {
            onError(isErrorSearch) {
                searchViewModel.onError(ServerErrorException())
            }
        }
    } else {
        null
    }

    val sd = remember {
        mutableStateOf(
            SD(
                searchCategoryID = searchData.searchCategoryID,
                searchCategoryName = searchData.searchCategoryName,
                searchParentID = searchData.searchParentID,
                searchIsLeaf = searchData.searchIsLeaf,
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
                searchStringTextField.value = TextFieldValue()
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

        searchViewModel.searchAnalytic(searchData)
    }

    LaunchedEffect(openSearch.value){
        if (openSearch.value) {
            searchString.value = searchData.searchString
            searchStringTextField.value = TextFieldValue(searchData.searchString)
            selectedUser.value = searchData.userSearch
            selectedUserLogin.value = searchData.userLogin
            selectedUserFinished.value = searchData.searchFinished
            categoryId.value = searchData.searchCategoryID
            categoryName.value = searchData.searchCategoryName

            searchViewModel.getHistory(searchString.value)
        }
    }

    LaunchedEffect(openSearchCategory.value){
        if (openSearchCategory.value){
            scaffoldState.bottomSheetState.expand()
            focusManager.clearFocus()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    BaseContent(
        error = errorSearch,
        noFound = null,
        toastItem = searchViewModel.toastItem,
        topBar = {
            if (!scaffoldState.bottomSheetState.isExpanded) {
                SearchAppBar(
                    searchString = searchStringTextField,
                    onSearchClick = {
                        getSearchFilters()
                        searchViewModel.addHistory(searchString.value)
                        goToListing()
                    },
                    onUpdateHistory = {
                        searchString.value = it
                        searchViewModel.getHistory(it)
                    },
                    openSearch = openSearch,
                    onBeakClick = {
                        closeSearch()
                    }
                )
            }else{
                TopAppBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.primaryColor
                    ),
                    title = {},
                    navigationIcon = {
                        SmallIconButton(
                            drawables.closeBtn,
                            colors.black
                        ){
                            openSearchCategory.value = false
                        }
                    }
                )
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
                    isOpen = openCategory,
                    searchData = sd.value,
                    baseViewModel = searchViewModel,
                    isRefresh = refreshFromCategory,
                    isFilters = true,
                    onBackClicked = catBack
                ){
                    if (refreshFromCategory.value){
                        categoryId.value = sd.value.searchCategoryID
                        categoryName.value = sd.value.searchCategoryName

                        searchData.searchCategoryID = sd.value.searchCategoryID
                        searchData.searchCategoryName = sd.value.searchCategoryName
                        searchData.searchParentID = sd.value.searchParentID
                        searchData.searchIsLeaf = sd.value.searchIsLeaf
                        searchData.isRefreshing = true

                        refreshFromCategory.value = false
                    }
                    openSearchCategory.value = false
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ){
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
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
                                searchCategoryID = searchData.searchCategoryID,
                                searchCategoryName = searchData.searchCategoryName,
                                searchParentID = searchData.searchParentID,
                                searchIsLeaf = searchData.searchIsLeaf,
                            )

                            openCategory.value = true
                            openSearchCategory.value = true
                        },
                        clearCategory = {
                            categoryId.value = 1L
                            categoryName.value = searchViewModel.catDef.value
                            searchData.clear(searchViewModel.catDef.value)
                        }
                    )

                    HistoryLayout(
                        historyItems = history.value,
                        modifier = Modifier.padding(horizontal = dimens.smallPadding),
                        onItemClick = {
                            searchString.value = it
                            searchStringTextField.value = TextFieldValue(it)
                        },
                        onClearHistory = {
                            searchViewModel.deleteHistory()
                        },
                        onDeleteItem = {
                            searchViewModel.deleteItemHistory(it)
                        },
                        goToListing = {
                            searchString.value = it
                            searchStringTextField.value = TextFieldValue(it)
                            getSearchFilters()
                            goToListing()
                        }
                    )
                }

                AcceptedPageButton(
                    strings.categoryEnter,
                    Modifier.fillMaxWidth(if(isBigScreen.value) 0.8f else 1f).padding(dimens.smallPadding).align(Alignment.BottomCenter),
                ) {
                    getSearchFilters()
                    searchViewModel.addHistory(searchString.value)
                    goToListing()
                }
            }
        }
    }
}
