package market.engine.fragments.root.main.user.feedbacks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ReportPageType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.items.FeedbackItem
import market.engine.widgets.rows.ColumnWithScrollBars
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbacksContent(
    aboutMe : String? = null,
    component : FeedbacksComponent,
    onScrollDirectionChange: (isAtTop: Boolean) -> Unit
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.feedbacksViewModel
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val state = rememberLazyScrollState(viewModel)
    val listingBaseViewModel = viewModel.listingBaseViewModel
    val listingDataState by listingBaseViewModel.listingData.collectAsState()
    val listingData = listingDataState.data

    val htmlText = rememberRichTextState()

    val isLoading : State<Boolean> = rememberUpdatedState(
        data.loadState.refresh is LoadStateLoading
    )

    val filters by viewModel.filters.collectAsState()

    val currentFilter = remember(listingDataState) {
        if (
            listingData.filters.find {
                it.key == "evaluation"
            }?.value == "" ||
            listingData.filters.find { it.key == "evaluation" }?.value == null
        ) {
            filters.firstOrNull()
        } else {
            filters[(listingData.filters.find { it.key == "evaluation" }?.value?.toInt()
                ?: 0) + 1]
        }
    }

    LaunchedEffect(aboutMe){
        if (aboutMe != null) {
            htmlText.setHtml(aboutMe)
        } else {
            htmlText.setHtml(getString(strings.emptyAboutMeLabel))
        }
    }

    LaunchedEffect(state.areBarsVisible.value){
        snapshotFlow {
            state.areBarsVisible.value
        }.collect {
            if (!it) {
                onScrollDirectionChange(false)
            }
        }
    }

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh) {

        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.find { it.key == "evaluation" }?.value == "") {
                    NoItemsFoundLayout(
                        title = stringResource(strings.notFoundFeedbackLabel),
                        textButton = stringResource(strings.refreshButton),
                        viewModel = viewModel,
                    ) {
                        viewModel.updatePage()
                    }
                } else {
                    NoItemsFoundLayout(
                        textButton = stringResource(strings.resetLabel),
                        viewModel = viewModel,
                    ) {
                        viewModel.refreshListing()
                    }
                }
            }
        } else {
            null
        }
    }

    EdgeToEdgeScaffold(
        topBar = {
            if(model.type != ReportPageType.ABOUT_ME) {
                AnimatedVisibility(
                    visible = state.areBarsVisible.value,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                )
                {
                    Row(
                        modifier = Modifier.background(colors.primaryColor.copy(alphaBars))
                            .fillMaxWidth()
                            .padding(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        getDropdownMenu(
                            selectedText = currentFilter ?: stringResource(strings.allFilterParams),
                            selectedTextDef = stringResource(strings.allFilterParams),
                            selects = filters,
                            onClearItem = {
                                viewModel.refreshListing()
                            },
                            onItemClick = { filter ->
                                viewModel.setNewFilter(filter)
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
        isLoading = if (model.type == ReportPageType.ABOUT_ME) false else isLoading.value,
        onRefresh = viewModel::updatePage,
        noFound = noFound
    ) { contentPadding ->
        when {
            model.type == ReportPageType.ABOUT_ME -> {
                ColumnWithScrollBars {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(contentPadding),
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
                PagingLayout(
                    data = data,
                    state= state.scrollState,
                    contentPadding = contentPadding,
                    viewModel = listingBaseViewModel,
                    content = { item ->
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
                                component.goToOrder(id, type)
                            }
                        )
                    }
                )
            }
        }
    }
}
