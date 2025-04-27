package market.engine.fragments.root.main.listing.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.Tab
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent
import market.engine.fragments.root.main.listing.ListingViewModel
import market.engine.fragments.root.main.listing.SearchPagesComponents
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.tabs.PageTab
import market.engine.widgets.tabs.TabRow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    openSearch: MutableState<Boolean>,
    openSearchCategory: MutableState<Boolean>,
    searchData: SD,
    searchViewModel : ListingViewModel,
    catBack : MutableState<Boolean>,
    searchPages : Value<ChildPages<*, SearchPagesComponents>>,
    onTabSelect : (Int) -> Unit,
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
        searchViewModel.addHistory(
            searchString.value,
            if(selectedUserLogin.value == null) selectedUser.value else false,
            selectedUserFinished.value
        )

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

    val isLoading = searchViewModel.isShowProgress.collectAsState()

    val selectedTabIndex = remember {
        mutableStateOf(0)
    }

    val tabs = listOf(
        Tab(
            stringResource(strings.searchHistory),
        ),
        Tab(
            stringResource(strings.mySubscribedTitle),
        ),
    )

    BaseContent(
        error = errorSearch,
        isLoading = isLoading.value,
        onRefresh = {
            searchViewModel.setLoading(true)
            searchViewModel.onError(ServerErrorException())
            getSearchFilters()
            searchViewModel.getHistory(searchString.value)
            searchViewModel.viewModelScope.launch {
                delay(1000)
                searchViewModel.setLoading(false)
            }
        },
        noFound = null,
        toastItem = searchViewModel.toastItem,
        topBar = {
            if (!scaffoldState.bottomSheetState.isExpanded) {
                SearchAppBar(
                    searchString = searchStringTextField,
                    onSearchClick = {
                        getSearchFilters()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ){
                Column(
                    modifier = Modifier
                        .weight(1f)
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

                    TabRow(
                        tabs,
                        selectedTab = selectedTabIndex.value,
                        edgePadding = dimens.smallPadding,
                        containerColor = colors.primaryColor,
                        modifier = Modifier.fillMaxWidth(),
                    ){ index, tab ->
                        PageTab(
                            tab = tab,
                            selectedTab = selectedTabIndex.value,
                            currentIndex = index,
                            textStyle = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.clickable {
                                onTabSelect(index)
                            },
                        )
                    }

                    ChildPages(
                        pages = searchPages,
                        scrollAnimation = PagesScrollAnimation.Default,
                        onPageSelected = {
                            onTabSelect(it)
                            selectedTabIndex.value = it
                            focusManager.clearFocus()
                        }
                    ) { _, page ->
                        when(page){
                            is SearchPagesComponents.HistoryChild -> {
                                HistoryLayout(
                                    historyItems = history.value,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = dimens.smallPadding),
                                    onItemClick = { item ->
                                        searchString.value = item.query
                                        selectedUser.value = item.isUsersSearch
                                        selectedUserFinished.value = item.isFinished
                                        searchStringTextField.value =
                                            searchStringTextField.value.copy(
                                                text = item.query,
                                                selection = TextRange(item.query.length)
                                            )
                                        searchViewModel.deleteItemHistory(item.id)
                                    },
                                    onClearHistory = {
                                        searchViewModel.deleteHistory()
                                    },
                                    onDeleteItem = {
                                        searchViewModel.deleteItemHistory(it)
                                    },
                                    goToListing = { item ->
                                        searchString.value = item.query
                                        selectedUser.value = item.isUsersSearch
                                        selectedUserFinished.value = item.isFinished
                                        searchStringTextField.value =
                                            searchStringTextField.value.copy(
                                                text = item.query,
                                                selection = TextRange(item.query.length)
                                            )
                                        getSearchFilters()
                                        goToListing()
                                    }
                                )
                            }
                            is SearchPagesComponents.SubscriptionsChild -> {
                                SubscriptionsContent(
                                    page.component,
                                    Modifier
                                )
                            }
                        }
                    }
                }

                AcceptedPageButton(
                    strings.categoryEnter,
                    Modifier.fillMaxWidth(if(isBigScreen.value) 0.8f else 1f)
                        .padding(dimens.smallPadding),
                ) {
                    getSearchFilters()
                    goToListing()
                }
            }
        }
    }
}
