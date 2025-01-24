package market.engine.fragments.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.core.data.items.MesHeaderItem
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.getOfferImagePreview
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.exceptions.FullScreenImageViewer
import market.engine.widgets.exceptions.showNoItemLayout
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogsContent(
    component: DialogsComponent,
    modifier: Modifier,
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.dialogsViewModel
    val conversationInfo = viewModel.responseGetConversation.collectAsState()
    val offerInfo = viewModel.responseGetOfferInfo.collectAsState()
    val orderInfo = viewModel.responseGetOrderInfo.collectAsState()
    val data = model.pagingDataFlow.collectAsLazyPagingItems()
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData

    val userRole = remember { mutableStateOf("") }
    val isDisabledSendMes = remember { mutableStateOf(true) }
    val isDisabledAddPhotos = remember { mutableStateOf(true) }

    val isLoading: State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val successToast = stringResource(strings.operationSuccess)


    val refresh = {
        viewModel.resetScroll()
        viewModel.onRefresh()
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val isImageViewerVisible = remember { mutableStateOf(false) }

    val imageSize = remember { mutableStateOf(0) }

    val images = remember { mutableListOf<String>() }

    val pagerFullState = rememberPagerState(
        pageCount = { imageSize.value },
    )

    val scope = rememberCoroutineScope()

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpritation != null && it.interpritation != "" }) {
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

    val conversation = conversationInfo.value

    if (conversation != null) {
        BaseContent(
            topBar = {
                DialogsAppBar(
                    conversation.interlocutor,
                    modifier,
                    goToUser = {
                        component.goToUser(it)
                    },
                    onBack = {
                        component.onBackClicked()
                    }
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
                    backgroundColor = colors.white.copy(alpha = 0.5f),
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
                                        userRole.value = "seller"
                                        isDisabledAddPhotos.value = false
                                    } else {
                                        userRole.value = "buyer"
                                        isDisabledAddPhotos.value = true
                                    }
                                }

                                val title = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = colors.actionTextColor
                                        )
                                    ) {
                                        append(offer.title ?: "")
                                    }
                                }

                                val s = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = colors.titleTextColor
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
                                        userRole.value = "seller"
                                    } else {
                                        userRole.value = "buyer"
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

                                val imageUrl = order.suborders.firstOrNull()?.getOfferImagePreview()

                                MesHeaderItem(
                                    title = title,
                                    subtitle = subtitle,
                                    image = imageUrl,
                                ) {
                                    val type = if (userRole.value == "seller") {
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
                    },
                    filtersContent = null,
                    item = { messageItem ->
                        when (messageItem) {
                            is DialogsData.MessageItem -> {
                                DialogItem(
                                    messageItem,
                                    openImage = { index ->
                                        scope.launch {
                                            images.clear()
                                            images.addAll(messageItem.images?.map { it.url ?: "" } ?: emptyList())
                                            imageSize.value = images.size
                                            pagerFullState.scrollToPage(index)
                                            isImageViewerVisible.value = true
                                        }
                                    },
                                    onLongClick = {

                                    }
                                )
                            }

                            is DialogsData.SeparatorItem -> {
                                SeparatorDialogItem(
                                    messageItem as? DialogsData.SeparatorItem
                                        ?: return@ListingBaseContent,
                                )
                            }
                        }
                    }
                )

                //messenger bar

            }
        }
    }
}
