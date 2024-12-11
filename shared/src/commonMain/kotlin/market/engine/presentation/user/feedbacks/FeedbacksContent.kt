package market.engine.presentation.user.feedbacks

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import market.engine.core.filtersObjects.ReportFilters
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.types.ReportPageType
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.PagingCounterBar
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.grids.PagingList
import market.engine.widgets.lists.getDropdownMenu
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbacksContent(
    index : Int,
    component : FeedbacksComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.feedbacksViewModel
    val listingData = viewModel.listingData.data.subscribeAsState()
    val searchData = viewModel.listingData.searchData.subscribeAsState()
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val filters = listOf(
        "Все (Заказы)",
        "Положительные",
        "Негативные",
        "Нейтральные"
    )

    val noFound = @Composable {
        showNoItemLayout(
            textButton = stringResource(strings.refreshButton)
        ) {
            when (component.model.value.type) {
                ReportPageType.ALL_REPORTS -> {
                    listingData.value.filters = arrayListOf()
                    listingData.value.filters.addAll(ReportFilters.filtersAll)
                    listingData.value.filters.find { it.key == "user_id" }?.value =
                        component.model.value.userId.toString()
                }

                ReportPageType.FROM_BUYERS -> {
                    listingData.value.filters = arrayListOf()
                    listingData.value.filters.addAll(ReportFilters.filtersFromBuyers)
                    listingData.value.filters.find { it.key == "user_id" }?.value =
                        component.model.value.userId.toString()
                }

                ReportPageType.FROM_SELLERS -> {
                    listingData.value.filters = arrayListOf()
                    listingData.value.filters.addAll(ReportFilters.filtersFromSellers)
                    listingData.value.filters.find { it.key == "user_id" }?.value =
                        component.model.value.userId.toString()
                }

                ReportPageType.FROM_USER -> {
                    listingData.value.filters = arrayListOf()
                    listingData.value.filters.addAll(ReportFilters.filtersFromUsers)
                    listingData.value.filters.find { it.key == "user_id" }?.value =
                        component.model.value.userId.toString()
                }

                ReportPageType.ABOUT_ME -> {

                }
            }
            component.onRefresh()
        }
    }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)
    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    data.loadState.apply {
        when {
            refresh is LoadStateNotLoading && data.itemCount > 0 && !isLoading.value -> {
                error = null
                noItem = null
            }

            refresh is LoadStateNotLoading && data.itemCount < 1 && !isLoading.value -> {
                noItem = {
                    noFound()
                }
            }
            refresh is LoadStateError && !isLoading.value -> {
                error = {
                    onError(
                        ServerErrorException(
                            (data.loadState.refresh as LoadStateError).error.message ?: "", ""
                        )
                    ) {
                        data.retry()
                    }
                }
            }
        }
    }

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = listingData.value.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listingData.value.firstVisibleItemScrollOffset
    )

    val selectedTabIndex by rememberUpdatedState(index)
    val currentFilter by remember { mutableStateOf("Все (Заказы)") }

    var showUpButton by remember { mutableStateOf(false) }
    var showDownButton by remember { mutableStateOf(false) }

    val currentIndex by remember {
        derivedStateOf {
            state.firstVisibleItemIndex + if(listingData.value.totalCount > 1) 2 else 1
        }
    }

    LaunchedEffect(state.firstVisibleItemIndex){
        showUpButton = 2 < (state.firstVisibleItemIndex / listingData.value.pageCountItems)
        showDownButton = listingData.value.prevIndex != null &&
                state.firstVisibleItemIndex < (listingData.value.prevIndex ?: 0)
    }

    Column {
        FeedbackTabs(
            selectedTab = selectedTabIndex,
            onTabSelected = { index ->
                // Изменяем Tab type, вызываем component.selectFeedbackPage(...)
            }
        )

        getDropdownMenu(
            selectedText = currentFilter,
            selects = filters,
            onClearItem = {

            },
            onItemClick = {

            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            ) {
                when {
                    error != null -> item { error?.invoke() }
                    noItem != null -> item { noFound() }
                    else -> {
                        items(data.itemCount) {
                            val item = data[it]
                            if (item != null) {
                                FeedbackItem(
                                    item,
                                    onClickReporter = {

                                    },
                                    onClickSnapshot = {

                                    },
                                    onClickOrder = {

                                    }
                                )
                            }
                        }
                    }
                }
            }

            PagingCounterBar(
                currentPage = currentIndex,
                totalPages = listingData.value.totalCount,
                modifier = Modifier.align(Alignment.BottomStart),
                showUpButton = showUpButton,
                showDownButton = showDownButton,
                onClick = {
                    when{
                        showUpButton -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                listingData.value.prevIndex = currentIndex
                                state.scrollToItem(0)
                                showUpButton = false
                                showDownButton = true
                            }
                        }
                        showDownButton -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                state.scrollToItem(listingData.value.prevIndex ?: 1)
                                listingData.value.prevIndex = null
                                showDownButton = false
                                showUpButton = true
                            }
                        }
                    }
                }
            )
        }
    }
}
