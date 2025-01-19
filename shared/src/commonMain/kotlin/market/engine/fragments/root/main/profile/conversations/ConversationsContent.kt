package market.engine.fragments.root.main.profile.conversations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.exceptions.ProfileDrawer
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.SortingOffersContent
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationsContent(
    component: ConversationsComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()


    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val successToast = stringResource(strings.operationSuccess)

    val refresh = {
        listingData.value.resetScroll()
        viewModel.onRefresh()
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpritation != null && it.interpritation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                viewModel.onRefresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.emptyOffersIcon
            ) {
                listingData.value.resetScroll()
                viewModel.onRefresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(viewModel.updateItem.value) {
        if (viewModel.updateItem.value != null) {
            withContext(Dispatchers.Default) {

                withContext(Dispatchers.Main) {

                    viewModel.updateItem.value = null
                }
            }
        }
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(strings.messageTitle, model.navigationItems)
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        BaseContent(
            topBar = {
                ConversationsAppBar(
                    drawerState = drawerState,
                    modifier = modifier
                )
            },
            onRefresh = {
                refresh()
            },
            error = null,
            noFound = null,
            isLoading = isLoading.value,
            toastItem = viewModel.toastItem,
            modifier = modifier.fillMaxSize()
        ) {
            ListingBaseContent(
                columns = columns,
                listingData = listingData.value,
                searchData = searchData.value,
                data = data,
                baseViewModel = viewModel,
                onRefresh = {
                    refresh()
                },
                noFound = noFound,
                additionalBar = {
                    FiltersBar(
                        searchData.value,
                        listingData.value,
                        isShowGrid = false,
                        onFilterClick = {
                            viewModel.activeFiltersType.value = "filters"
                        },
                        onSortClick = {
                            viewModel.activeFiltersType.value = "sorting"
                        },
                        onRefresh = {
                            refresh()
                        }
                    )
                },
                filtersContent = { isRefreshingFromFilters, onClose ->
                    when (viewModel.activeFiltersType.value) {
                        "filters" -> {

                        }

                        "sorting" -> SortingOffersContent(
                            isRefreshingFromFilters,
                            listingData.value,
                            onClose
                        )
                    }
                },
                item = { conversation ->

                }
            )
        }
    }
}
