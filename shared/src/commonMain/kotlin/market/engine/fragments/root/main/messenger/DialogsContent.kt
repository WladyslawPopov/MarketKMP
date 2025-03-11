package market.engine.fragments.root.main.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.common.clipBoardEvent
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.core.data.items.MesHeaderItem
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.getOfferImagePreview
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.fragments.base.BackHandler
import market.engine.widgets.ilustrations.FullScreenImageViewer
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.items.SeparatorDialogItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogsContent(
    component: DialogsComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.dialogsViewModel
    val offerInfo = viewModel.responseGetOfferInfo.collectAsState()
    val orderInfo = viewModel.responseGetOrderInfo.collectAsState()
    val data = model.pagingDataFlow.collectAsLazyPagingItems()
    val imagesUpload = viewModel.responseImages.collectAsState()
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData

    val userRole = remember { mutableStateOf("") }
    val isDisabledSendMes = remember { mutableStateOf(true) }
    val isDisabledAddPhotos = remember { mutableStateOf(true) }

    val isLoading = viewModel.isShowProgress.collectAsState()

    val messageTextState = remember { viewModel.messageTextState }

    val focusManager = LocalFocusManager.current

    val scaffoldState = rememberBottomSheetScaffoldState()
    val isImageViewerVisible = remember { mutableStateOf(false) }

    val imageSize = remember { mutableStateOf(0) }

    val images = remember { mutableListOf<String>() }

    val pagerFullState = rememberPagerState(
        pageCount = { imageSize.value },
    )

    val scope = rememberCoroutineScope()

    val copyId = stringResource(strings.idCopied)
    val textCopied = stringResource(strings.textCopied)

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.scrollItem.value,
        initialFirstVisibleItemScrollOffset = viewModel.offsetScrollItem.value
    )

    LaunchedEffect(data.loadState.refresh){
        viewModel.setLoading(data.loadState.refresh is LoadStateLoading)

        if (data.loadState.refresh !is LoadStateLoading){
            scrollState.scrollToItem(0)
        }
    }

    BackHandler(model.backHandler){
        component.onBackClicked()
    }

    val refresh = {
        viewModel.onRefresh()
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpretation != null && it.interpretation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                viewModel.onRefresh()
            }
        } else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.dialogIcon
            ) {
                viewModel.resetScroll()
                viewModel.onRefresh()
            }
        }
    }

    LaunchedEffect(isImageViewerVisible.value) {
        if (isImageViewerVisible.value) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed) {
        if (scaffoldState.bottomSheetState.isCollapsed) {
            isImageViewerVisible.value = false
        }
    }

    val conversation = viewModel.responseGetConversation.value
    if (conversation != null) {
        BaseContent(
            topBar = {
                DialogsAppBar(
                    conversation,
                    modifier,
                    goToUser = {
                        component.goToUser(it)
                    },
                    onBack = {
                        if (!isImageViewerVisible.value) {
                            component.onBackClicked()
                        }else{
                            isImageViewerVisible.value = false
                        }
                    },
                    onRefresh = {
                        refresh()
                    },
                    onMenuClick = { key->
                        when(key){
                            "delete_dialog" -> {
                                viewModel.deleteConversation(conversation.id){
                                    component.onBackClicked()
                                }
                            }
                            "copyId" -> {
                                clipBoardEvent(conversation.aboutObjectId.toString())
                                viewModel.showToast(
                                    successToastItem.copy(
                                        message = copyId
                                    )
                                )
                            }
                        }
                    }
                )
            },
            onRefresh = {
                refresh()
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
                        images = images,
                        isUpdate = isImageViewerVisible.value,
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
                            listingData = listingData.value,
                            searchData = searchData.value,
                            data = data,
                            baseViewModel = viewModel,
                            onRefresh = {
                                refresh()
                            },
                            noFound = noFound,
                            isReversingPaging = true,
                            additionalBar = {
                                val offer = offerInfo.value
                                val order = orderInfo.value
                                val sign = stringResource(strings.currencySign)
                                val orderLabel = stringResource(strings.orderLabel)
                                val headerItem = when {
                                    offer != null -> {
                                        if (offer.sellerData?.markedAsDeleted == true) {
                                            isDisabledSendMes.value = true
                                            isDisabledAddPhotos.value = true
                                        } else {
                                            isDisabledSendMes.value = false

                                            if (offer.sellerData?.id == conversation.interlocutor?.id) {
                                                userRole.value = "buyer"
                                                isDisabledAddPhotos.value = true
                                            } else {
                                                userRole.value = "seller"
                                                isDisabledAddPhotos.value = false
                                            }
                                        }

                                        val title = buildAnnotatedString {
                                            append(offer.title ?: "")
                                        }

                                        val s = buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    color = colors.priceTextColor
                                                )
                                            ) {
                                                append(offer.currentPricePerItem.toString())
                                                append(sign)
                                            }
                                        }

                                        val imageUrl = offer.getOfferImagePreview()


                                        MesHeaderItem(
                                            title = title,
                                            subtitle = s,
                                            image = imageUrl,
                                        ) {
                                            component.goToOffer(offer.id)
                                        }

                                    }

                                    order != null -> {
                                        if (order.sellerData?.markedAsDeleted == true) {
                                            isDisabledSendMes.value = true
                                            isDisabledAddPhotos.value = true
                                        } else {
                                            isDisabledSendMes.value = false
                                            isDisabledAddPhotos.value = false

                                            if (order.sellerData?.id == conversation.interlocutor?.id) {
                                                userRole.value = "buyer"
                                            } else {
                                                userRole.value = "seller"
                                            }
                                        }
                                        val title = buildAnnotatedString {
                                            withStyle(SpanStyle(color = colors.titleTextColor)) {
                                                append(orderLabel)
                                            }
                                            append(" #${order.id}")
                                        }

                                        val subtitle = buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    color = colors.actionTextColor,
                                                )
                                            ) {
                                                append(order.suborders.firstOrNull()?.title)
                                            }
                                        }

                                        val imageUrl =
                                            order.suborders.firstOrNull()?.getOfferImagePreview()

                                        MesHeaderItem(
                                            title = title,
                                            subtitle = subtitle,
                                            image = imageUrl,
                                        ) {
                                            val type = if (userRole.value != "seller") {
                                                DealTypeGroup.BUY
                                            } else {
                                                DealTypeGroup.SELL
                                            }
                                            component.goToOrder(order.id, type)
                                        }
                                    }

                                    else -> {
                                        null
                                    }
                                }
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
                            scrollState = scrollState,
                            item = { messageItem ->
                                val isDeleteItem = remember { mutableStateOf(false) }
                                when (messageItem) {
                                    is DialogsData.MessageItem -> {
                                        if(!isDeleteItem.value){
                                            DialogItem(
                                                messageItem,
                                                openImage = { index ->
                                                    scope.launch {
                                                        images.clear()
                                                        images.addAll(messageItem.images?.map {
                                                            it.url ?: ""
                                                        } ?: emptyList())
                                                        imageSize.value = images.size
                                                        pagerFullState.scrollToPage(index)
                                                        isImageViewerVisible.value = true
                                                    }
                                                },
                                                onMenuClick = { key ->
                                                    when (key) {
                                                        "delete" -> {
                                                            viewModel.deleteMessage(messageItem.id){
                                                                isDeleteItem.value = true
                                                            }
                                                        }

                                                        "copy" -> {
                                                            clipBoardEvent(messageItem.message)
                                                            viewModel.showToast(
                                                                successToastItem.copy(
                                                                    message = textCopied
                                                                )
                                                            )
                                                        }
                                                    }
                                                },
                                                goToUser = {
                                                    component.goToUser(it)
                                                },
                                                goToOffer = {
                                                    component.goToOffer(it)
                                                },
                                                goToListing = {
                                                    component.goToNewSearch(it)
                                                }
                                            )
                                        }
                                    }

                                    is DialogsData.SeparatorItem -> {
                                        SeparatorDialogItem(
                                            messageItem as? DialogsData.SeparatorItem
                                                ?: return@ListingBaseContent,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    MessengerBar(
                        messageTextState = messageTextState,
                        imagesUpload = imagesUpload.value,
                        isDisabledSendMes = isDisabledSendMes.value,
                        isDisabledAddPhotos = isDisabledAddPhotos.value,
                        getImages = { files->
                            viewModel.getImages(files)
                        },
                        deleteImage = {
                            viewModel.deleteImage(it)
                        },
                        sendMessage = {
                            viewModel.sendMessage(model.dialogId, messageTextState.value)
                        }
                    )
                }
            }
        }
    }
}
