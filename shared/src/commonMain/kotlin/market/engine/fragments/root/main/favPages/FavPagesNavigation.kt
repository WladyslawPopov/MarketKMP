package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.Tab
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.OnError
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.root.main.favPages.favorites.FavoritesContent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.dialogs.CustomDialog
import market.engine.widgets.tabs.ReorderTabRow
import market.engine.widgets.tooltip.TooltipWrapper
import market.engine.widgets.tooltip.rememberTooltipState


@Serializable
data class FavPagesConfig(
    @Serializable
    val favItem: FavoriteListItem
)

@Composable
fun FavPagesNavigation(
    component: FavPagesComponent,
    modifier: Modifier
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.viewModel
    val err = viewModel.errorMessage.collectAsState()

    val uiState = viewModel.favPagesState.collectAsState()
    val customDialogState = viewModel.customDialogState.collectAsState()

    val favTabList = remember { mutableStateOf(uiState.value.favTabList) }
    val isDragMode = uiState.value.isDragMode
    val initPosition = uiState.value.initPosition
    val appBarState = uiState.value.appState

    val tooltipState = rememberTooltipState()

    val onTooltipClick = remember {
        mutableStateOf(
            {
                viewModel.settings.setSettingValue(
                    "create_blank_offer_list_notify_badge",
                    false
                )
                tooltipState.hide()
            }
        )
    }

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex =
            (initPosition).coerceIn(0, (favTabList.value.size-1).coerceAtLeast(0))
    )

    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            {
                OnError(err.value) {
                    component.onRefresh()
                }
            }
        } else {
            null
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        TooltipWrapper(
            modifier= Modifier,
            tooltipState = tooltipState,
            onClick = onTooltipClick,
            content = { tooltipState ->
                Column {
                    SimpleAppBar(
                        data = appBarState
                    ){
                        ReorderTabRow(
                            tabs = favTabList.value.map {
                                Tab(
                                    id = it.id,
                                    title = it.title ?: "",
                                    image = it.images.firstOrNull(),
                                    isPined = it.markedAsPrimary,
                                    onClick = {

                                    }
                                )
                            }.toList(),
                            selectedTab = initPosition,
                            onTabSelected = {
                                component.selectPage(it)
                            },
                            isDragMode = isDragMode,
                            onTabsReordered = { list ->
                                val newList = list.map { listItem ->
                                    favTabList.value.find { it.title == listItem.title && it.id == listItem.id } ?: FavoriteListItem()
                                }
                                favTabList.value = newList
                            },
                            lazyListState = lazyListState,
                            getOperations = { id, callback ->
                                if (id > 1000) {
                                    viewModel.getOperationFavTab(id, callback)
                                }else{
                                    viewModel.getDefOperationFavTab(callback)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(end = dimens.smallPadding),
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        ChildPages(
                            modifier = Modifier.fillMaxSize(),
                            pages = component.componentsPages,
                            scrollAnimation = PagesScrollAnimation.Default,
                            onPageSelected = {
                                component.selectPage(it)
                            }
                        ) { _, page ->
                            when (page) {
                                is FavPagesComponents.SubscribedChild -> {
                                    SubscriptionsContent(
                                        page.component,
                                        Modifier
                                    )
                                }

                                is FavPagesComponents.FavoritesChild -> {
                                    FavoritesContent(
                                        page.component,
                                        Modifier
                                    )
                                }
                            }
                        }

                        if (isDragMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.white.copy(alpha = 0.3f))
                                    .blur(dimens.extraLargePadding)
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            viewModel.updateFavTabList(favTabList.value)
                                            viewModel.closeDragMode()
                                            component.fullRefresh()
                                        }
                                    }
                            )
                        }
                    }

                    CustomDialog(
                        uiState = customDialogState.value,
                    ){ state ->
                        Column {
                            if (state.fields.isNotEmpty()) {
                                SetUpDynamicFields(state.fields)
                            }
                        }
                    }
                }
            }
        )
    }
}
