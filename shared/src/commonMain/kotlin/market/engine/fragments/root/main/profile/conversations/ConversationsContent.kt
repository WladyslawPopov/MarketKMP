package market.engine.fragments.root.main.profile.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.Platform
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.NavigationItemUI
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.PlatformWindowType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.filterContents.CustomModalDrawer
import market.engine.widgets.filterContents.filters.DialogsFilterContent
import market.engine.widgets.filterContents.sorts.SortingOrdersContent
import market.engine.widgets.items.ConversationItem
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationsContent(
    component: ConversationsComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItemUI>
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel

    val listingBaseViewModel = viewModel.listingBaseViewModel

    val listingDataState = listingBaseViewModel.listingData.collectAsState()

    val listingData = listingDataState.value.data

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem by viewModel.updateItem.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val toastItem by viewModel.toastItem.collectAsState()

    val activeType by listingBaseViewModel.activeWindowType.collectAsState()

    val selectedItems by listingBaseViewModel.selectItems.collectAsState()

    val listingState = rememberLazyScrollState(viewModel)

    val err by viewModel.errorMessage.collectAsState()

    val error : (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            { OnError(err) {
                listingBaseViewModel.clearAllFilters()
                viewModel.refresh()
            } }
        }else{
            null
        }
    }

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh, activeType) {
        when {
            activeType == ActiveWindowListingType.LISTING -> {
                if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
                    @Composable {
                        if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                            NoItemsFoundLayout(
                                textButton = stringResource(strings.resetLabel)
                            ) {
                                listingBaseViewModel.clearAllFilters()
                                viewModel.refresh()
                            }
                        } else {
                            NoItemsFoundLayout(
                                title = stringResource(strings.simpleNotFoundLabel),
                                icon = drawables.dialogIcon
                            ) {
                                viewModel.refresh()
                            }
                        }
                    }
                } else {
                    null
                }
            }

            else -> {
                null
            }
        }
    }

    CustomModalDrawer(
        modifier = modifier,
        title = stringResource(strings.messageTitle),
        publicProfileNavigationItems = publicProfileNavigationItems,
    ) { mod, drawerState ->
        EdgeToEdgeScaffold(
            topBar = {
                DrawerAppBar(
                    drawerState = drawerState,
                    data = SimpleAppBarData(
                        listItems = listOf(
                            NavigationItemUI(
                                NavigationItem(
                                    title = "",
                                    hasNews = false,
                                    isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                                    badgeCount = null,
                                ),
                                icon = drawables.recycleIcon,
                                tint = colors.inactiveBottomNavIconColor,
                                onClick = {
                                    viewModel.updatePage()
                                }
                            )
                        )
                    ),
                    color = if (!listingState.areBarsVisible.value)
                        colors.primaryColor.copy(alphaBars)
                    else colors.primaryColor
                ) {
                    TextAppBar(
                        stringResource(strings.messageTitle)
                    )
                }

                if (model.message != null) {
                    Row(
                        modifier = Modifier.background(colors.white).fillMaxWidth()
                            .padding(dimens.mediumPadding)
                    ) {
                        Text(
                            stringResource(strings.selectDialogLabel),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.black
                        )
                    }
                }

                val filterBarUiState by listingBaseViewModel.filterBarUiState.collectAsState()

                DeletePanel(
                    selectedItems.size,
                    onCancel = {
                        listingBaseViewModel.clearSelectedItems()
                    },
                    onDelete = {
                        listingBaseViewModel.deleteSelectedItems()
                    }
                )

                FiltersBar(
                    filterBarUiState,
                    isVisible = listingState.areBarsVisible.value &&
                            activeType == ActiveWindowListingType.LISTING,
                )
            },
            onRefresh = {
                viewModel.refresh()
            },
            error = error,
            noFound = noFound,
            isLoading = isLoading.value,
            toastItem = toastItem,
            modifier = modifier.fillMaxSize()
        ) { contentPadding ->
            when (activeType) {
                ActiveWindowListingType.FILTERS -> {
                    DialogsFilterContent(
                        listingData.filters,
                        Modifier.padding(top = contentPadding.calculateTopPadding())
                    ) {
                        listingBaseViewModel.applyFilters(it)
                    }
                }

                ActiveWindowListingType.SORTING -> {
                    SortingOrdersContent(
                        listingData.sort,
                        Modifier.padding(top = contentPadding.calculateTopPadding())
                    ) {
                        listingBaseViewModel.applySorting(it)
                    }
                }

                else -> {
                    PagingLayout(
                        data = data,
                        viewModel = listingBaseViewModel,
                        state = listingState.scrollState,
                        contentPadding = contentPadding,
                        content = { conversation ->
                            ConversationItem(
                                conversation,
                                updateItem,
                                selectedItems.contains(conversation.conversation.id),
                                onSelected = {
                                    if (selectedItems.contains(it)) {
                                        listingBaseViewModel.removeSelectItem(it)
                                    } else {
                                        listingBaseViewModel.addSelectItem(it)
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
