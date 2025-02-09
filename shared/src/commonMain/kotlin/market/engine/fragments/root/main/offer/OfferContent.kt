package market.engine.fragments.root.main.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.launch
import market.engine.common.openUrl
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.network.networkObjects.DealType
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Param
import market.engine.core.network.networkObjects.PaymentMethod
import market.engine.core.network.networkObjects.Value
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.OfferStates
import market.engine.core.utils.convertDateWithMinutes
import market.engine.fragments.base.BaseContent
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.PopupActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.dialogs.AddBidDialog
import market.engine.widgets.dialogs.CreateOfferDialog
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.widgets.dialogs.ListPicker
import market.engine.widgets.dialogs.rememberPickerState
import market.engine.widgets.exceptions.FullScreenImageViewer
import market.engine.widgets.exceptions.HorizontalImageViewer
import market.engine.widgets.dropdown_menu.getOfferOperations
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.widgets.items.PromoOfferRowItem
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.bars.UserPanel
import market.engine.widgets.dialogs.CustomDialog
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

    val goToBids = 11

    val focusManager = LocalFocusManager.current

    val stateColumn = rememberLazyListState(
        initialFirstVisibleItemIndex = offerViewModel.scrollItem.value,
    )

    val scope = rememberCoroutineScope()

    BackHandler(model.backHandler){
        component.onBeakClick()
    }

    LaunchedEffect(stateColumn){
        snapshotFlow {
            stateColumn.firstVisibleItemIndex
        }.collect { item->
            offerViewModel.scrollItem.value = item
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            offerViewModel.addHistory(model.id)
            offerViewModel.clearTimers()
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
        }
    }

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(isError.value) { component.updateOffer(lotState.value?.id ?: 1L, model.isSnapshot) } }
    }else{
        null
    }

    lotState.value?.let { offer ->

        val showBidDialog = remember { mutableStateOf(false) }
        val myMaximalBid = remember { mutableStateOf(offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: "") }

        BaseContent(
            topBar = {
                OfferAppBar(
                    offer,
                    offerViewModel,
                    onBeakClick = {
                        if (!isImageViewerVisible.value) {
                            component.onBeakClick()
                        } else {
                            isImageViewerVisible.value = false
                        }
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
                Box(modifier.fillMaxSize()) {
                    LazyColumn(
                        state = stateColumn,
                        modifier = Modifier.background(color = colors.primaryColor)
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    isShowOptions.value = false
                                    focusManager.clearFocus()
                                })
                            },
                        contentPadding = PaddingValues(dimens.smallPadding),
                        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                    ) {
                        //images offer
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clickable { isImageViewerVisible.value = !isImageViewerVisible.value  },
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
                        //title
                        item {
                            TitleText(
                                offer.title ?: "",
                                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                            )
                        }
                        //simple Price
                        if ((offerState.value != OfferStates.ACTIVE && offerState.value != OfferStates.PROTOTYPE) || isMyOffer.value ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                ) {
                                    Text(
                                        text = (offer.currentPricePerItem ?: "") +
                                                " " + stringResource(strings.currencySign),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colors.titleTextColor
                                    )
                                }
                            }
                        }
                        //action seller mode and active promo options
                        if (isMyOffer.value && offerState.value != OfferStates.SNAPSHOT) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                                ) {

                                    SeparatorLabel(stringResource(strings.actionsOffersParameterName))

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.smallPadding),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PopupActionButton(
                                            stringResource(strings.operationsParameterName),
                                            color = colors.steelBlue,
                                            tint = colors.alwaysWhite,
                                            isShowOptions = isShowOptions
                                        )
                                    }

                                    AnimatedVisibility(
                                        isShowOptions.value,
                                        enter = expandIn(),
                                        exit = fadeOut()
                                    ) {
                                        getOfferOperations(
                                            offer,
                                            offerViewModel,
                                            offset = DpOffset(20.dp, 0.dp),
                                            showCopyId = false,
                                            onUpdateMenuItem = { offer ->
                                                component.updateOffer(offer.id, model.isSnapshot)
                                            },
                                            goToCreateOffer = { type ->
                                                component.goToCreateOffer(type, offer.catpath, offer.id, null)
                                            },
                                            onClose = {
                                                isShowOptions.value = false
                                            },
                                            onBack = {
                                                component.onBeakClick()
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                PromoRow(
                                    offer,
                                    showName = true,
                                    modifier = Modifier.padding(dimens.mediumPadding)
                                ) {

                                }
                            }
                        }

                        //bids price
                        if (offer.saleType != "buy_now" && !isMyOffer.value && offerState.value == OfferStates.ACTIVE) {
                            item {
                                AuctionPriceLayout(
                                    offer = offer,
                                    offerViewModel.updateItemTrigger.value,
                                    myMaximalBid = myMaximalBid.value,
                                    onBidChanged = { newBid ->
                                        myMaximalBid.value = newBid
                                    },
                                    onAddBidClick = {
                                        if (UserData.token != "") {
                                            showBidDialog.value = true
                                        }else{
                                            component.goToLogin()
                                        }
                                    }
                                )
                            }
                        }
                        //buy now price
                        if ((offer.saleType == "buy_now" || offer.saleType == "auction_with_buy_now") && !isMyOffer.value) {
                            item {
                                val showDialog = remember { mutableStateOf(false) }
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
                                            offerViewModel.addToBasket(offer.id)
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
                                    }
                                )

                                CustomDialog(
                                    showDialog = showDialog.value,
                                    title = "",
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
                        // actions and other status
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(colors.white)
                                    .padding(dimens.smallPadding)
                            ) {
                                Column {
                                    //mail to seller
                                    if (offer.sellerData != null && !isMyOffer.value && offerState.value == OfferStates.ACTIVE) {
                                        MessageToSeller(
                                            offer,
                                            onClick = {
                                                if (UserData.token != "") {
                                                    isShowMesDialog.value = true
                                                }else{
                                                    component.goToLogin()
                                                }
                                            }
                                        )

                                        if (isShowMesDialog.value){
                                            CreateOfferDialog(
                                                isShowMesDialog.value,
                                                offer,
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
                                    if (false) {
                                        ProposalToSeller(
                                            if (UserData.login == offer.sellerData?.id) "act_on_proposal" else "make_proposal",
                                        )
                                    }
                                    // who pays for delivery
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(dimens.smallPadding),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        SmallImageButton(
                                            drawables.deliveryIcon
                                        ) {

                                        }

                                        Text(
                                            text = stringResource(strings.whoPayForDeliveryLabel),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.greenColor
                                        )

                                        Spacer(modifier = Modifier.width(dimens.smallSpacer))

                                        Text(
                                            text = offer.whoPaysForDelivery?.name ?: "",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.black,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }

                                    if (offer.antisniper) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(dimens.smallPadding),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            SmallIconButton(
                                                drawables.antiSniperIcon,
                                                colors.inactiveBottomNavIconColor,
                                            ) {

                                            }

                                            Text(
                                                text = stringResource(strings.antiSniperEnabledLabel),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.inactiveBottomNavIconColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // state params
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(colors.white)
                                    .padding(dimens.smallPadding)
                            ) {
                                Column {
                                    //bids winner or last bid
                                    BidsWinnerOrLastBid(offer, offerState.value){
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
                        }
                        // seller panel
                        item {
                            SeparatorLabel(stringResource(strings.sellerLabel))
                            val errorString = remember { mutableStateOf("") }

                            UserPanel(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(colors.white)
                                    .padding(vertical = dimens.smallPadding),
                                offer.sellerData,
                                updateTrigger = offerViewModel.updateItemTrigger.value,
                                goToUser = {
                                    component.goToUser(offer.sellerData?.id ?: 1L, false)
                                },
                                goToAllLots = {
                                    component.goToUsersListing(offer.sellerData)
                                },
                                goToAboutMe = {
                                    component.goToUser(offer.sellerData?.id ?: 1L, true)
                                },
                                addToSubscriptions = {
                                    if(UserData.token != "") {
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
                                            onError = { es ->
                                                errorString.value = es
                                            }
                                        )
                                    }else{
                                        component.goToLogin()
                                    }
                                },
                                goToSubscriptions = {
                                    component.goToSubscribes()
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
                        //payment and delivery
                        if (offerState.value != OfferStates.PROTOTYPE) {
                            item {
                                PaymentAndDeliverySection(
                                    offer.dealTypes,
                                    offer.paymentMethods,
                                    offer.deliveryMethods,
                                )
                            }
                            //Parameters
                            offer.params?.let {
                                item {
                                    ParametersSection(it)
                                }
                            }
                        }
                        //descriptions offer
                        item {
                            DescriptionHtmlOffer(offer)
                        }
                        //bids list
                        item {
                            if (offerState.value == OfferStates.ACTIVE) {
                                val showDialog = remember { mutableStateOf(false) }

                                AuctionBidsSection(
                                    offer,
                                    offerViewModel.updateItemTrigger.value,
                                    onRebidClick = {
                                        if (UserData.token != "") {
                                            showDialog.value = true
                                        }else{
                                            component.goToLogin()
                                        }
                                    }
                                )
                            }
                        }
                        // removed bids
                        item {

                        }
                        //recommended list offers
                        item {
                            if (ourChoiceList.value.isNotEmpty()) {
                                SeparatorLabel(stringResource(strings.ourChoice))

                                LazyRow(
                                    modifier = Modifier.height(300.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
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

                            LazyRow(
                                modifier = Modifier.height(300.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                    val sum = offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: ""

                    AddBidDialog(
                        showBidDialog.value,
                        sum,
                        onDismiss = {
                            showBidDialog.value = false
                        },
                        onSuccess = {
                            offerViewModel.addBid(
                                sum,
                                offer,
                                onSuccess = {
                                    component.updateOffer(offer.id, false)
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
                }
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
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.white)
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
    updateTrigger : Int,
    myMaximalBid: String,
    onBidChanged: (String) -> Unit,
    onAddBidClick: () -> Unit
) {
    if (updateTrigger < 0) return
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colors.white)
            .fillMaxWidth()
            .padding(dimens.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Current price
            Column(
                modifier = Modifier.padding(dimens.smallPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(strings.currentPriceParameterName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText,
                    modifier = Modifier.padding(dimens.smallPadding)
                )

                Text(
                    text = offer.currentPricePerItem.toString() + " " + stringResource(strings.currencySign),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.titleTextColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(dimens.smallPadding)
                )
            }

            // Your maximum bid
            Column(
                modifier = Modifier.padding(dimens.smallPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(strings.yourMaxBidParameterName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText,
                    modifier = Modifier.padding(dimens.smallPadding)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(dimens.smallPadding)
                ) {
                    OutlinedTextField(
                        value = myMaximalBid,
                        onValueChange = onBidChanged,
                        modifier = Modifier
                            .width(120.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        placeholder = {
                            Text(
                                text = offer.minimalAcceptablePrice ?: offer.currentPricePerItem ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.grayText
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = colors.white,
                            focusedContainerColor = colors.white,
                            unfocusedBorderColor = colors.black,
                            focusedBorderColor = colors.titleTextColor,
                            unfocusedTextColor = colors.black,
                            focusedTextColor = colors.black
                        ),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        })
                    )
                    Text(
                        text = stringResource(strings.currencySign),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(dimens.smallPadding)
                    )
                }

                Spacer(modifier = Modifier.height(dimens.smallSpacer))

                SimpleTextButton(
                    text = stringResource(strings.actionAddBid),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                    onClick = onAddBidClick,
                )
            }
        }
    }
}

@Composable
fun BuyNowPriceLayout(
    offer: Offer,
    offerState: OfferStates,
    onBuyNowClick: () -> Unit,
    onSaleClick: () -> Unit = {},
    onAddToCartClick: () -> Unit
) {
    if (offerState == OfferStates.PROTOTYPE || offerState == OfferStates.ACTIVE) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.white)
                .padding(dimens.mediumPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.smallPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = dimens.smallPadding),
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
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DiscountText(price = offer.buyNowPrice.toString())

                        Spacer(modifier = Modifier.width(dimens.extraSmallPadding))

                        // Discount badge
                        val discountText = "-${offer.discountPercentage}%"
                        DiscountBadge(
                            text = discountText
                        )
                    }
                }

                // Current price display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = (offer.buyNowPrice ?: "") + " "
                                + stringResource(strings.currencySign),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.titleTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(dimens.smallSpacer))

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
                            backgroundColor = colors.greenColor,
                            modifier = Modifier
                                .padding(horizontal = dimens.mediumPadding),
                            textColor = colors.alwaysWhite,
                            onClick = onSaleClick,
                        )
                    }

                    if (offerState == OfferStates.ACTIVE) {
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    onAddToCartClick()
                                }
                                .background(colors.brightGreen)
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
}

@Composable
fun MessageToSeller(
    offer: Offer,
    onClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SmallIconButton(
            drawables.mail,
            colors.actionTextColor,
        ) {

        }

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
    type : String
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding)
            .clickable {

            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SmallIconButton(
            drawables.makeProposalIcon,
            colors.inactiveBottomNavIconColor,
        ) {

        }

        Text(
            text = stringResource(strings.actionProposalPriceLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inactiveBottomNavIconColor
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

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.titleTextColor)) {
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
                            color = colors.titleTextColor
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
                .fillMaxWidth()
                .padding(dimens.smallPadding)
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            SmallImageButton(
                drawables.iconGroup,
            ) {

            }

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
            .padding(dimens.smallPadding)
            .clickable {
                goToLocation()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SmallImageButton(
            drawables.locationIcon,
            onClick = goToLocation
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
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallImageButton(
                drawables.iconClock
            ) {

            }
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
    deliveryMethods: List<DeliveryMethod>?
) {
    SeparatorLabel(stringResource(strings.paymentAndDeliveryLabel))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.white, shape = MaterialTheme.shapes.medium)
            .padding(dimens.smallPadding)
    ) {
        // Deal Types
        InfoBlock(
            title = stringResource(strings.dealTypeLabel),
            content = dealTypes?.joinToString(separator = ". ") { it.name ?: ""  } ?: ""
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

@Composable
fun InfoBlock(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.smallPadding)
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.grayText,
            modifier = Modifier.padding(dimens.extraSmallPadding)
        )

        // Content
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black,
            modifier = Modifier.padding(dimens.smallPadding)
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
    parameters: List<Param>?
) {
    if (!parameters.isNullOrEmpty()) {
        SeparatorLabel(stringResource(strings.parametersLabel))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.white, shape = MaterialTheme.shapes.medium)
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
    onRebidClick: (id: Long) -> Unit
) {
    if (updateTrigger < 0) return
    val bids = offer.bids
    if (bids != null) {
        SeparatorLabel(stringResource(strings.bidsLabel))
        var isRebidShown = false
        var isYourBidShown = false

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .background(color = colors.white, shape = MaterialTheme.shapes.medium)
                .padding(dimens.smallPadding)
        ) {
            // Header row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimens.mediumPadding),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = stringResource(strings.bidsUserLabel),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.grayText,
                    )
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
                }
            }

            // List items
            itemsIndexed(bids) { i, bid ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimens.smallPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${i + 1}. ${bid.obfuscatedMoverLogin ?: bid.moverLogin ?: "User"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black,
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${bid.curprice} ${stringResource(strings.currencySign)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black,
                        )

                        // Rebid Button logic
                        if (!isRebidShown && i == 0 && bid.moverId != UserData.login) {
                            isRebidShown = true
                            SimpleTextButton(
                                text = stringResource(strings.rebidLabel),
                                backgroundColor = colors.notifyTextColor,
                                textColor = colors.alwaysWhite,
                                textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.heightIn(max = 35.dp),
                            ) {
                                onRebidClick(offer.id)
                            }
                        }

                        if (!isYourBidShown && bid.moverId == UserData.login) {
                            isYourBidShown = true
                            SimpleTextButton(
                                text = stringResource(strings.yourBidLabel),
                                backgroundColor = colors.textA0AE,
                                textColor = colors.alwaysWhite,
                                textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.heightIn(max = 35.dp),
                            ) {
                            }
                        }
                    }

                    Text(
                        text = bid.ts?.convertDateWithMinutes() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (bids.isEmpty()) {
                item {
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
}
