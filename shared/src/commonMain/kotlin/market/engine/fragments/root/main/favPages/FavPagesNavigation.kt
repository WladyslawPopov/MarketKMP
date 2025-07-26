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
import androidx.compose.runtime.LaunchedEffect
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
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.screens.OnError
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
    val favItemId: Long
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
    val appBarState = uiState.value.appState
    val initPos = viewModel.initPosition.collectAsState()
    val updateFullPage = viewModel.updatePage.collectAsState()

    val tooltipState = rememberTooltipState()
    val toastItem = viewModel.toastItem.collectAsState()

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

    LaunchedEffect(initPos.value) {
        if (initPos.value != component.componentsPages.value.selectedIndex) {
            component.selectPage(initPos.value)
        }
    }

    LaunchedEffect(updateFullPage.value) {
        if(updateFullPage.value > 0) {
            component.fullRefresh()
        }
    }

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex =
            (initPos.value).coerceIn(0, (favTabList.value.size-1).coerceAtLeast(0))
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

    EdgeToEdgeScaffold(
        topBar = {
            TooltipWrapper(
                modifier= Modifier,
                tooltipState = tooltipState,
                onClick = onTooltipClick,
                content = { tooltipState ->
                    SimpleAppBar(
                        data = appBarState
                    ) {
                        ReorderTabRow(
                            tabs = favTabList.value,
                            selectedTab = initPos.value,
                            isDragMode = isDragMode,
                            menuList = viewModel.menuItems.value,
                            onTabsReordered = { newList ->
                                favTabList.value = newList
                            },
                            lazyListState = lazyListState,
                            modifier = Modifier.fillMaxWidth().padding(end = dimens.smallPadding),
                        )
                    }
                }
            )
        },
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        toastItem = toastItem.value,
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ChildPages(
                modifier = Modifier.fillMaxSize(),
                pages = component.componentsPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    viewModel.selectPage(it)
                }
            ) { _, page ->
                when (page) {
                    is FavPagesComponents.SubscribedChild -> {
                        SubscriptionsContent(
                            page.component,
                            Modifier.padding(contentPadding)
                        )
                    }

                    is FavPagesComponents.FavoritesChild -> {
                        FavoritesContent(
                            page.component,
                            Modifier.padding(top = contentPadding.calculateTopPadding())
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
                    SetUpDynamicFields(state.fields){
                        viewModel.setNewField(it)
                    }
                }
            }
        }
    }
}
