package market.engine.fragments.root.main.offer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.common.openUrl
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.network.networkObjects.DealType
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Param
import market.engine.core.network.networkObjects.PaymentMethod
import market.engine.core.network.networkObjects.Value
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.OfferStates
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Bids
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.RemoveBid
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.onClickItem
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseContent
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.dialogs.AddBidDialog
import market.engine.widgets.dialogs.OfferMessagingDialog
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.widgets.dialogs.ListPicker
import market.engine.widgets.dialogs.rememberPickerState
import market.engine.widgets.ilustrations.FullScreenImageViewer
import market.engine.widgets.ilustrations.HorizontalImageViewer
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.widgets.items.offer_Items.PromoOfferRowItem
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.bars.UserPanel
import market.engine.widgets.buttons.PromoBuyBtn
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dialogs.CustomDialog
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.items.BidsListItem
import market.engine.widgets.items.RemovedBidsListItem
import market.engine.widgets.rows.ColumnWithScrollBars
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.LazyRowWithScrollBars
import market.engine.widgets.textFields.OutlinedTextInputField
import market.engine.widgets.texts.DiscountText
import market.engine.widgets.texts.SeparatorLabel
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfferContent(
    component: OfferComponent,
    modifier: Modifier
) {
    val model by component.model.subscribeAsState()
    val offerViewModel = model.offerViewModel

    val visitedHistory = offerViewModel.responseHistory.collectAsState()
    val ourChoiceList = offerViewModel.responseOurChoice.collectAsState()

    val catHistory = offerViewModel.responseCatHistory.collectAsState()
    val blackList = offerViewModel.statusList.collectAsState()

    val remainingTime = offerViewModel.remainingTime.collectAsState()

    val lotState = offerViewModel.responseOffer.collectAsState()

    val isLoading = offerViewModel.isShowProgress.collectAsState()
    val isError = offerViewModel.errorMessage.collectAsState()

    val isMyOffer = offerViewModel.isMyOffer
    val offerState = offerViewModel.offerState

    val isImageViewerVisible = remember { mutableStateOf(false) }
    val isShowOptions = remember { mutableStateOf(false) }
    val isShowMesDialog = remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState()

    val goToBids = 6

    val focusManager = LocalFocusManager.current

    val stateColumn = rememberLazyListState(
        initialFirstVisibleItemIndex = offerViewModel.scrollItem.value,
    )

    val scope = rememberCoroutineScope()

    BackHandler(model.backHandler){
        component.onBackClick()
    }

    LaunchedEffect(stateColumn){
        snapshotFlow {
            stateColumn.firstVisibleItemIndex
        }.collect { item->
            offerViewModel.scrollItem.value = item
        }
    }

    val imageSize = remember { mutableStateOf(1) }

    val images = remember { mutableListOf("") }

    val pagerState = rememberPagerState(
        pageCount = { imageSize.value },
    )

    val pagerFullState = rememberPagerState(
        pageCount = { imageSize.value },
    )

    val showBidDialog = remember { mutableStateOf(false) }
    val myMaximalBid = remember { mutableStateOf("") }

    val showDialog = remember { mutableStateOf(false) }
    val showOperationsDialog = remember { mutableStateOf("") }
    val title = remember { mutableStateOf(AnnotatedString("")) }
    val fields = remember { mutableStateOf< ArrayList<Fields>>(arrayListOf()) }

    val isClicked = remember { mutableStateOf(false) }

    LaunchedEffect(isImageViewerVisible.value){
        if (!isImageViewerVisible.value){
            scaffoldState.bottomSheetState.collapse()
            pagerState.scrollToPage(pagerFullState.currentPage)
        }else{
            if (pagerState.currentPage != pagerFullState.currentPage) {
                pagerFullState.scrollToPage(pagerState.currentPage)
            }
            if (images.isNotEmpty()){
                scaffoldState.bottomSheetState.expand()
            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed) {
        if (scaffoldState.bottomSheetState.isCollapsed) {
            isImageViewerVisible.value = false
            pagerState.scrollToPage(pagerFullState.currentPage)
        }
    }

    LaunchedEffect(lotState.value){
        lotState.value?.let { offer ->
            images.clear()
            images.addAll(
                when {
                    offer.images?.isNotEmpty() == true -> offer.images?.map { it.urls?.big?.content.orEmpty() } ?: emptyList()
                    offer.externalImages?.isNotEmpty() == true -> offer.externalImages
                    else -> listOf("empty")
                }
            )
            imageSize.value = images.size

            myMaximalBid.value = offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: ""
        }
    }

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(isError) {
            showOperationsDialog.value = ""
            component.updateOffer(lotState.value?.id ?: 1L, model.isSnapshot)
        } }
    }else{
        null
    }

    lotState.value?.let { offer ->
        val copiedString = stringResource(strings.idCopied)
        val currency = stringResource(strings.currencyCode)
        val defList = buildList {
            add(MenuItem(
                id = "copyId",
                title = stringResource(strings.copyOfferId),
                icon = drawables.copyIcon,
                onClick = {
                    clipBoardEvent(offer.id.toString())
                    offerViewModel.showToast(
                        successToastItem.copy(
                            message = copiedString
                        )
                    )
                }
            ))

            add(MenuItem(
                id = "share",
                title = stringResource(strings.shareOffer),
                icon = drawables.shareIcon,
                onClick = {
                    offer.publicUrl?.let { openShare(it) }
                }
            ))

            add(MenuItem(
                id = "calendar",
                title = stringResource(strings.addToCalendar),
                icon = drawables.calendarIcon,
                onClick = {
                    offer.publicUrl?.let { openCalendarEvent(it) }
                }
            ))

            if (UserData.token != "") {
                add(
                    MenuItem(
                        id = "create_blank_offer_list",
                        title = stringResource(strings.createNewOffersListLabel),
                        icon = drawables.addFolderIcon,
                        onClick = {
                            offerViewModel.getFieldsCreateBlankOfferList { t, f ->
                                title.value = AnnotatedString(t)
                                fields.value.clear()
                                fields.value.addAll(f)
                                showOperationsDialog.value = "create_blank_offer_list"
                            }
                        }
                    )

                )
            }
        }
        val operationsList = offerViewModel.menuList.collectAsState().value.map { operation ->
            MenuItem(
                id = operation.id ?: "",
                title = operation.name ?: "",
                onClick = {
                    operation.onClickItem(
                        offer.parseToOfferItem(),
                        offerViewModel,
                        title,
                        fields,
                        showOperationsDialog,
                        onUpdateOfferItem = {
                            when(operation.id){
                                "watch", "unwatch","create_blank_offer_list"  -> {
                                    lotState.value?.isWatchedByMe = !offer.isWatchedByMe
                                    offerViewModel.getOperations(offer.id)
                                }
                                else -> {
                                    component.updateOffer(it, model.isSnapshot)
                                }
                            }
                        },
                        {
                            component.goToProposalPage(ProposalType.ACT_ON_PROPOSAL)
                        },
                        {
                            component.goToCreateOffer(CreateOfferType.COPY, offer.catpath, offer.id, offer.externalImages)
                        },
                        { t, id ->
                            component.goToDynamicSettings(t, id)
                        },
                    )
                }
            )
        }
        val menuPromoList = offerViewModel.menuPromoList.collectAsState().value.map { operation ->
            MenuItem(
                id = operation.id ?: "",
                title = "${(operation.name ?: "")} (${operation.price*-1} $currency)",
                onClick = {
                    offerViewModel.getPromoOperationFields(offer.id, operation.id ?: "") { t, f ->
                        title.value = AnnotatedString(t)
                        fields.value.clear()
                        fields.value.addAll(f)
                        showOperationsDialog.value = operation.id ?: ""
                    }
                }
            )
        }

        BaseContent(
            topBar = {
                OfferAppBar(
                    offerState.value == OfferStates.SNAPSHOT,
                    offer,
                    defMenu = defList,
                    optionMenu = operationsList,
                    onBeakClick = {
                        if (!isImageViewerVisible.value) {
                            component.onBackClick()
                        } else {
                            isImageViewerVisible.value = false
                        }
                    },
                    onRefresh = {
                        component.updateOffer(offer.id, model.isSnapshot)
                    }
                )
            },
            isLoading = isLoading.value,
            error = error,
            noFound = null,
            toastItem = offerViewModel.toastItem,
            onRefresh = {
                component.updateOffer(offer.id, model.isSnapshot)
            },
            modifier = Modifier.fillMaxSize()
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
                        images = images
                    )
                },
            ) {
                LazyColumnWithScrollBars(
                    state = stateColumn,
                    modifierList = modifier.background(color = colors.primaryColor)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                isShowOptions.value = false
                                focusManager.clearFocus()
                            })
                        },
                    contentPadding = dimens.smallPadding
                ) {
                    //images offer
                    item {
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable { isImageViewerVisible.value = !isImageViewerVisible.value  }
                                .fillMaxWidth()
                                .height(if(isBigScreen.value) 500.dp else 300.dp)
                                .zIndex(6f),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalImageViewer(
                                images = images,
                                pagerState = pagerState,
                            )

                            if (offer.videoUrls?.isNotEmpty() == true) {
                                SmallImageButton(
                                    drawables.iconYouTubeSmall,
                                    modifierIconSize = Modifier.size(dimens.largeIconSize),
                                    modifier = Modifier
                                        .size(90.dp)
                                        .align(Alignment.TopEnd)
                                        .zIndex(1f), // Higher priority
                                ) {
                                    // Open web view YouTube
                                    openUrl(offer.videoUrls[0])
                                }
                            }
                        }
                    }
                    if (offer.hasTempImages) {
                        item {
                            Text(
                                stringResource(strings.tempPhotoLabel),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.grayText
                            )
                        }
                    }
                    //category stack
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.Start,
                            verticalArrangement = Arrangement.SpaceAround,
                        ) {
                            if (catHistory.value.isNotEmpty()) {
                                catHistory.value.forEachIndexed { index, cat ->
                                    Text(
                                        text = if (catHistory.value.size - 1 == index)
                                            cat.name ?: ""
                                        else (cat.name ?: "") + "->",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (catHistory.value.size - 1 == index) colors.black else colors.grayText,
                                        modifier = Modifier.padding(dimens.smallPadding)
                                            .clickable {
                                                //go to Listing
                                                component.goToCategory(cat)
                                            }
                                    )
                                }
                            }
                        }
                    }
                    //count and views label
                    item {
                        if (offerState.value == OfferStates.ACTIVE || isMyOffer.value) {
                            val countString =
                                getCountString(offerState.value, offer)

                            FlowRow(
                                horizontalArrangement = Arrangement.Start,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (countString.isNotEmpty()) {
                                    Text(
                                        text = countString,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.grayText,
                                        modifier = Modifier.padding(dimens.smallPadding)
                                    )
                                }

                                Text(
                                    text = stringResource(strings.viewsParams) + ": " + offer.viewsCount,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.grayText,
                                    modifier = Modifier.padding(dimens.smallPadding)
                                )
                            }
                        }
                    }
                    //title
                    item {
                        TitleText(
                            offer.title ?: "",
                            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                        )
                    }

                    item {
                        val columns = remember { if (isBigScreen.value) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1) }
                        LazyVerticalStaggeredGrid(
                            columns = columns,
                            modifier = Modifier
                                .heightIn(200.dp, 5000.dp)
                                .wrapContentHeight(),
                            userScrollEnabled = false,
                            verticalItemSpacing = dimens.smallPadding,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally),
                            content = {
                                if (offer.note != null) {
                                    item {
                                        Row(
                                            modifier = Modifier.background(
                                                colors.white,
                                                MaterialTheme.shapes.small
                                            ).clip(MaterialTheme.shapes.small).clickable {
                                                offerViewModel.getOperationFields(
                                                    offer.id,
                                                    "edit_note",
                                                    "offers",
                                                    onSuccess = { t, f ->
                                                        title.value = AnnotatedString(t)
                                                        fields.value.clear()
                                                        fields.value.addAll(f)
                                                        showOperationsDialog.value = "edit_note"
                                                    }
                                                )
                                            }.padding(dimens.smallPadding),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                        ) {
                                            Icon(
                                                painterResource(drawables.editNoteIcon),
                                                contentDescription = "",
                                                modifier = Modifier.size(dimens.smallIconSize),
                                                tint = colors.black
                                            )

                                            Text(
                                                text = offer.note ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.black,
                                                modifier = Modifier.weight(1f)
                                            )

                                            SmallIconButton(
                                                drawables.cancelIcon,
                                                colors.grayText
                                            ) {
                                                if (!isClicked.value) {
                                                    isClicked.value = true
                                                    offerViewModel.deleteNote(offer.id) {
                                                        isClicked.value = false
                                                        component.updateOffer(
                                                            offer.id,
                                                            model.isSnapshot
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    //simple Price
                                    if ((offerState.value != OfferStates.ACTIVE && offerState.value != OfferStates.PROTOTYPE) || isMyOffer.value ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = (offer.currentPricePerItem ?: "") +
                                                        " " + stringResource(strings.currencySign),
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = colors.priceTextColor
                                            )

    //                                    if(offerState.value == OfferStates.SNAPSHOT){
    //                                        ActionButton(
    //                                            strings.currentStateOfferLabel
    //                                        ){
    //                                            component.navigateToOffers(offer.id)
    //                                        }
    //                                    }
                                        }
                                    }
                                }

                                item {
                                    //action seller mode and active promo options
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                    ) {
                                        SeparatorLabel(stringResource(strings.actionsOffersParameterName))

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                SimpleTextButton(
                                                    text = stringResource(strings.actionsLabel),
                                                    textStyle = MaterialTheme.typography.bodyMedium,
                                                    textColor = colors.white,
                                                    backgroundColor = colors.steelBlue,
                                                    leadIcon = {
                                                        Icon(
                                                            painter = painterResource(drawables.shareMenuIcon),
                                                            contentDescription = "",
                                                            modifier = Modifier.size(dimens.smallIconSize),
                                                            tint = colors.white
                                                        )
                                                    },
                                                ) {
                                                    isShowOptions.value = !isShowOptions.value
                                                }

                                                PopUpMenu(
                                                    openPopup = isShowOptions.value,
                                                    onClosed = { isShowOptions.value = false },
                                                    menuList = operationsList
                                                )
                                            }

                                            Column {
                                                val isOpenPopup =
                                                    remember { mutableStateOf(false) }

                                                if (isMyOffer.value && offerState.value == OfferStates.ACTIVE) {
                                                    PromoBuyBtn {
                                                        isOpenPopup.value = true
                                                    }
                                                }

                                                PopUpMenu(
                                                    openPopup = isOpenPopup.value,
                                                    menuList = menuPromoList,
                                                    onClosed = { isOpenPopup.value = false }
                                                )
                                            }
                                        }

                                        if (offer.promoOptions != null && isMyOffer.value) {
                                            PromoRow(
                                                offer.promoOptions,
                                                showName = true,
                                                modifier = Modifier.padding(dimens.mediumPadding)
                                            ) {

                                            }
                                        }
                                    }

                                }

                                item {
                                    // state params
                                    Column(
                                        modifier = Modifier
                                            .background(
                                                colors.white,
                                                MaterialTheme.shapes.small
                                            )
                                            .clip(MaterialTheme.shapes.small)
                                            .padding(dimens.smallPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                    ) {
                                        //bids winner or last bid
                                        BidsWinnerOrLastBid(offer, offerState.value) {
                                            scope.launch {
                                                stateColumn.animateScrollToItem(goToBids)
                                            }
                                        }

                                        TimeOfferSession(
                                            offer,
                                            remainingTime.value,
                                            offerState.value,
                                        )

                                        LocationOffer(offer) {
                                            //go to Listing
                                            component.goToRegion(offer.region)
                                        }
                                    }
                                }

                                item {
                                    //bids price
                                    if (offer.saleType != "buy_now" && !isMyOffer.value && offerState.value == OfferStates.ACTIVE) {
                                        AuctionPriceLayout(
                                            offer = offer,
                                            offerViewModel.updateItemTrigger.value,
                                            onAddBidClick = { bid ->
                                                if (UserData.token != "") {
                                                    myMaximalBid.value = bid
                                                    showBidDialog.value = true
                                                }else{
                                                    component.goToLogin()
                                                }
                                            },
                                            modifier = Modifier
                                        )
                                    }
                                }

                                item {
                                    //buy now price
                                    if ((offer.saleType == "buy_now" || offer.saleType == "auction_with_buy_now") && !isMyOffer.value) {
                                        val values = remember { (1..offer.originalQuantity).map { it.toString() } }
                                        val valuesPickerState = rememberPickerState()

                                        BuyNowPriceLayout(
                                            offer = offer,
                                            offerState.value,
                                            onBuyNowClick = {
                                                if (UserData.token != "") {
                                                    if (offer.originalQuantity > 1) {
                                                        showDialog.value = true
                                                    } else {
                                                        val item = Pair(
                                                            offer.sellerData?.id ?: 1L, listOf(
                                                                SelectedBasketItem(
                                                                    offerId = offer.id,
                                                                    pricePerItem = offer.currentPricePerItem?.toDouble()
                                                                        ?: 0.0,
                                                                    selectedQuantity = 1
                                                                )
                                                            )
                                                        )
                                                        component.goToCreateOrder(item)
                                                    }
                                                }else{
                                                    component.goToLogin()
                                                }
                                            },
                                            onAddToCartClick = {
                                                if (UserData.token != "") {
                                                    val bodyAddB = HashMap<String, JsonElement>()
                                                    bodyAddB["offer_id"] = JsonPrimitive(offer.id)
                                                    offerViewModel.addOfferToBasket(
                                                        bodyAddB
                                                    ) { hm ->
                                                        offerViewModel.showToast(
                                                            successToastItem.copy(
                                                                message = hm
                                                            )
                                                        )
                                                    }
                                                }else{
                                                    component.goToLogin()
                                                }
                                            },
                                            onSaleClick = {
                                                component.goToCreateOffer(
                                                    CreateOfferType.COPY_PROTOTYPE,
                                                    offer.catpath,
                                                    offer.id,
                                                    images
                                                )
                                            },
                                            modifier = Modifier
                                        )

                                        CustomDialog(
                                            showDialog = showDialog.value,
                                            title = AnnotatedString(""),
                                            body = {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    SeparatorLabel(
                                                        stringResource(strings.chooseAmountLabel)
                                                    )

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .padding(dimens.mediumPadding),
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        ListPicker(
                                                            state = valuesPickerState,
                                                            items = values,
                                                            visibleItemsCount = 3,
                                                            modifier = Modifier.fillMaxWidth(0.5f),
                                                            textModifier = Modifier.padding(dimens.smallPadding),
                                                            textStyle = MaterialTheme.typography.titleLarge,
                                                            dividerColor = colors.textA0AE
                                                        )
                                                    }
                                                }
                                            },
                                            onSuccessful = {
                                                val item = Pair(offer.sellerData?.id ?: 1L, listOf(SelectedBasketItem(
                                                    offerId = offer.id,
                                                    pricePerItem = offer.currentPricePerItem?.toDouble() ?: 0.0,
                                                    selectedQuantity = valuesPickerState.selectedItem.toIntOrNull() ?: 1
                                                )))
                                                component.goToCreateOrder(item)
                                                showDialog.value = false
                                            },
                                            onDismiss = {
                                                showDialog.value = false
                                            }
                                        )
                                    }
                                }

                                item {
                                    // seller panel
                                    if (offer.sellerData != null && !isMyOffer.value) {
                                        Column(
                                            modifier = Modifier,
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                        ) {
                                            // SeparatorLabel(stringResource(strings.sellerLabel))
                                            val errorString = remember { mutableStateOf("") }

                                            UserPanel(
                                                modifier = Modifier
                                                    .background(
                                                        colors.white,
                                                        MaterialTheme.shapes.small
                                                    )
                                                    .clip(MaterialTheme.shapes.small).fillMaxSize(),
                                                offer.sellerData,
                                                updateTrigger = offerViewModel.updateItemTrigger.value,
                                                goToUser = {
                                                    component.goToUser(
                                                        offer.sellerData?.id ?: 1L,
                                                        false
                                                    )
                                                },
                                                goToAllLots = {
                                                    component.goToUsersListing(offer.sellerData)
                                                },
                                                goToAboutMe = {
                                                    component.goToUser(offer.sellerData?.id ?: 1L, true)
                                                },
                                                addToSubscriptions = {
                                                    if (UserData.token != "") {
                                                        offerViewModel.addNewSubscribe(
                                                            LD(),
                                                            SD().copy(
                                                                userLogin = offer.sellerData?.login,
                                                                userID = offer.sellerData?.id ?: 1L,
                                                                userSearch = true
                                                            ),
                                                            onSuccess = {
                                                                offerViewModel.getUserInfo(
                                                                    offer.sellerData?.id ?: 1L
                                                                )
                                                            },
                                                            errorCallback = { es ->
                                                                errorString.value = es
                                                            }
                                                        )
                                                    } else {
                                                        component.goToLogin()
                                                    }
                                                },
                                                goToSubscriptions = {
                                                    component.goToSubscribes()
                                                },
                                                goToSettings = {
                                                    component.goToDynamicSettings(it, null)
                                                },
                                                isBlackList = blackList.value
                                            )

                                            CreateSubscribeDialog(
                                                errorString.value != "",
                                                errorString.value,
                                                onDismiss = {
                                                    errorString.value = ""
                                                },
                                                goToSubscribe = {
                                                    component.goToSubscribes()
                                                    errorString.value = ""
                                                }
                                            )
                                        }
                                    }
                                }
                                item {
                                    // actions and other status
                                    Column(
                                        modifier = Modifier
                                            .background(
                                                colors.white,
                                                MaterialTheme.shapes.small
                                            )
                                            .clip(MaterialTheme.shapes.small)
                                            .padding(dimens.smallPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(
                                            dimens.smallPadding
                                        )
                                    ) {
                                        //mail to seller
                                        if (offer.sellerData != null && !isMyOffer.value && offerState.value == OfferStates.ACTIVE) {
                                            MessageToSeller(
                                                offer,
                                                onClick = {
                                                    if (UserData.token != "") {
                                                        isShowMesDialog.value = true
                                                    } else {
                                                        component.goToLogin()
                                                    }
                                                }
                                            )

                                            if (isShowMesDialog.value) {
                                                OfferMessagingDialog(
                                                    isShowMesDialog.value,
                                                    offer.parseToOfferItem(),
                                                    onSuccess = { dialogId ->
                                                        component.goToDialog(dialogId)
                                                        isShowMesDialog.value = false
                                                    },
                                                    onDismiss = {
                                                        isShowMesDialog.value = false
                                                    },
                                                    baseViewModel = offerViewModel
                                                )
                                            }
                                        }

                                        //make proposal to seller
                                        if (offer.isProposalEnabled) {
                                            ProposalToSeller(
                                                isMyOffer.value,
                                            ) {
                                                if (UserData.token.isNotBlank()) {
                                                    if (UserData.login == offer.sellerData?.id) {
                                                        component.goToProposalPage(
                                                            ProposalType.ACT_ON_PROPOSAL
                                                        )
                                                    } else {
                                                        component.goToProposalPage(
                                                            ProposalType.MAKE_PROPOSAL
                                                        )
                                                    }
                                                } else {
                                                    component.goToLogin()
                                                }
                                            }
                                        }
                                        // who pays for delivery
                                        Row(
                                            modifier = Modifier
                                                .padding(dimens.smallPadding),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                dimens.smallPadding
                                            )
                                        ) {
                                            Image(
                                                painterResource(drawables.deliveryIcon),
                                                contentDescription = null,
                                                modifier = Modifier.size(dimens.smallIconSize),
                                            )

                                            Text(
                                                text = stringResource(strings.whoPayForDeliveryLabel),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.grayText,
                                                modifier = Modifier.fillMaxWidth(0.5f)
                                            )

                                            Text(
                                                text = offer.whoPaysForDelivery?.name
                                                    ?: "",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = colors.black,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }

                                        if (offer.antisniper) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(dimens.smallPadding),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    dimens.smallPadding
                                                )
                                            ) {
                                                Icon(
                                                    painterResource(drawables.antiSniperIcon),
                                                    contentDescription = null,
                                                    tint = colors.negativeRed,
                                                    modifier = Modifier.size(dimens.smallIconSize),
                                                )

                                                Text(
                                                    text = stringResource(strings.antiSniperEnabledLabel),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = colors.inactiveBottomNavIconColor
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    //payment and delivery
                                    if (offerState.value != OfferStates.PROTOTYPE) {
                                        PaymentAndDeliverySection(
                                            offer.dealTypes,
                                            offer.paymentMethods,
                                            offer.deliveryMethods,
                                            if(!isBigScreen.value) Modifier.fillMaxWidth() else Modifier
                                        )
                                    }
                                }
                                item {
                                    if (offerState.value != OfferStates.PROTOTYPE) {
                                        //Parameters
                                        offer.params?.let {
                                            ParametersSection(it, if(!isBigScreen.value) Modifier.fillMaxWidth() else Modifier)
                                        }
                                    }
                                }
                            }
                        )
                    }
                    //descriptions offer
                    item {
                        DescriptionHtmlOffer(offer)
                    }
                    //bids list
                    item {
                        if (offerState.value == OfferStates.ACTIVE) {
                            AuctionBidsSection(
                                offer,
                                offerViewModel.updateItemTrigger.value,
                                isDeletesBids = false,
                                onRebidClick = {
                                    if (UserData.token != "") {
                                        showBidDialog.value = true
                                    }else{
                                        component.goToLogin()
                                    }
                                },
                                goToUser = {
                                    component.goToUser(it, false)
                                }
                            )
                        }
                    }
                    // removed bids
                    item {
                        if (offerState.value == OfferStates.ACTIVE) {
                            AuctionBidsSection(
                                offer,
                                offerViewModel.updateItemTrigger.value,
                                isDeletesBids = true,
                                onRebidClick = {
                                    if (UserData.token != "") {
                                        showBidDialog.value = true
                                    }else{
                                        component.goToLogin()
                                    }
                                }
                            )
                        }
                    }
                    //recommended list offers
                    item {
                       if (ourChoiceList.value.isNotEmpty()) {
                           SeparatorLabel(stringResource(strings.ourChoice))
                           LazyRowWithScrollBars(
                               heightMod = Modifier.fillMaxSize().heightIn(max = 300.dp).padding(
                                   bottom = dimens.largePadding,
                                   top = dimens.mediumPadding
                               ),
                           ) {
                               items(ourChoiceList.value) { offer ->
                                   PromoOfferRowItem(
                                       offer
                                   ) {
                                       component.navigateToOffers(offer.id)
                                   }
                               }
                           }
                       }
                    }
                    // visited list offers
                    item {
                        if (visitedHistory.value.isNotEmpty()) {
                            SeparatorLabel(stringResource(strings.lastViewedOffers))
                        }
                        LazyRowWithScrollBars(
                            heightMod = Modifier.fillMaxSize().heightIn(max = 300.dp).padding(
                                bottom = dimens.largePadding,
                                top = dimens.mediumPadding
                            ),
                        ) {
                            items(visitedHistory.value) { offer ->
                                PromoOfferRowItem(
                                    offer
                                ) {
                                    component.navigateToOffers(offer.id)
                                }
                            }
                        }
                    }
                }

                AddBidDialog(
                    showBidDialog.value,
                    myMaximalBid.value,
                    onDismiss = {
                        showBidDialog.value = false
                    },
                    onSuccess = {
                        offerViewModel.addBid(
                            myMaximalBid.value,
                            offer,
                            onSuccess = {
                                offerViewModel.updateBidsInfo(offer)
                                showBidDialog.value = false
                                scope.launch {
                                    stateColumn.animateScrollToItem(goToBids)
                                }
                            },
                            onDismiss = {
                                showBidDialog.value = false
                            }
                        )
                    },
                )

                OfferOperationsDialogs(
                    offer.parseToOfferItem(),
                    title,
                    fields,
                    showOperationsDialog,
                    offerViewModel,
                    updateItem = { id ->
                        component.updateOffer(id, model.isSnapshot)
                    }
                )
            }
        }
    }
}

@Composable
fun DescriptionHtmlOffer(
    offer: Offer
){
    val state = rememberRichTextState()
    val standardState = rememberRichTextState()
    val sst = stringResource(strings.standardDescriptionParameterName)
    val ast = stringResource(strings.additionalDescriptionsParameterName)


    val descriptionsDecodeHtmlString : MutableState<AnnotatedString> = remember {
        mutableStateOf(
            buildAnnotatedString {

                offer.standardDescriptions?.forEach { standard ->
                    if ( standard.description != null) {
                        val formattedDate =
                            standard.timestamp.toString().convertDateWithMinutes()

                        append("\n")
                        if (standard.deleted == false && standard.active == true) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(sst)
                            }
                            append("\n")
                            append(standardState.setHtml(standard.description).annotatedString)
                            append("\n")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$sst $formattedDate")
                            }
                        } else {
                            append("\n")
                            append(standard.description)
                        }
                    }
                }

                offer.addedDescriptions?.forEach { added ->
                    if ( added.text != null) {
                        val formattedDate =
                            added.timestamp.toString().convertDateWithMinutes()

                        append("\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$ast $formattedDate")
                        }
                        append("\n")
                        append("${added.text}\n")

                    }
                }
            }
        )
    }

    SeparatorLabel(stringResource(strings.description))

    Box(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .fillMaxWidth()
            .padding(dimens.smallPadding)
    ) {
        state.setHtml(offer.description ?: "")

        Column {
            Text(
                text = state.annotatedString,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                modifier = Modifier.padding(dimens.smallPadding)
            )

            Text(
                text = descriptionsDecodeHtmlString.value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                modifier = Modifier.padding(dimens.smallPadding)
            )
        }
    }
}

@Composable
fun getCountString(offerState: OfferStates, offer: Offer): String {
    val saleType = offer.saleType
    val isCompleted = offerState == OfferStates.COMPLETED
    val isCompletedOrInActive = isCompleted || offerState == OfferStates.INACTIVE

    val quantityInfo = "${stringResource(strings.quantityParameterName)}: ${offer.estimatedActiveOffersCount}"

    val qFullInfo = "${stringResource(strings.quantityParameterName)}: ${offer.currentQuantity} ${stringResource(strings.fromParameterName)} ${offer.originalQuantity}"

    return when {
        saleType == "buy_now" -> {
            if (isCompletedOrInActive) {
                quantityInfo
            } else {
                qFullInfo
            }
        }

        saleType == "auction_with_buy_now" && offer.originalQuantity > 1 -> {
            if (isCompletedOrInActive) {
                quantityInfo
            } else {
                qFullInfo
            }
        }

        else -> ""
    }
}

@Composable
fun AuctionPriceLayout(
    offer: Offer,
    updateTrigger: Int,
    onAddBidClick: (String) -> Unit,
    modifier: Modifier
) {
    if (updateTrigger < 0) return

    val myMaximalBid = remember {
        mutableStateOf(
            TextFieldValue(offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: "")
        )
    }

    Row(
        modifier = modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .padding(dimens.mediumPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding, Alignment.End),
        verticalAlignment = Alignment.Top
    ) {
        // Current price
        Column(
            modifier = Modifier.padding(dimens.smallPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.Top)
        ) {
            Text(
                text = stringResource(strings.currentPriceParameterName),
                style = MaterialTheme.typography.bodySmall,
                color = colors.grayText,
            )

            Text(
                text = offer.currentPricePerItem.toString() + " " + stringResource(strings.currencySign),
                style = MaterialTheme.typography.titleLarge,
                color = colors.priceTextColor,
                fontWeight = FontWeight.Bold,
            )
        }

        // Your maximum bid
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding, Alignment.Top)
        ) {
            OutlinedTextInputField(
                value = myMaximalBid.value,
                onValueChange = {
                    myMaximalBid.value = it
                },
                label = stringResource(strings.yourBidLabel),
                suffix = stringResource(strings.currencySign),
                keyboardType = KeyboardType.Number,
                placeholder = offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: "",
                modifier = Modifier.widthIn(max = 200.dp)
            )

            SimpleTextButton(
                text = stringResource(strings.actionAddBid),
                backgroundColor = colors.inactiveBottomNavIconColor,
                textColor = colors.alwaysWhite,
                onClick = {
                    onAddBidClick(myMaximalBid.value.text)
                },
            )
        }
    }
}

@Composable
fun BuyNowPriceLayout(
    offer: Offer,
    offerState: OfferStates,
    onBuyNowClick: () -> Unit,
    onSaleClick: () -> Unit = {},
    onAddToCartClick: () -> Unit,
    modifier: Modifier
) {
    if (offerState == OfferStates.PROTOTYPE || offerState == OfferStates.ACTIVE) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.white,MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .padding(dimens.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.padding(vertical = dimens.smallPadding),
            ) {
                Text(
                    text = stringResource(strings.buyNowPriceParameterName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText,
                )
            }

            // Current price row with discount
            if (offer.discountPercentage > 0 && offer.buyNowPrice != offer.currentPricePerItem) {
                Row(
                    modifier = Modifier.padding(dimens.smallPadding),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiscountText(price = offer.currentPricePerItem.toString())

                    // Discount badge
                    val discountText = "-${offer.discountPercentage}%"
                    DiscountBadge(
                        text = discountText
                    )
                }
            }

            // Current price display
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (offer.buyNowPrice ?: "") + " "
                            + stringResource(strings.currencySign),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.priceTextColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                SimpleTextButton(
                    text = stringResource(strings.buyNow),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    modifier = Modifier
                        .padding(horizontal = dimens.mediumPadding),
                    textColor = colors.alwaysWhite,
                    onClick = onBuyNowClick,
                )


                if (offerState == OfferStates.PROTOTYPE) {
                    SimpleTextButton(
                        text = stringResource(strings.saleLabel),
                        backgroundColor = colors.brightGreen,
                        modifier = Modifier
                            .padding(horizontal = dimens.mediumPadding),
                        textColor = colors.alwaysWhite,
                        onClick = onSaleClick,
                    )
                }

                if (offerState == OfferStates.ACTIVE) {
                    Box(
                        modifier = Modifier
                            .background(colors.brightGreen,MaterialTheme.shapes.small)
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                onAddToCartClick()
                            }
                            .padding(dimens.smallPadding)
                    ) {
                        Icon(
                            painter = painterResource(drawables.addToCartIcon),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.mediumIconSize),
                            tint = colors.alwaysWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageToSeller(
    offer: Offer,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Icon(
            painterResource(drawables.mail),
            contentDescription = null,
            tint = colors.actionTextColor,
            modifier = Modifier.size(dimens.smallIconSize),
        )

        Column {
            Text(
                text = stringResource(strings.actionAskSellerLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.actionTextColor
            )

            offer.sellerData?.averageResponseTime?.let {
                if (it != "") {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.notifyTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun ProposalToSeller(
    isMyOffer: Boolean,
    goToProposalPage: () -> Unit
){
    Row(
        modifier = Modifier
            .clickable {
                goToProposalPage()
            }
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Icon(
            painterResource(if(!isMyOffer) drawables.makeProposalIcon else drawables.proposalIcon),
            contentDescription = null,
            tint = colors.priceTextColor,
            modifier = Modifier.size(dimens.smallIconSize),
        )

        Text(
            text = if(!isMyOffer) stringResource(strings.actionProposalPriceLabel) else stringResource(strings.proposalTitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.priceTextColor
        )
    }
}

@Composable
fun BidsWinnerOrLastBid(
    offer: Offer,
    offerState: OfferStates,
    onClick: () -> Unit
){
    val bids = offer.bids

    val sourceString: AnnotatedString = if (!bids.isNullOrEmpty()) {
        val bidsCount = bids.size
        // Get the leader's login, preferring obfuscatedMoverLogin if available
        val leadingBidder = bids[0].obfuscatedMoverLogin ?: bids[0].moverLogin ?: ""

        // Annotated string to format the text
        buildAnnotatedString {
            if (offerState == OfferStates.ACTIVE) {
                withStyle(style = SpanStyle(color = colors.actionTextColor)) {
                    append("${stringResource(strings.bidsMadeLabel)} : ")
                }
                append("$bidsCount \n")
                append("${stringResource(strings.leadingBidsNowLabel)} : ")
            }else{
                append("${stringResource(strings.winnerParameterName)} ")
            }

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.priceTextColor)) {
                append(leadingBidder)
            }
        }
    } else {
        if (offer.saleType == "buy_now") {
            if (offer.buyerData?.login != null){
                buildAnnotatedString {
                    append("${stringResource(strings.buyerParameterName)} ")

                    withStyle(style = SpanStyle(color = colors.actionTextColor)) {
                        append(offer.buyerData?.login)
                    }
                }
            }else{
                buildAnnotatedString {}
            }

        } else {
            if (offerState == OfferStates.ACTIVE) {
                // No bids yet message
                buildAnnotatedString {
                    append("${stringResource(strings.noBids)}, ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = colors.priceTextColor
                        )
                    ) {
                        append(stringResource(strings.yourBidsFirstLabel))
                    }
                }
            }else{
                buildAnnotatedString {}
            }
        }
    }
    if ( sourceString.text != "") {
        Row(
            modifier = Modifier
                .clickable {
                    onClick()
                }
                .fillMaxWidth()
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            Image(
                painterResource(drawables.iconGroup),
                contentDescription = null,
                modifier = Modifier.size(dimens.smallIconSize)
            )

            Text(
                text = sourceString,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
        }
    }
}

@Composable
fun LocationOffer(
    offer: Offer,
    goToLocation: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                goToLocation()
            }
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Image(
            painterResource(drawables.locationIcon),
            contentDescription = null,
            modifier = Modifier.size(dimens.smallIconSize)
        )

        Text(
            text = buildAnnotatedString {
                if (offer.freeLocation != null) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(offer.freeLocation)
                    }
                }

                if (offer.region?.name  != null){
                    append(", ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.actionTextColor)) {
                        append(offer.region!!.name)
                    }
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
    }
}

@Composable
fun TimeOfferSession(
    offer: Offer,
    updatedTime: Long,
    state: OfferStates
) {
    if (state != OfferStates.PROTOTYPE && state != OfferStates.SNAPSHOT) {
        // Preload string resources
        val inactiveLabel = stringResource(strings.offerSessionInactiveLabel)
        val completedLabel = stringResource(strings.offerSessionCompletedLabel)
        val futureLabel = stringResource(strings.offerSessionFutureLabel)
        val daysLabel = stringResource(strings.dayLabel)
        val hoursLabel = stringResource(strings.hoursLabel)
        val minutesLabel = stringResource(strings.minutesLabel)
        val secondsLabel = stringResource(strings.secondsLabel)
        val beforeGraduationLabel = stringResource(strings.beforeGraduationLabel)

        val endTime = offer.session?.end?.toLongOrNull()

        // AnnotatedString to track the styled remaining time
        val remainingTime = when (state) {
            OfferStates.ACTIVE -> {
                if (endTime != null) {
                    buildAnnotatedString {
                        append(
                            formatRemainingTimeAnnotated(
                                updatedTime,
                                beforeGraduationLabel,
                                daysLabel,
                                hoursLabel,
                                minutesLabel,
                                secondsLabel
                            )
                        )
                    }
                } else {
                    buildAnnotatedString {
                        append(completedLabel)
                    }
                }
            }

            OfferStates.INACTIVE -> {
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(inactiveLabel)
                    }
                }
            }

            OfferStates.COMPLETED -> {
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(completedLabel)
                    }
                }
            }

            OfferStates.FUTURE -> {
                buildAnnotatedString {
                    append(futureLabel)
                }
            }

            else -> {
                AnnotatedString("")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(drawables.iconClock),
                contentDescription = null,
                modifier = Modifier.size(dimens.smallIconSize)
            )
            // Display the styled AnnotatedString
            Text(
                text = buildAnnotatedString {
                    append(remainingTime)
                    if (state != OfferStates.FUTURE) {
                        if (offer.session?.end != null) {
                            append("\n(${offer.session?.end?.convertDateWithMinutes()})")
                        }
                    }else{
                        if (offer.session?.start != null) {
                            append("\n(${offer.session?.start?.convertDateWithMinutes()})")
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
        }
    }
}

fun formatRemainingTimeAnnotated(
    millisUntilFinished: Long,
    beforeGraduationLabel: String,
    daysLabel: String,
    hoursLabel: String,
    minutesLabel: String,
    secondsLabel: String
): AnnotatedString {
    val days = millisUntilFinished / (1000 * 60 * 60 * 24)
    val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
    val minutes = (millisUntilFinished / (1000 * 60)) % 60
    val seconds = (millisUntilFinished / 1000) % 60

    return buildAnnotatedString {
        if (days > 0) {
            append("$days ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(daysLabel)
            }
            append(" $beforeGraduationLabel")
        } else {
            if (hours > 0) {
                append("$hours ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(hoursLabel)
                }
                append(" ")
            }
            if (minutes > 0) {
                append("$minutes ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(minutesLabel)
                }
                append(" ")
            }

            if (seconds > 0) {
                append("$seconds ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(secondsLabel)
                }
            }

            append(" $beforeGraduationLabel")
        }
    }
}

@Composable
fun PaymentAndDeliverySection(
    dealTypes: List<DealType>?,
    paymentMethods: List<PaymentMethod>?,
    deliveryMethods: List<DeliveryMethod>?,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        SeparatorLabel(stringResource(strings.paymentAndDeliveryLabel))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.white, shape = MaterialTheme.shapes.small)
                .padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            // Deal Types
            InfoBlock(
                title = stringResource(strings.dealTypeLabel),
                content = dealTypes?.joinToString(separator = ". ") { it.name ?: "" } ?: ""
            )

            // Payment Methods
            InfoBlock(
                title = stringResource(strings.paymentMethodLabel),
                content = paymentMethods?.joinToString(separator = ". ") { it.name ?: "" } ?: ""
            )

            // Delivery Methods
            InfoBlock(
                title = stringResource(strings.deliveryMethodLabel),
                content = formatDeliveryMethods(deliveryMethods)
            )
        }
    }
}

@Composable
fun InfoBlock(title: String, content: String) {
    Column(
        modifier = Modifier
            .padding(vertical = dimens.smallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        horizontalAlignment = Alignment.Start
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.grayText,
        )

        // Content
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black,
        )
    }
}

@Composable
fun formatDeliveryMethods(deliveryMethods: List<DeliveryMethod>?): String {
    val currencySign = stringResource(strings.currencySign)
    val withCountry = stringResource(strings.withinCountry)
    val withWorld = stringResource(strings.withinWorld)
    val withCity = stringResource(strings.withinCity)
    val comment = stringResource(strings.commentLabel)

    return deliveryMethods?.joinToString(separator = "\n\n") { dm ->
        buildString {
            append(dm.name)
            dm.priceWithinCity?.let { price ->
                if (price.toDouble().toLong() != -1L) {
                    append("\n$withCity $price$currencySign")
                }
            }
            dm.priceWithinCountry?.let { price ->
                if (price.toDouble().toLong() != -1L) {
                    append("\n$withCountry $price$currencySign")
                }
            }
            dm.priceWithinWorld?.let { price ->
                if (price.toDouble().toLong() != -1L) {
                    append("\n$withWorld $price$currencySign")
                }
            }
            if (!dm.comment.isNullOrEmpty()) {
                append("\n$comment ${dm.comment}")
            }
        }
    } ?: ""
}

@Composable
fun ParametersSection(
    parameters: List<Param>?,
    modifier: Modifier
) {
    if (!parameters.isNullOrEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            SeparatorLabel(stringResource(strings.parametersLabel))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.white, shape = MaterialTheme.shapes.small)
                    .padding(dimens.smallPadding)
            ) {
                parameters.forEach { parameter ->
                    InfoBlock(
                        title = parameter.name ?: "",
                        content = formatParameterValue(parameter.value)
                    )
                }
            }
        }
    }
}

@Composable
fun formatParameterValue(value: Value?): String {
    return buildString {
        value?.valueFree?.let { append(it) }

        value?.valueChoices?.forEach {
            if (isNotEmpty()) append("; ")
            append(it.name)
        }

        if (isNotEmpty() && last() == ' ') {
            dropLast(2)
        }
    }
}

@Composable
fun AuctionBidsSection(
    offer: Offer,
    updateTrigger : Int,
    isDeletesBids : Boolean,
    onRebidClick: (id: Long) -> Unit,
    goToUser: (id: Long) -> Unit = {}
) {
    if (updateTrigger < 0) return
    val bids = if(isDeletesBids) offer.removedBids else offer.bids
    if (bids != null) {
        SeparatorLabel(
            title = stringResource(strings.bidsLabel),
            annotatedString = if(isDeletesBids) {
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = colors.titleTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(
                            stringResource(strings.removedBidsLabel)
                        )
                    }
                }
            }else{
                null
            }
        )

        ColumnWithScrollBars(
            modifier = Modifier.background(color = colors.white, shape = MaterialTheme.shapes.small)
            .heightIn(max = 400.dp)
            .padding(bottom = dimens.largePadding, top = dimens.mediumPadding)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(strings.bidsUserLabel),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.grayText,
                )
                if(!isDeletesBids) {
                    Text(
                        text = stringResource(strings.yourMaxBidParameterName),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.grayText,
                    )
                    Text(
                        text = stringResource(strings.dateParameterName),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.grayText,
                    )
                }else{
                    Text(
                        text = stringResource(strings.commentLabel),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.grayText,
                    )
                }
            }

            // List items
            bids.forEachIndexed { i, bid ->
                if (!isDeletesBids) {
                    if (bid is Bids) {
                        BidsListItem(
                            i = i,
                            bid = bid,
                            offer = offer,
                            goToUser = {
                                goToUser(it)
                            },
                            onRebidClick = onRebidClick
                        )
                    }
                }else{
                    if (bid is RemoveBid) {
                        RemovedBidsListItem(
                            i,
                            bid
                        )
                    }
                }
            }

            if (bids.isEmpty()) {
                Text(
                    text = stringResource(strings.noBids),
                    style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                    color = colors.notifyTextColor,
                    modifier = Modifier.padding(top = dimens.mediumPadding)
                )
            }
        }
    }
}
