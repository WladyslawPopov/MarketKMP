package market.engine.fragments.root.main.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.fragments.base.BackHandler
import market.engine.widgets.ilustrations.FullScreenImageViewer
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.items.SeparatorDialogItem
import market.engine.widgets.rows.UserRow
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogsContent(
    component: DialogsComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.dialogsViewModel
    val uiState = viewModel.dialogContentState.collectAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    val messageBarData = viewModel.messageBarState.collectAsState()

    val images = viewModel.images.collectAsState()
    val imageSize = viewModel.imageSize.collectAsState()
    val selectIndex = viewModel.selectedImageIndex.collectAsState()

    val listingData = viewModel.listingData.value.data

    val listingBaseState = uiState.value.listingBaseState
    val headerItem = uiState.value.mesHeader
    val conversation = uiState.value.conversations
    val appBarData = uiState.value.appBarState

    val isLoading = viewModel.isShowProgress.collectAsState()

    val focusManager = LocalFocusManager.current

    val scaffoldState = rememberBottomSheetScaffoldState()

    val pagerFullState = rememberPagerState(
        pageCount = { imageSize.value },
    )

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.scrollState.value.scrollItem,
        initialFirstVisibleItemScrollOffset = viewModel.scrollState.value.offsetScrollItem
    )

    LaunchedEffect(data.loadState.refresh){
        viewModel.setLoading(data.loadState.refresh is LoadStateLoading)

        if (data.loadState.refresh !is LoadStateLoading){
            scrollState.scrollToItem(0)
        }
    }

    LaunchedEffect(selectIndex.value ) {
        if (selectIndex.value != null) {
            pagerFullState.scrollToPage(selectIndex.value!!)
        }
    }

    BackHandler(model.backHandler){
        component.onBackClicked()
    }


    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        viewModel.updatePage()
                    }
                } else {
                    showNoItemLayout(
                        title = stringResource(strings.simpleNotFoundLabel),
                        icon = drawables.dialogIcon
                    ) {
                        viewModel.updatePage()
                    }
                }
            }
        } else {
            null
        }
    }

    LaunchedEffect(images.value) {
        if (images.value.isNotEmpty()) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed) {
        if (scaffoldState.bottomSheetState.isCollapsed) {
            viewModel.closeImages()
        }
    }

    BaseContent(
        topBar = {
            SimpleAppBar(
                data = appBarData
            ){
                if (conversation?.interlocutor != null) {
                    UserRow(
                        user = conversation.interlocutor!!,
                        modifier = Modifier.clickable {
                            component.goToUser(conversation.interlocutor?.id!!)
                        }
                    )
                }else{
                    TextAppBar(
                        stringResource(strings.messageTitle)
                    )
                }
            }
        },
        onRefresh = {
            viewModel.updatePage()
        },
        isHideContent = false,
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            sheetContentColor = colors.transparent,
            sheetBackgroundColor = colors.transparent,
            contentColor = colors.transparent,
            backgroundColor = colors.transparent,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = true,
            sheetContent = {
                FullScreenImageViewer(
                    pagerFullState = pagerFullState,
                    images = images.value,
                    isUpdate = images.value.isNotEmpty(),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
                    .fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ListingBaseContent(
                        uiState = listingBaseState,
                        data = data,
                        baseViewModel = viewModel,
                        noFound = noFound,
                        additionalBar = {
                            Column {
                                AnimatedVisibility(
                                    headerItem != null,
                                    enter = expandIn(),
                                    exit = fadeOut()
                                ) {
                                    if (headerItem != null) {
                                        DialogsHeader(
                                            headerItem
                                        )
                                    }
                                }
                            }
                        },
                        filtersContent = null,
                        item = { messageItem ->
                            when (messageItem) {
                                is DialogsData.MessageItem -> {
                                    DialogItem(
                                        item = messageItem,
                                    )
                                }

                                is DialogsData.SeparatorItem -> {
                                    SeparatorDialogItem(
                                        messageItem,
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                MessengerBar(
                    data = messageBarData.value,
                    events = viewModel.messageBarEvents
                )
            }
        }
    }
}
