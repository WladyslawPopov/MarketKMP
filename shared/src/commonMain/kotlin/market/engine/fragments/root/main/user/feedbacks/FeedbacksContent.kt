package market.engine.fragments.root.main.user.feedbacks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.filtersObjects.ReportFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.core.data.types.ReportPageType
import market.engine.fragments.base.BaseContent
import market.engine.widgets.bars.PagingCounterBar
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbacksContent(
    aboutMe : String? = null,
    component : FeedbacksComponent,
    onScrollDirectionChange: (isAtTop: Boolean) -> Unit
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.feedbacksViewModel
    val listingData = viewModel.listingData.value.data
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.scrollItem.value,
        initialFirstVisibleItemScrollOffset = viewModel.offsetScrollItem.value
    )

    val type = model.type
    val userId = model.userId

    val htmlText = rememberRichTextState()

    val filters = listOf(
        stringResource(strings.allFilterParams),
        stringResource(strings.positiveFilterParams),
        stringResource(strings.negativeFilterParams),
        stringResource(strings.neutralFilterParams)
    )

    var showUpButton by remember { mutableStateOf(false) }
    var showDownButton by remember { mutableStateOf(false) }

    val currentIndex by remember {
        derivedStateOf {
            state.firstVisibleItemIndex + if(listingData.value.totalCount > 1) 2 else 1
        }
    }

    LaunchedEffect(data.loadState.refresh){
        if((data.loadState.refresh as? LoadStateError)?.error?.message != null){
            viewModel.onError(
                ServerErrorException(
                    (data.loadState.refresh as? LoadStateError)?.error?.message ?: "", ""
                )
            )
        }
    }

    val isLoading : State<Boolean> = rememberUpdatedState(
        data.loadState.refresh is LoadStateLoading
    )

    var error : (@Composable () -> Unit)? = null
    var noItem : (@Composable () -> Unit)? = null

    val refreshPage = {
        if(type != ReportPageType.ABOUT_ME){
            ReportFilters.clearTypeFilter(type)
            listingData.value.filters = ReportFilters.getByTypeFilter(type)
            listingData.value.filters.find { it.key == "user_id" }?.value = userId.toString()
        }

        viewModel.refresh()
        data.refresh()
        viewModel.updateItemTrigger.value++
    }

    data.loadState.apply {
        when {
            refresh is LoadStateLoading -> {
                error = null
                noItem = null
            }

            refresh is LoadStateNotLoading && data.itemCount < 1 -> {
                noItem = {
                    if (listingData.value.filters.find { it.key == "evaluation" }?.value == "") {
                        showNoItemLayout(
                            title = stringResource(strings.notFoundFeedbackLabel),
                            textButton = stringResource(strings.refreshButton)
                        ) {
                            refreshPage()
                        }
                    }else{
                        showNoItemLayout(
                            textButton = stringResource(strings.resetLabel)
                        ) {
                            viewModel.currentFilter.value = filters[0]
                            refreshPage()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(state.firstVisibleItemIndex){
        val isAtTop = currentIndex < 3
        onScrollDirectionChange(isAtTop)
        showUpButton = 2 < (state.firstVisibleItemIndex / PAGE_SIZE)
        showDownButton = listingData.value.prevIndex != null &&
                state.firstVisibleItemIndex < (listingData.value.prevIndex ?: 0)
    }

    LaunchedEffect(Unit){
        state.scrollToItem( viewModel.scrollItem.value, viewModel.offsetScrollItem.value)

        viewModel.currentFilter.value = if (listingData.value.filters.find { it.key == "evaluation" }?.value == "" || listingData.value.filters.find { it.key == "evaluation" }?.value == null) {
            filters[0]
        }else{
            filters[(listingData.value.filters.find { it.key == "evaluation" }?.value?.toInt() ?: 0) + 1]
        }
    }

    Column {
        AnimatedVisibility (model.type != ReportPageType.ABOUT_ME) {
            Row(
                modifier = Modifier.fillMaxWidth(if(isBigScreen) 0.5f else 1f).padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                getDropdownMenu(
                    selectedText = viewModel.currentFilter.value,
                    selectedTextDef = filters[0],
                    selects = filters,
                    onClearItem = {
                        viewModel.currentFilter.value = filters[0]
                        refreshPage()
                    },
                    onItemClick = { filter ->

                        viewModel.currentFilter.value = filter

                        when (filters.indexOf(filter)) {
                            0 -> {
                                listingData.value.filters.find { it.key == "evaluation" }?.value = ""
                                listingData.value.filters.find { it.key == "evaluation" }?.interpretation = null
                            }

                            1 -> {
                                listingData.value.filters.find { it.key == "evaluation" }?.value = "0"
                                listingData.value.filters.find { it.key == "evaluation" }?.interpretation = ""
                            }

                            2 -> {
                                listingData.value.filters.find { it.key == "evaluation" }?.value = "1"
                                listingData.value.filters.find { it.key == "evaluation" }?.interpretation = ""
                            }

                            3 -> {
                                listingData.value.filters.find { it.key == "evaluation" }?.value = "2"
                                listingData.value.filters.find { it.key == "evaluation" }?.interpretation = ""
                            }
                        }
                        refreshPage()
                    }
                )
            }
        }

        BaseContent(
            modifier = Modifier.fillMaxSize(),
            isLoading = if (model.type == ReportPageType.ABOUT_ME) false else isLoading.value,
            onRefresh = {
                viewModel.onError(ServerErrorException())
                refreshPage()
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumnWithScrollBars{
                    when {
                        error != null -> item { error?.invoke() }
                        noItem != null -> item { noItem?.invoke() }
                        model.type == ReportPageType.ABOUT_ME -> {
                            item {
                                if (aboutMe != null) {
                                    htmlText.setHtml(aboutMe)
                                } else {
                                    htmlText.setHtml(stringResource(strings.emptyAboutMeLabel))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = htmlText.annotatedString,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.darkBodyTextColor
                                    )
                                }
                            }
                        }
                        else -> {
                            items(data.itemCount) {
                                val item = data[it]
                                if (item != null) {
                                    FeedbackItem(
                                        item,
                                        UserData.login == model.userId,
                                        onClickReporter = { id ->
                                            component.goToUser(id)
                                        },
                                        onClickSnapshot = { id ->
                                            component.goToSnapshot(id)
                                        },
                                        onClickOrder = { id, type ->
                                            component.goToOrder(id,type)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (listingData.value.totalCount > 0) {
                    PagingCounterBar(
                        currentPage = currentIndex,
                        totalPages = listingData.value.totalCount,
                        modifier = Modifier.align(Alignment.BottomStart),
                        showUpButton = showUpButton,
                        showDownButton = showDownButton,
                        onClick = {
                            when {
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
    }
}
