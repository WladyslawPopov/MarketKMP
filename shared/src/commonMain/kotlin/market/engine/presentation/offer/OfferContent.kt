package market.engine.presentation.offer

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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import market.engine.widgets.exceptions.getOfferOperations
import market.engine.widgets.items.PromoOfferRowItem
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.texts.DiscountText
import market.engine.widgets.texts.TitleText
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

                                }
                            }
                        }
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
fun TimeOfferSession(
    offer: Offer,
    scope: CoroutineScope,
    state: OfferStates,
    onTimerFinish: (() -> Unit)? = null // Optional callback when timer finishes
) {
    // AnnotatedString to track the styled remaining time
    var remainingTime by remember { mutableStateOf(AnnotatedString("")) }

    // Preload string resources
    val inactiveLabel = stringResource(strings.offerSessionInactiveLabel)
    val completedLabel = stringResource(strings.offerSessionCompletedLabel)
    val futureLabel = stringResource(strings.offerSessionFutureLabel)
    val daysLabel = stringResource(strings.daysLabel)
    val hoursLabel = stringResource(strings.hoursLabel)
    val minutesLabel = stringResource(strings.minutesLabel)
    val secondsLabel = stringResource(strings.secondsLabel)

    val endTime = offer.session?.end?.toLongOrNull()

    // Handle different states
    LaunchedEffect(state, endTime) {
        when (state) {
            OfferStates.ACTIVE -> {
                if (endTime != null) {
                    startKmpCountdownTimer(
                        scope = scope,
                        endTime = endTime,
                        onTick = { millisUntilFinished ->
                            remainingTime = formatRemainingTimeAnnotated(
                                millisUntilFinished,
                                daysLabel,
                                hoursLabel,
                                minutesLabel,
                                secondsLabel
                            )
                        },
                        onFinish = {
                            remainingTime = buildAnnotatedString {
                                append(inactiveLabel)
                            }
                            onTimerFinish?.invoke()
                        }
                    )
                } else {
                    remainingTime = buildAnnotatedString {
                        append(completedLabel)
                    }
                }
            }

            OfferStates.INACTIVE -> {
                remainingTime = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(inactiveLabel)
                    }
                }
            }

            OfferStates.COMPLETED -> {
                remainingTime = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(completedLabel)
                    }
                }
            }

            OfferStates.FUTURE -> {
                remainingTime = buildAnnotatedString {
                    append(futureLabel)
                }
            }

            else -> {
                remainingTime = AnnotatedString("")
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SmallImageButton(
            drawables.iconClock
        ) {
        }

        // Display the styled AnnotatedString
        Text(
            text = remainingTime,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.black
        )
    }
}


fun startKmpCountdownTimer(
    endTime: Long,
    scope: CoroutineScope,
    onTick: (Long) -> Unit,
    onFinish: () -> Unit
) {
    val intervalMillis = 1000L
    scope.launch {
        while (true) {
            val remainingMillis = endTime - getCurrentDate().toLong()
            if (remainingMillis <= 0) {
                onFinish()
                break
            }
            onTick(remainingMillis)
            delay(intervalMillis)
        }
    }
}


fun formatRemainingTimeAnnotated(
    millisUntilFinished: Long,
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
            append(" ")
        }
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
