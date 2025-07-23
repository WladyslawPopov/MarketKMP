package market.engine.fragments.root.main.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.zIndex
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.widgets.ilustrations.FullScreenImageViewer
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.DialogsHeader
import market.engine.widgets.bars.MessengerBar
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.filterContents.CustomBottomSheet
import market.engine.widgets.items.DialogItem
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

    val listingBaseViewModel = viewModel.listingBaseViewModel

    val messageBarData = viewModel.messageBarState.collectAsState()

    val images = viewModel.images.collectAsState()
    val selectIndex = viewModel.selectedImageIndex.collectAsState()

    val listingData = viewModel.listingData.value.data

    val headerItem = uiState.value.mesHeader
    val conversation = uiState.value.conversations
    val appBarData = uiState.value.appBarState

    val isLoading = viewModel.isShowProgress.collectAsState()

    val focusManager = LocalFocusManager.current

    val toastItem = viewModel.toastItem.collectAsState()

    val pagerFullState = rememberPagerState(
        pageCount = { images.value.size },
    )

    var mesBarHeight by remember { mutableStateOf(dimens.zero) }
    val density = LocalDensity.current

    LaunchedEffect(images.value, selectIndex.value) {
        snapshotFlow {
            images.value to selectIndex.value
        }.collectLatest { (images, selectedIndex) ->
            if (selectedIndex != null && images.isNotEmpty() && images.size > selectedIndex) {
                pagerFullState.scrollToPage(selectedIndex)
            }
        }
    }

    BackHandler(model.backHandler){
        component.onBackClicked()
    }

    val listingState = rememberLazyScrollState(viewModel)

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    NoItemsFoundLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        viewModel.updatePage()
                    }
                } else {
                    NoItemsFoundLayout(
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

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = appBarData,
                color = colors.white.copy(alphaBars)
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

            AnimatedVisibility(
                headerItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (headerItem != null) {
                    DialogsHeader(
                        headerItem
                    )
                }
            }
        },
        onRefresh = {
            viewModel.updatePage()
        },
        error = null,
        noFound = noFound,
        isLoading = isLoading.value,
        toastItem = toastItem.value,
        showContentWhenLoading = true,
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        CustomBottomSheet(
            initValue = selectIndex.value != null,
            contentPadding = contentPadding,
            onClosed = {
                viewModel.closeImages()
            },
            sheetContent = {
                FullScreenImageViewer(
                    pagerFullState = pagerFullState,
                    images = images.value,
                    isUpdate = images.value.isNotEmpty(),
                    modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())
                )
            }
        ){
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
                    .fillMaxSize(),
            )
            {
                PagingLayout(
                    data = data,
                    viewModel = listingBaseViewModel,
                    state = listingState.scrollState,
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = mesBarHeight
                    ),
                    content = { messageItem ->
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
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(
                            colors.primaryColor.copy(alphaBars),
                            MaterialTheme.shapes.medium
                        )
                        .clip(MaterialTheme.shapes.medium)
                        .onSizeChanged {
                            val newHeight = with(density) { it.height.toDp() }
                            mesBarHeight = newHeight
                        }
                        .padding(bottom = contentPadding.calculateBottomPadding())
                        .zIndex(15f)
                ) {
                    MessengerBar(
                        data = messageBarData.value,
                        events = viewModel.messageBarEvents,
                        isLoading = isLoading.value
                    )
                }
            }
        }
    }
}
