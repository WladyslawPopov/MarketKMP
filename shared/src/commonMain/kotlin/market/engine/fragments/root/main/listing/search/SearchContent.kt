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
import androidx.compose.material3.TabRowDefaults
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
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.data.items.Tab
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent
import market.engine.fragments.root.main.listing.ListingUiState
import market.engine.fragments.root.main.listing.ListingViewModel
import market.engine.fragments.root.main.listing.SearchEvents
import market.engine.fragments.root.main.listing.SearchPagesComponents
import market.engine.fragments.root.main.listing.SearchUiState
import market.engine.widgets.bars.SimpleAppBar
import market.engine.widgets.bars.SimpleAppBarData
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.tabs.PageTab
import market.engine.widgets.tabs.TabRow
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    uiSearchUiState: SearchUiState,
    searchEvents: SearchEvents,
    searchPages : Value<ChildPages<*, SearchPagesComponents>>,
) {
    val focusManager = LocalFocusManager.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    val openCategory = remember { mutableStateOf(false) }
    val refreshFromCategory = remember { mutableStateOf(false) }

    val sd = remember(uiSearchUiState) {
        SD(
            searchCategoryID = uiSearchUiState.searchCategoryID,
            searchCategoryName = uiSearchUiState.searchCategoryName,
            searchParentID = uiSearchUiState.searchParentID,
            searchIsLeaf = uiSearchUiState.searchIsLeaf,
        )
    }

    LaunchedEffect(uiSearchUiState.openCategory){
        if (uiSearchUiState.openCategory){
            scaffoldState.bottomSheetState.expand()
            focusManager.clearFocus()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    val selectedTabIndex = remember {
        mutableStateOf(0)
    }

    val tabs = buildList {
        add(
            Tab(stringResource(strings.searchHistory))
        )
        if (UserData.token != "") {
            add(Tab(stringResource(strings.mySubscribedTitle)))
        }
    }


    BaseContent(
        error = null,
        isLoading = false,
        onRefresh = {
            searchEvents.onRefresh()
        },
        noFound = null,
        topBar = {
            if (!scaffoldState.bottomSheetState.isExpanded) {
                if (uiSearchUiState.appBarData != null) {
                    SimpleAppBar(
                        data = uiSearchUiState.appBarData
                    )
                }
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
//                            openSearchCategory.value = false
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
//                CategoryContent(
//                    isOpen = openCategory.value,
//                    searchData = sd.value,
//                    baseViewModel = searchViewModel,
//                    isRefresh = refreshFromCategory,
//                    isFilters = true,
//                    onBackClicked = catBack
//                ){
//                    if (refreshFromCategory.value){
//                        categoryId.value = sd.value.searchCategoryID
//                        categoryName.value = sd.value.searchCategoryName
//
//                        searchData.searchCategoryID = sd.value.searchCategoryID
//                        searchData.searchCategoryName = sd.value.searchCategoryName
//                        searchData.searchParentID = sd.value.searchParentID
//                        searchData.searchIsLeaf = sd.value.searchIsLeaf
//                        searchData.isRefreshing = true
//
//                        refreshFromCategory.value = false
//                    }
////                    openSearchCategory.value = false
//                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
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

                    FiltersSearchBar(uiSearchUiState, searchEvents)

                    TabRow(
                        tabs,
                        selectedTab = selectedTabIndex.value,
                        containerColor = colors.primaryColor,
                        modifier = Modifier.fillMaxWidth(),
                    ){ index, tab ->
                        PageTab(
                            tab = tab,
                            selectedTab = selectedTabIndex.value,
                            currentIndex = index,
                            textStyle = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.clickable {
                                searchEvents.onTabSelect(index)
                            },
                        )
                    }

                    ChildPages(
                        pages = searchPages,
                        scrollAnimation = PagesScrollAnimation.Default,
                        onPageSelected = {
                            searchEvents.onTabSelect(it)
                            selectedTabIndex.value = it
                            focusManager.clearFocus()
                        }
                    ) { _, page ->
                        when(page){
                            is SearchPagesComponents.HistoryChild -> {
                                HistoryLayout(
                                    historyItems = uiSearchUiState.searchHistory,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = dimens.smallPadding),
                                    onItemClick = { item ->
                                        searchEvents.editHistoryItem(item)
                                    },
                                    onClearHistory = {
                                        searchEvents.onDeleteHistory()
                                    },
                                    onDeleteItem = {
                                        searchEvents.onDeleteHistoryItem(it)
                                    },
                                    goToListing = { item ->
                                        searchEvents.onHistoryItemClicked(item)
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
                    searchEvents.goToListing()
                }
            }
        }
    }
}
