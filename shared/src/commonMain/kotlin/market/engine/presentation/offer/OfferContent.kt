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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import application.market.agora.business.core.network.functions.UserOperations
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.network.networkObjects.Offer
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
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
    val offer = offerViewModel.responseOffer.collectAsState()
    val isLoading = offerViewModel.isShowProgress.collectAsState()

    val isImageViewerVisible = remember { mutableStateOf(false) }

    val scrollState = rememberCoroutineScope()

    val images = when {
        offer.value.images?.isNotEmpty() == true -> offer.value.images?.map { it.urls?.big?.content.orEmpty() } ?: emptyList()
        offer.value.externalImages?.isNotEmpty() == true -> offer.value.externalImages?: emptyList()
        else -> emptyList()
    }
    val pagerState = rememberPagerState(
        pageCount = { images.size},
    )
    val descriptionsDecodeHtmlString : MutableState<AnnotatedString> = remember { mutableStateOf(
            buildAnnotatedString {

            }
        )
    }
    val visitedHistory = offerViewModel.responseHistory.collectAsState()
    val ourChoiceList = offerViewModel.responseOurChoice.collectAsState()
    val catHistory = offerViewModel.responseCatHistory.collectAsState()


    val isShowOptions = remember { mutableStateOf(false) }
    val myMaximalBid = remember { mutableStateOf("") }

    val isMyOffer = remember { mutableStateOf(false) }
    var offerState by remember {
        mutableStateOf(OfferStates.ACTIVE)
    }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(offer.value) {
        offerState = when {
            model.value.isSnapshot -> OfferStates.SNAPSHOT
            offer.value.isPrototype -> OfferStates.PROTOTYPE
            offer.value.state == "active" -> {
                when {
                    offer.value.session == null -> OfferStates.COMPLETED
                    (offer.value.session?.start?.toLongOrNull() ?: 1L) > getCurrentDate().toLong() -> OfferStates.FUTURE
                    (offer.value.session?.end?.toLongOrNull() ?: 1L) - getCurrentDate().toLong() > 0 -> OfferStates.ACTIVE
                    else -> OfferStates.INACTIVE
                }
            }
            offer.value.state == "sleeping" -> {
                if (offer.value.session == null) OfferStates.COMPLETED else OfferStates.INACTIVE
            }
            else -> offerState
        }

        isMyOffer.value = UserData.userInfo?.login == offer.value.sellerData?.login

        mainViewModel.sendEvent(UIMainEvent.UpdateTopBar{
            if (!isImageViewerVisible.value) {
                OfferAppBar(
                    offer.value,
                    isFavorite = offer.value.isWatchedByMe,
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

    DisposableEffect(Unit){
        onDispose {
            offerViewModel.addHistory(model.value.id)
        }
    }

    Box(modifier.fillMaxSize()){
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
                            if (offer.value.videoUrls?.isNotEmpty() == true) {
                                SmallImageButton(
                                    drawables.iconYouTubeSmall,
                                    modifierIconSize = Modifier.size(dimens.largeIconSize),
                                    modifier = Modifier.align(Alignment.TopEnd),
                                ){
                                    //open web view youtube
                                }
                            }

                            HorizontalImageViewer(
                                images = images,
                                pagerState = pagerState,
                            )
                        }
                    }
                    if (offer.value.hasTempImages) {
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
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (catHistory.value.size - 1 == index) colors.black else colors.steelBlue,
                                        modifier = Modifier.padding(dimens.smallPadding).clickable {
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
                        val countString = getCountString(offerState, offer.value, isMyOffer.value)

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
                                text = stringResource(strings.viewsParams) + ": " + offer.value.viewsCount,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.steelBlue,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )
                        }
                    }
                    //title
                    item {
                        Text(
                            text = offer.value.title ?: "",
                            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = dimens.extraLargeText
                            ),
                        )
                    }
                    //simple Price
                    if (offerState == OfferStates.INACTIVE || offerState == OfferStates.COMPLETED || isMyOffer.value) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                            ) {
                                Text(
                                    text = (offer.value.currentPricePerItem ?: "") +
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
                                offer.value,
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
                                        offer.value,
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
                    if (offer.value.saleType != "buy_now" && !isMyOffer.value && offerState == OfferStates.ACTIVE) {
                        item {
                            AuctionPriceLayout(
                                offer = offer.value,
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
                    if ((offer.value.saleType == "buy_now" || offer.value.saleType == "auction_with_buy_now") && !isMyOffer.value) {
                        item {
                            BuyNowPriceLayout(
                                offer = offer.value,
                                offerState,
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
                                if (offer.value.sellerData != null && !isMyOffer.value && offerState == OfferStates.ACTIVE) {
                                    MessageToSeller(offer.value)
                                }

                                //make proposal to seller
                                if (offer.value.isProposalEnabled){
                                    ProposalToSeller(
                                        if(UserData.login == offer.value.sellerData?.id) "act_on_proposal" else "make_proposal",
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
                                    ){

                                    }

                                    Text(
                                        text = stringResource(strings.whoPayForDeliveryLabel),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.greenColor
                                    )

                                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                                    Text(
                                        text = offer.value.whoPaysForDelivery?.name ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.black,
                                        fontStyle = FontStyle.Italic
                                    )
                                }


                                if(offer.value.antisniper) {
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
                                if (offerState == OfferStates.ACTIVE && offer.value.saleType != "buy_now") {
                                    BidsWinnerOrLastBid(offer.value)
                                }

                                TimeOfferSession(
                                    offer.value,
                                    offerViewModel.viewModelScope,
                                    offerState,
                                ){
                                    component.updateOffer(offer.value.id)
                                }

                                LocationOffer(offer.value){
                                    //go to Listing
                                }
                            }
                        }
                    }
                    // seller panel
                    item {
                        SellerPanel(
                            offer.value,
                            goToSeller = {

                            },
                            goToAllLots = {

                            },
                            goToAboutMe = {

                            },
                            addToSubscriptions = {

                            },
                            checkBlackList = ::checkStatusSeller
                        )
                    }
                    //descriptions offer
                    item {
                        TitleText(
                            text = stringResource(strings.description),
                            modifier = Modifier.fillMaxWidth().padding(vertical = dimens.smallPadding)
                        )

                        val descriptionHtml = offer.value.description ?: ""
                        descriptionsDecodeHtmlString.value = descriptionHtml.parseHtmlToAnnotatedString()

                        offer.value.addedDescriptions?.forEach { added ->
                            val formattedDate = added.timestamp.toString().convertDateWithMinutes()
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

                        offer.value.standardDescriptions?.forEach { standard ->
                            val formattedDate = standard.timestamp.toString().convertDateWithMinutes()
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
                    //recommended list offers
                    if (ourChoiceList.value.isNotEmpty()) {
                        item {
                            TitleText(
                                text = stringResource(strings.ourChoice),
                                modifier = Modifier.fillMaxWidth()
                            )
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
                    if (visitedHistory.value.isNotEmpty()) {
                        item {
                            TitleText(
                                text = stringResource(strings.lastViewedOffers),
                                modifier = Modifier.fillMaxWidth()
                            )

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
    offer: Offer
){
    val bids = offer.bids

    val sourceString: AnnotatedString = if (!bids.isNullOrEmpty()) {
        val bidsCount = bids.size
        // Get the leader's login, preferring obfuscatedMoverLogin if available
        val leadingBidder = bids[0].obfuscatedMoverLogin ?: bids[0].moverLogin ?: ""

        // Annotated string to format the text
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = colors.actionTextColor)) {
                append("${stringResource(strings.bidsMadeLabel)} : ")
            }
            append("$bidsCount \n")

            append("${stringResource(strings.leadingBidsNowLabel)} : ")

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.titleTextColor)) {
                append(leadingBidder)
            }
        }
    } else {
        // No bids yet message
        buildAnnotatedString {
            append("${stringResource(strings.noBids)}, ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.titleTextColor)) {
                append(stringResource(strings.yourBidsFirstLabel))
            }
        }
    }

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
            drawables.iconGroup
        ){

        }

        Text(
            text = sourceString,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
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

@Composable
fun SellerPanel(
    offer: Offer,
    goToSeller: () -> Unit,
    goToAllLots: () -> Unit,
    goToAboutMe: () -> Unit,
    addToSubscriptions: () -> Unit,
    goToSettings: (() -> Unit)? = null,
    checkBlackList: suspend (Long, String, UserOperations) -> String? // Suspend function to check black/white lists
) {
    val seller = offer.sellerData
    val statusCode : MutableState<String?> = remember { mutableStateOf(null) }
    val userOperations = koinInject<UserOperations>()
    // Check if the seller is in the black/white list
    LaunchedEffect(seller?.id) {
        seller?.let {
            statusCode.value = checkBlackList(it.id, "blacklist_sellers", userOperations)
        }
    }

    if (seller != null) {
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
                    .padding(horizontal = dimens.smallPadding),
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

                Row(
                    modifier = Modifier.weight(1f).padding(dimens.smallPadding).clickable {
                        goToSeller()
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val image = seller.avatar?.thumb?.content
                    if (image != null) {
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

                    Spacer(modifier = Modifier.width(dimens.smallPadding))

                    Text(
                        text = seller.login ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.brightBlue),
                    )

                    Spacer(modifier = Modifier.width(dimens.smallPadding))

                    if (seller.rating > 0) {
                        Box(
                            modifier = Modifier
                                .background(colors.ratingBlue, shape = MaterialTheme.shapes.medium)
                                .padding(horizontal = dimens.smallPadding, vertical = dimens.extraSmallPadding)
                        ) {
                            Text(
                                text = seller.rating.toString(),
                                color = colors.alwaysWhite,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
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
            statusCode.value?.let { status ->
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
    scope: CoroutineScope,
    state: OfferStates,
    onTimerFinish: (() -> Unit)? = null // Optional callback when timer finishes
) {
    // AnnotatedString to track the styled remaining time
    val remainingTime = remember { mutableStateOf(AnnotatedString("")) }

    // Preload string resources
    val inactiveLabel = stringResource(strings.offerSessionInactiveLabel)
    val completedLabel = stringResource(strings.offerSessionCompletedLabel)
    val futureLabel = stringResource(strings.offerSessionFutureLabel)
    val daysLabel = stringResource(strings.daysLabel)
    val hoursLabel = stringResource(strings.hoursLabel)
    val minutesLabel = stringResource(strings.minutesLabel)
    val secondsLabel = stringResource(strings.secondsLabel)
    val beforeGraduationLabel = stringResource(strings.beforeGraduationLabel)

    val endTime = offer.session?.end?.toLongOrNull()

    // Handle different states
    LaunchedEffect(state, endTime) {
        when (state) {
            OfferStates.ACTIVE -> {
                if (endTime != null) {
                    startCountdownTimer(
                        scope = scope,
                        endTime = endTime,
                        onTick = { millisUntilFinished ->
                            remainingTime.value = formatRemainingTimeAnnotated(
                                millisUntilFinished,
                                beforeGraduationLabel,
                                daysLabel,
                                hoursLabel,
                                minutesLabel,
                                secondsLabel
                            )
                        },
                        onFinish = {
                            remainingTime.value = buildAnnotatedString {
                                append(inactiveLabel)
                            }
                            onTimerFinish?.invoke()
                        }
                    )
                } else {
                    remainingTime.value = buildAnnotatedString {
                        append(completedLabel)
                    }
                }
            }

            OfferStates.INACTIVE -> {
                remainingTime.value = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(inactiveLabel)
                    }
                }
            }

            OfferStates.COMPLETED -> {
                remainingTime.value = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(completedLabel)
                    }
                }
            }

            OfferStates.FUTURE -> {
                remainingTime.value = buildAnnotatedString {
                    append(futureLabel)
                }
            }

            else -> {
                remainingTime.value = AnnotatedString("")
            }
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
                append(remainingTime.value)
                if (offer.session?.end != null) {
                    append(" \n (${offer.session.end.convertDateWithMinutes()})")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
    }
}

fun startCountdownTimer(
    endTime: Long,
    scope: CoroutineScope,
    onTick: (Long) -> Unit,
    onFinish: () -> Unit
) {
    val intervalMillis = 1000L // 1 second interval
    scope.launch(Dispatchers.IO) {
        while (true) {
            val currentTime = getCurrentDate().toLong() // Ensure the timestamp is correctly updated
            val remainingMillis = (endTime - currentTime) * 1000
            if (remainingMillis <= 0) {
                withContext(Dispatchers.Main) {
                    onFinish()
                }
                break
            }
            withContext(Dispatchers.Main) {
                onTick(remainingMillis) // Update remaining time on the main thread
            }
            delay(intervalMillis)
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
        }
    }
}

suspend fun checkStatusSeller(
    id: Long,
    list: String,
    userOperations: UserOperations
): String? {
    val body = hashMapOf("list_type" to list)

    val buf = withContext(Dispatchers.IO) {
        userOperations.getUsersOperationsGetUserList(UserData.login, body)
    }

    val success = buf.success ?: return null
    val data = success.body?.data ?: return null

    val found = data.find { it.id == id }
    return when {
        found != null -> list
        list == "blacklist_sellers" -> checkStatusSeller(id, "blacklist_buyers", userOperations)
        list == "blacklist_buyers" -> checkStatusSeller(id, "whitelist_buyers", userOperations)
        else -> null
    }
}
