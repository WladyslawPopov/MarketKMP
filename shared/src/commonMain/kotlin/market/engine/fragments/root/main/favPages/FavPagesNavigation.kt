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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.MenuItem
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
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val err by viewModel.errorMessage.collectAsState()

    val uiState by viewModel.favPagesState.collectAsState()
    val customDialogState by viewModel.customDialogState.collectAsState()
    val isLoading by viewModel.isShowProgress.collectAsState()

    val pages by component.componentsPages.subscribeAsState()

    val favTabListState by viewModel.favoritesTabList.collectAsState()

    val favTabList = remember(favTabListState) { mutableStateOf(favTabListState) }

    val isDragMode = uiState.isDragMode
    val appBarState = uiState.appState

    val tooltipState = rememberTooltipState()
    val toastItem by viewModel.toastItem.collectAsState()

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

    val lazyListState = rememberLazyListState()

    LaunchedEffect(pages){
        lazyListState.animateScrollToItem(
            pages.selectedIndex
        )
    }

    val error : (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            {
                OnError(err) {
                    component.onRefresh()
                }
            }
        } else {
            null
        }
    }

    val activeFavTabId = remember { mutableStateOf(1L) }

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
                        val menuList = remember { mutableStateOf<List<MenuItem>>(emptyList()) }

                        ReorderTabRow(
                            tabs = favTabList.value,
                            selectedTab = pages.selectedIndex,
                            isDragMode = isDragMode,
                            menuList = menuList.value,
                            onTabsReordered = { newList ->
                                favTabList.value = newList
                            },
                            onLongClick = { id ->
                                viewModel.viewModelScope.launch {
                                    activeFavTabId.value = id
                                    if (id > 1000) {
                                        menuList.value = viewModel.getOperationFavTab(id)
                                    }else{
                                        menuList.value = viewModel.getDefOperationFavTab()
                                    }
                                }
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
        toastItem = toastItem,
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ChildPages(
                modifier = Modifier.fillMaxSize(),
                pages = pages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    viewModel.selectPage(it)
                }
            )
            { _, page ->
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
                                viewModel.closeDragMode()
                                viewModel.updateFavTabList(favTabList.value)
                            }
                        }
                )
            }
        }

        CustomDialog(
            uiState = customDialogState,
            onDismiss = {
                viewModel.closeDialog()
            },
            isLoading = isLoading,
            onSuccessful = {
                viewModel.onClickOperation(customDialogState.typeDialog, activeFavTabId.value)
                activeFavTabId.value = 1
            },
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
