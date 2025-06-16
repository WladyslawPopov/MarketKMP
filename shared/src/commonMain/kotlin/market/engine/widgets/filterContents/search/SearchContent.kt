package market.engine.widgets.filterContents.search

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.collectLatest
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.states.SearchUiState
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent
import market.engine.fragments.root.main.listing.SearchPagesComponents
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.tabs.PageTab
import market.engine.widgets.tabs.TabRow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    uiSearchUiState: SearchUiState,
    searchPages : Value<ChildPages<*, SearchPagesComponents>>,
) {
    val focusManager = LocalFocusManager.current
    val scaffoldState = rememberBottomSheetScaffoldState()
    val searchEvents = uiSearchUiState.searchEvents

    LaunchedEffect(uiSearchUiState){
        snapshotFlow {
            uiSearchUiState.categoryState.openCategory
        }.collectLatest {
            if (it) {
                scaffoldState.bottomSheetState.expand()
                focusManager.clearFocus()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

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
                viewModel = uiSearchUiState.categoryState.categoryViewModel,
                onCompleted = {
                    searchEvents.openSearchCategory(false, true)
                },
                onClose = {
                    searchEvents.openSearchCategory(false, false)
                }
            )
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

                if(uiSearchUiState.tabs.size > 1) {
                    TabRow(
                        uiSearchUiState.tabs,
                        selectedTab = uiSearchUiState.selectedTabIndex,
                        containerColor = colors.primaryColor,
                        modifier = Modifier.fillMaxWidth(),
                    ) { index, tab ->
                        PageTab(
                            tab = tab,
                            selectedTab = uiSearchUiState.selectedTabIndex,
                            currentIndex = index,
                            textStyle = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.clickable {
                                searchEvents.onTabSelect(index)
                            },
                        )
                    }
                }

                ChildPages(
                    pages = searchPages,
                    scrollAnimation = PagesScrollAnimation.Default,
                    onPageSelected = {
                        searchEvents.onTabSelect(it)
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
                stringResource(strings.categoryEnter),
                Modifier.fillMaxWidth(if(isBigScreen.value) 0.8f else 1f)
                    .padding(dimens.smallPadding),
            ) {
                searchEvents.goToListing()
            }
        }
    }
}
