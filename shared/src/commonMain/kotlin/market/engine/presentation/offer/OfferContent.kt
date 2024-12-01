package market.engine.presentation.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.SAPI
import market.engine.core.globalData.UserData
import market.engine.core.network.networkObjects.DealType
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Param
import market.engine.core.network.networkObjects.PaymentMethod
import market.engine.core.network.networkObjects.Value
import market.engine.core.types.OfferStates
import market.engine.core.util.convertDateWithMinutes
import market.engine.core.util.getCurrentDate
import market.engine.core.util.parseHtmlToAnnotatedString
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.PopupActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.exceptions.FullScreenImageViewer
import market.engine.widgets.exceptions.HorizontalImageViewer
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.exceptions.getOfferOperations
import market.engine.widgets.items.PromoOfferRowItem
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.texts.DiscountText
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfferContent(
    component: OfferComponent,
    modifier: Modifier
) {
    val mainViewModel: MainViewModel = koinViewModel()

    val model = component.model.subscribeAsState()
    val offerViewModel = model.value.offerViewModel

    val visitedHistory = offerViewModel.responseHistory.collectAsState()
    val ourChoiceList = offerViewModel.responseOurChoice.collectAsState()

    val catHistory = offerViewModel.responseCatHistory.collectAsState()
    val blackList = offerViewModel.statusList.collectAsState()

    val remainingTime = offerViewModel.remainingTime.collectAsState()

    val lotState = offerViewModel.responseOffer.collectAsState()

    val isLoading = offerViewModel.isShowProgress.collectAsState()

    val isImageViewerVisible = remember { mutableStateOf(false) }

    val scrollState = rememberCoroutineScope()

    val offerState = remember { mutableStateOf(OfferStates.ACTIVE) }
    val isShowOptions = remember { mutableStateOf(false) }
    val myMaximalBid = remember { mutableStateOf("") }
    val isMyOffer = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val offer = lotState.value

    if (offer != null) {

        val images = when {
            offer.images?.isNotEmpty() == true -> offer.images.map { it.urls?.big?.content.orEmpty() }
            offer.externalImages?.isNotEmpty() == true -> offer.externalImages
            else -> emptyList()
        }
        val pagerState = rememberPagerState(
            pageCount = { images.size },
        )

        LaunchedEffect(offer) {
            offerState.value = when {
                model.value.isSnapshot -> OfferStates.SNAPSHOT
                offer.isPrototype -> OfferStates.PROTOTYPE
                offer.state == "active" -> {
                    when {
                        offer.session == null -> OfferStates.COMPLETED
                        (offer.session.start?.toLongOrNull()
                            ?: 1L) > getCurrentDate().toLong() -> OfferStates.FUTURE

                        (offer.session.end?.toLongOrNull()
                            ?: 1L) - getCurrentDate().toLong() > 0 -> OfferStates.ACTIVE

                        else -> OfferStates.INACTIVE
                    }
                }

                offer.state == "sleeping" -> {
                    if (offer.session == null) OfferStates.COMPLETED else OfferStates.INACTIVE
                }

                else -> offerState.value
            }

            isMyOffer.value = UserData.userInfo?.login == offer.sellerData?.login

            //init timers
            val initTimer =
                ((offer.session?.end?.toLongOrNull() ?: 1L) - (getCurrentDate().toLongOrNull()
                    ?: 1L)) * 1000

            if (initTimer < 24 * 60 * 60 * 1000) {
                offerViewModel.startTimer(initTimer) {
                    component.updateOffer(offer.id)
                }
            }else{
                offerViewModel._remainingTime.value = initTimer
            }

            if (!isMyOffer.value && offerState.value == OfferStates.ACTIVE && offer.saleType != "buy_now") {
                offerViewModel.startTimerUpdateBids(offer) {
                    component.updateOffer(offer.id)
                }
            }

            mainViewModel.sendEvent(UIMainEvent.UpdateTopBar {
                if (!isImageViewerVisible.value) {
                    OfferAppBar(
                        offer,
                        isFavorite = offer.isWatchedByMe,
                        onFavClick = {

                        },
                        onCartClick = {

                        },
                        onBeakClick = {
                            component.onBeakClick()
                        }
                    )
                }
            })
            mainViewModel.sendEvent(UIMainEvent.UpdateFloatingActionButton {})
            mainViewModel.sendEvent(UIMainEvent.UpdateError(null))
            mainViewModel.sendEvent(UIMainEvent.UpdateNotFound(null))
        }

        DisposableEffect(Unit) {
            onDispose {
                offerViewModel.addHistory(model.value.id)
                offerViewModel.clearTimers()
            }
        }

        Box(modifier.fillMaxSize()) {
            SwipeRefreshContent(
                isRefreshing = isLoading.value,
                modifier = modifier.fillMaxSize(),
                onRefresh = {
                    component.updateOffer(model.value.id)
                },
            ) {
                AnimatedVisibility(
                    modifier = modifier.fillMaxSize(),
                    visible = !isLoading.value,
                    enter = expandIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
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
                                    .clickable { isImageViewerVisible.value = true },
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
                                            color = if (catHistory.value.size - 1 == index) colors.black else colors.steelBlue,
                                            modifier = Modifier.padding(dimens.smallPadding)
                                                .clickable {
                                                    //go to Listing
                                                    if (cat.isLeaf) {
                                                        //cat.parentId
                                                    } else {
                                                        //cat.id
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }
                        //count and views label
                        item {
                            val countString =
                                getCountString(offerState.value, offer, isMyOffer.value)

                            FlowRow(
                                horizontalArrangement = Arrangement.Start,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (countString.isNotEmpty()) {
                                    Text(
                                        text = countString,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.steelBlue,
                                        modifier = Modifier.padding(dimens.smallPadding)
                                    )
                                }

                                Text(
                                    text = stringResource(strings.viewsParams) + ": " + offer.viewsCount,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.steelBlue,
                                    modifier = Modifier.padding(dimens.smallPadding)
                                )
                            }
                        }
                        //title
                        item {
                            Text(
                                text = offer.title ?: "",
                                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimens.extraLargeText
                                ),
                            )
                        }
                        //simple Price
                        if (offerState.value == OfferStates.INACTIVE || offerState.value == OfferStates.COMPLETED || isMyOffer.value) {
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
                        // active promo options
                        if (isMyOffer.value) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TitleText(
                                        stringResource(strings.activatePromoParameterName),
                                        color = colors.black
                                    )
                                }

                                PromoRow(
                                    offer,
                                    showName = true,
                                    modifier = Modifier.padding(dimens.mediumPadding)
                                ) {

                                }
                            }
                        }
                        //action seller mode
                        if (isMyOffer.value) {
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TitleText(
                                            stringResource(strings.actionsOffersParameterName),
                                            color = colors.black
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        PopupActionButton(
                                            stringResource(strings.operationsParameterName),
                                            color = colors.textA0AE,
                                            tint = colors.alwaysWhite,
                                            isShowOptions = isShowOptions
                                        )
                                    }

                                    AnimatedVisibility(
                                        isShowOptions.value,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        getOfferOperations(
                                            offer,
                                            offset = IntOffset(-150, 0),
                                            showCopyId = false,
                                            onUpdateMenuItem = { offer ->
                                                component.updateOffer(offer.id)
                                            },
                                            onClose = {
                                                isShowOptions.value = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        //bids price
                        if (offer.saleType != "buy_now" && !isMyOffer.value && offerState.value == OfferStates.ACTIVE) {
                            item {
                                AuctionPriceLayout(
                                    offer = offer,
                                    myMaximalBid = myMaximalBid.value,
                                    onBidChanged = { newBid ->
                                        myMaximalBid.value = newBid
                                    },
                                    onAddBidClick = {

                                    }
                                )
                            }
                        }
                        //buy now price
                        if ((offer.saleType == "buy_now" || offer.saleType == "auction_with_buy_now") && !isMyOffer.value) {
                            item {
                                BuyNowPriceLayout(
                                    offer = offer,
                                    offerState.value,
                                    onBuyNowClick = {

                                    },
                                    onAddToCartClick = {

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
                                        MessageToSeller(offer)
                                    }

                                    //make proposal to seller
                                    if (offer.isProposalEnabled) {
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
                                            style = MaterialTheme.typography.titleMedium,
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
                                    BidsWinnerOrLastBid(offer, offerState.value)

                                    TimeOfferSession(
                                        offer,
                                        remainingTime.value,
                                        offerState.value,
                                    )

                                    LocationOffer(offer) {
                                        //go to Listing
                                    }
                                }
                            }
                        }
                        // seller panel
                        item {
                            SellerPanel(
                                offer,
                                goToSeller = {

                                },
                                goToAllLots = {

                                },
                                goToAboutMe = {

                                },
                                addToSubscriptions = {

                                },
                                isBlackList = blackList.value
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
                                AuctionBidsSection(
                                    offer,
                                    onRebidClick = { id ->
                                        // The same what and click to bids btn
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
                                TitleSeparatorOffer(strings.ourChoice)

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
                                TitleSeparatorOffer(strings.lastViewedOffers)
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
                }
                //full screen image viewer
                if (isImageViewerVisible.value) {
                    FullScreenImageViewer(
                        images = images,
                        initialIndex = pagerState.currentPage,
                        onClose = { exitPage ->
                            scrollState.launch {
                                isImageViewerVisible.value = false
                                pagerState.scrollToPage(exitPage)
                            }
                        }
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
    val descriptionsDecodeHtmlString : MutableState<AnnotatedString> = remember {
        mutableStateOf(
            buildAnnotatedString {

            }
        )
    }

    TitleSeparatorOffer(strings.description)

    val descriptionHtml = offer.description ?: ""
    descriptionsDecodeHtmlString.value = descriptionHtml.parseHtmlToAnnotatedString()

    offer.addedDescriptions?.forEach { added ->
        if ( added.text != null) {
            val formattedDate =
                added.timestamp.toString().convertDateWithMinutes()
            descriptionsDecodeHtmlString.value = buildAnnotatedString {
                append(descriptionsDecodeHtmlString.value)

                append("\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${stringResource(strings.additionalDescriptionsParameterName)} $formattedDate")
                }
                append("\n")
                append("${added.text}\n")
            }
        }
    }

    offer.standardDescriptions?.forEach { standard ->
        if ( standard.description != null) {
            val formattedDate =
                standard.timestamp.toString().convertDateWithMinutes()
            descriptionsDecodeHtmlString.value = buildAnnotatedString {
                append(descriptionsDecodeHtmlString.value)
                append("\n")
                if (!standard.deleted && standard.active) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(strings.standardDescriptionParameterName))
                    }
                    append("\n")
                    append(standard.description.parseHtmlToAnnotatedString())
                    append("\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${stringResource(strings.editedStandardDescriptionParameterName)} $formattedDate")
                    }
                } else {
                    append("\n")
                    append(standard.description.parseHtmlToAnnotatedString())
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.white)
            .padding(dimens.smallPadding)
    ) {
        Text(
            text = descriptionsDecodeHtmlString.value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
        )
    }
}

@Composable
fun getCountString(offerState: OfferStates, offer: Offer, isMyOffer: Boolean): String {
    val saleType = offer.saleType
    val isCompletedOrInactive = offerState == OfferStates.COMPLETED || offerState == OfferStates.INACTIVE

    return when {
        saleType == "buy_now" -> {
            val quantityInfo = "${stringResource(strings.quantityParameterName)}: ${offer.estimatedActiveOffersCount}"
            if (isCompletedOrInactive) {
                if (isMyOffer)
                    "${stringResource(strings.boughtParameterName)}: ${offer.estimatedActiveOffersCount} ${stringResource(strings.fromParameterName)} ${offer.originalQuantity}"
                else quantityInfo
            } else {
                "${stringResource(strings.quantityParameterName)}: ${offer.currentQuantity} ${stringResource(strings.fromParameterName)} ${offer.originalQuantity}"
            }
        }

        saleType == "auction_with_buy_now" && offer.originalQuantity > 1 -> {
            if (isCompletedOrInactive) {
                if (isMyOffer)
                    "${stringResource(strings.boughtParameterName)}: ${offer.estimatedActiveOffersCount} ${stringResource(strings.fromParameterName)} ${offer.originalQuantity}"
                else "${stringResource(strings.quantityParameterName)}: ${offer.estimatedActiveOffersCount}"
            } else {
                "${stringResource(strings.quantityParameterName)}: ${offer.currentQuantity} ${stringResource(strings.fromParameterName)} ${offer.originalQuantity}"
            }
        }

        else -> ""
    }
}

@Composable
fun AuctionPriceLayout(
    offer: Offer,
    myMaximalBid: String,
    onBidChanged: (String) -> Unit,
    onAddBidClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(colors.white)
            .fillMaxWidth()
            .padding(dimens.mediumPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Current price
            Column {
                Text(
                    text = stringResource(strings.currentPriceParameterName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText
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
            Column {
                Text(
                    text = stringResource(strings.yourMaxBidParameterName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText
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
            }
        }

        Spacer(modifier = Modifier.height(dimens.smallSpacer))

        SimpleTextButton(
            text = stringResource(strings.actionAddBid),
            backgroundColor = colors.inactiveBottomNavIconColor,
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = dimens.mediumPadding),
            textColor = colors.alwaysWhite,
            onClick = onAddBidClick,
        )
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
                .padding(dimens.mediumPadding)
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
                                .background(colors.brightGreen)
                                .padding(dimens.smallPadding)
                                .clickable {
                                    onAddToCartClick()
                                }
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
    offer: Offer
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
            drawables.mail,
            colors.actionTextColor,
        ) {

        }

        Column {
            offer.sellerData?.averageResponseTime?.let {
                if (it != "") {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.notifyTextColor
                    )
                }
            }

            Text(
                text = stringResource(strings.actionAskSellerLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.actionTextColor
            )
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
    offerState: OfferStates
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
                        append(offer.buyerData.login)
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
                        append(offer.region.name)
                    }
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellerPanel(
    offer: Offer,
    goToSeller: () -> Unit,
    goToAllLots: () -> Unit,
    goToAboutMe: () -> Unit,
    addToSubscriptions: () -> Unit,
    goToSettings: (() -> Unit)? = null,
    isBlackList: String? // Suspend function to check black/white lists
) {
    val seller = offer.sellerData

    if (seller != null) {

        TitleSeparatorOffer(strings.sellerLabel)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(colors.white)
                .padding(vertical = dimens.smallPadding)
        ) {
            // Header row with seller details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(dimens.smallPadding)
                    .clickable {
                        goToSeller()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Seller rating badge
                if (seller.ratingBadge?.imageUrl != null) {
                    LoadImage(
                        seller.ratingBadge.imageUrl,
                        isShowLoading = false,
                        isShowEmpty = false,
                        size = dimens.mediumIconSize
                    )
                    Spacer(modifier = Modifier.width(dimens.smallPadding))
                }

                // Verified seller icon
                if (seller.isVerified) {
                    Image(
                        painter = painterResource(drawables.verifySellersIcon),
                        contentDescription = null,
                        modifier = Modifier.size(dimens.mediumIconSize)
                    )
                    Spacer(modifier = Modifier.width(dimens.smallPadding))
                }

                val image = seller.avatar?.thumb?.content
                if (image != null && image != "${SAPI.SERVER_BASE}images/no_avatar.svg") {
                    Card(
                        modifier = Modifier.padding(dimens.smallPadding),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        LoadImage(
                            url = image,
                            isShowLoading = false,
                            isShowEmpty = false,
                            size = 60.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = seller.login ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.brightBlue,
                        modifier = Modifier.padding(dimens.smallPadding)
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    if (seller.rating > 0) {
                        Box(
                            modifier = Modifier
                                .background(colors.ratingBlue, shape = MaterialTheme.shapes.medium)
                                .padding(dimens.smallPadding)
                        ) {
                            Text(
                                text = seller.rating.toString(),
                                color = colors.alwaysWhite,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Button: "All Offers"
                SimpleTextButton(
                    text = stringResource(strings.allOffers),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    shape = MaterialTheme.shapes.medium,
                    onClick = goToAllLots
                )

                Spacer(modifier = Modifier.width(dimens.smallPadding))

                // Button: "About Me"
                SimpleTextButton(
                    text = stringResource(strings.aboutMeLabel),
                    backgroundColor = colors.textA0AE,
                    shape = MaterialTheme.shapes.medium,
                    onClick = goToAboutMe
                )

                Spacer(modifier = Modifier.width(dimens.smallPadding))

                // Add to subscriptions icon
                SmallIconButton(
                    icon = drawables.subscriptionIcon,
                    color = colors.greenColor,
                    modifierIconSize = Modifier.size(dimens.mediumIconSize),
                    onClick = addToSubscriptions
                )
            }

            Spacer(modifier = Modifier.height(dimens.smallPadding))

            // Check if the seller is in the black list
            // Status annotation display based on the list type
            isBlackList?.let { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.smallPadding)
                        .clickable {
                            when (status) {
                                "blacklist_sellers" -> goToSettings?.invoke() // Blacklist sellers action
                                "blacklist_buyers" -> goToSettings?.invoke() // Blacklist buyers action
                                "whitelist_buyers" -> goToSettings?.invoke() // Whitelist buyers action
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(drawables.infoIcon),
                        contentDescription = null,
                        tint = when (status) {
                            "blacklist_sellers", "blacklist_buyers" -> colors.notifyTextColor
                            "whitelist_buyers" -> colors.black
                            else -> colors.notifyTextColor
                        },
                        modifier = Modifier.size(dimens.smallIconSize)
                    )

                    Spacer(modifier = Modifier.width(dimens.smallPadding))

                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(strings.publicBlockUserLabel))
                            append(": ")
                            withStyle(
                                style = SpanStyle(
                                    color = colors.black,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append(
                                    when (status) {
                                        "blacklist_sellers" -> stringResource(strings.blackListUserLabel)
                                        "blacklist_buyers" -> stringResource(strings.blackListUserLabel)
                                        "whitelist_buyers" -> stringResource(strings.whiteListUserLabel)
                                        else -> ""
                                    }
                                )
                            }
                        },
                        color = when (status) {
                            "blacklist_sellers", "blacklist_buyers" -> colors.notifyTextColor
                            "whitelist_buyers" -> colors.black
                            else -> colors.notifyTextColor
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Display vacation or seller status
            if (seller.vacationEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.smallPadding)
                        .background(colors.outgoingBubble, shape = MaterialTheme.shapes.medium)
                        .clickable { goToSettings?.invoke() }
                        .padding(dimens.smallPadding)
                ) {
                    val vacationMessage = buildAnnotatedString {
                        // Header
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.notifyTextColor)) {
                            append(if (UserData.login == seller.id) {
                                stringResource(strings.publicVacationMyLabel)
                            } else {
                                stringResource(strings.publicVacationHeaderLabel)
                            })
                        }
                        append("\n")

                        // From Date
                        withStyle(style = SpanStyle(color = colors.brightBlue)) {
                            append("${stringResource(strings.fromParameterName)} ")
                            append(seller.vacationStart.toString().convertDateWithMinutes())
                        }
                        append(" ")

                        // To Date
                        withStyle(style = SpanStyle(color = colors.brightBlue)) {
                            append("${stringResource(strings.toAboutParameterName)} ")
                            append(seller.vacationEnd.toString().convertDateWithMinutes())
                        }
                        append(".\n")

                        // Vacation Comment
                        if (seller.vacationMessage?.isNotEmpty() == true) {
                            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.grayText)) {
                                append(seller.vacationMessage)
                            }
                        }
                    }

                    Text(
                        text = vacationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.notifyTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TimeOfferSession(
    offer: Offer,
    updatedTime: Long,
    state: OfferStates
) {
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
            .wrapContentHeight(),
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
                if (offer.session?.end != null) {
                    append(" \n (${offer.session.end.convertDateWithMinutes()})")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
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
        }
    }
}


@Composable
fun PaymentAndDeliverySection(
    dealTypes: List<DealType>?,
    paymentMethods: List<PaymentMethod>?,
    deliveryMethods: List<DeliveryMethod>?
) {
    TitleSeparatorOffer(strings.paymentAndDeliveryLabel)
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
fun TitleSeparatorOffer(
    title: StringResource
){
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleLarge,
        color = colors.black,
        modifier = Modifier.padding(horizontal = dimens.mediumPadding, vertical = dimens.smallPadding)
    )
}

@Composable
fun ParametersSection(
    parameters: List<Param>?
) {
    if (!parameters.isNullOrEmpty()) {
        TitleSeparatorOffer(strings.parametersLabel)

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
    onRebidClick: (id: Long) -> Unit
) {
   val bids =  offer.bids
    if (bids != null) {
        TitleSeparatorOffer(strings.bidsLabel)

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
                        if (i == 0 || bid.moverId == UserData.login) {
                            SimpleTextButton(
                                text = if (bid.moverId == UserData.login)
                                    stringResource(strings.yourBidLabel)
                                else stringResource(strings.rebidLabel),
                                textColor = colors.black,
                                textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                backgroundColor = if (bid.moverId == UserData.login)
                                    colors.grayText
                                else colors.notifyTextColor,
                                modifier = Modifier.heightIn(max = 35.dp),
                            ) {
                                onRebidClick(offer.id)
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
