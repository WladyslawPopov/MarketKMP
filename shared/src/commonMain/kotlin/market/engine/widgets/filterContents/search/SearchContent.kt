package market.engine.widgets.filterContents.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.SearchUiState
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent
import market.engine.fragments.root.main.listing.SearchPagesComponents
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.filterContents.CustomBottomSheet
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.tabs.PageTab
import market.engine.widgets.tabs.TabRow
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.stringResource


@Composable
fun SearchContent(
    uiSearchUiState: SearchUiState,
    searchPages : Value<ChildPages<*, SearchPagesComponents>>,
) {
    val focusManager = LocalFocusManager.current
    val searchEvents = uiSearchUiState.searchEvents
    val appBarData = uiSearchUiState.appBarData
    val openCategory = uiSearchUiState.categoryState.openCategory
    val searchStringState = uiSearchUiState.searchString

    val searchString = remember { mutableStateOf(TextFieldValue(searchStringState)) }

    EdgeToEdgeScaffold(
        modifier = Modifier.fillMaxSize(),
        isLoading = false,
        topBar = {
            if (!openCategory) {
                SimpleAppBar(
                    modifier = Modifier,
                    data = appBarData
                ) {
                    SearchTextField(
                        !openCategory,
                        searchString.value,
                        onValueChange = { newVal ->
                            searchString.value = newVal
                            searchEvents.updateSearch(
                                newVal.text
                            )
                        },
                        goToListing = {
                            searchEvents.goToListing()
                        },
                        onClearSearch = {
                            searchEvents.clearSearch()
                        }
                    )
                }
            }else{
                Spacer(Modifier.safeContentPadding())
            }
        },
    ){ contentPadding ->
        CustomBottomSheet(
            initValue = openCategory,
            contentPadding = contentPadding,
            onClosed = {
                searchEvents.openSearchCategory(value = false, complete = false)
            },
            sheetContent = {
                CategoryContent(
                    viewModel = uiSearchUiState.categoryState.categoryViewModel,
                    onCompleted = {
                        searchEvents.openSearchCategory(value = false, complete = true)
                    },
                    onClose = {
                        searchEvents.openSearchCategory(value = false, complete = false)
                    }
                )
            }
        ){
            Box(
                modifier = Modifier
                .padding(contentPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = dimens.largePadding)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Spacer(modifier = Modifier.fillMaxWidth().padding(dimens.smallSpacer))

                    FiltersSearchBar(uiSearchUiState, searchEvents)

                    if (uiSearchUiState.tabs.size > 1) {
                        TabRow(
                            uiSearchUiState.tabs,
                            dividerColor = colors.transparent,
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
                        when (page) {
                            is SearchPagesComponents.HistoryChild -> {
                                HistoryLayout(
                                    historyItems = uiSearchUiState.searchHistory,
                                    modifier = Modifier.fillMaxSize()
                                        .padding(horizontal = dimens.smallPadding),
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
                                    Modifier.padding(bottom = dimens.largePadding),
                                )
                            }
                        }
                    }
                }

                AcceptedPageButton(
                    stringResource(strings.categoryEnter),
                    Modifier.align(Alignment.BottomCenter)
                ) {
                    searchEvents.goToListing()
                }
            }
        }
    }
}
