package market.engine.presentation.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import kotlinx.coroutines.launch
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.types.OfferStates
import market.engine.core.util.getCurrentDate
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.PopupActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
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
    val images = offer.value.images?.map { it.urls?.big?.content.orEmpty() } ?: emptyList()
    val pagerState = rememberPagerState(
        pageCount = { images.size},
    )
    val descriptionsDecodeHtmlString = remember { mutableStateOf("") }
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
                            repeat(catHistory.value.size) { index ->
                                Text(
                                    text = if (catHistory.value.size-1 == index)
                                        catHistory.value[index].name ?: ""
                                    else (catHistory.value[index].name ?: "") + "->",
                                    style = MaterialTheme.typography.bodySmall.
                                        copy(fontWeight = FontWeight.Bold),
                                    color = if (catHistory.value.size-1 == index) colors.black else colors.steelBlue,
                                    modifier = Modifier.padding(dimens.smallPadding).clickable {
                                        if (catHistory.value[index].isLeaf){
                                            //cat.parentId
                                        }else{
                                            //cat.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                    //count and views label
                    item {
                        val countString = getCountString(offerState, offer.value, isMyOffer.value)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (countString.isNotEmpty()) {
                                Text(
                                    text = countString,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.grayText,
                                    modifier = Modifier.padding(dimens.smallPadding)
                                )
                            }

                            Text(
                                text = stringResource(strings.viewsParams) + ": " + offer.value.viewsCount,
                                style = MaterialTheme.typography.bodyLarge.
                                copy(fontWeight = FontWeight.Bold),
                                color = colors.grayText,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )
                        }
                    }
                    //title
                    item {
                        Text(
                            text = offer.value.title ?: "",
                            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                            style = MaterialTheme.typography.titleLarge.
                                copy(fontWeight = FontWeight.Bold, fontSize = dimens.extraLargeText),
                        )
                    }
                    //simple Price
                    if (offerState != OfferStates.ACTIVE || isMyOffer.value) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                            ) {
                                Text(
                                    text = (offer.value.currentPricePerItem ?: "") +
                                            " " + stringResource(strings.currencySign),
                                    style = MaterialTheme.typography.titleLarge.
                                        copy(fontWeight = FontWeight.Bold),
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
                    if ((offer.value.saleType == "buy_now" || offer.value.saleType == "auction_with_buy_now") && !isMyOffer.value && offerState == OfferStates.ACTIVE) {
                        item {
                            BuyNowPriceLayout(
                                offer = offer.value,
                                onBuyNowClick = {

                                },
                                onAddToCartClick = {

                                }
                            )
                        }
                    }
                    //descriptions offer
                    item {
                        descriptionsDecodeHtmlString.value = ""

                        val handler = KsoupHtmlHandler
                            .Builder()
                            .onText { text ->
                                descriptionsDecodeHtmlString.value += text
                            }
                            .onError { error ->
                                descriptionsDecodeHtmlString.value  = "Error: ${error.message}"
                                println("HTML Parse Error: ${error.message}")
                            }
                            .build()

                        val ksoupHtmlParser = KsoupHtmlParser(
                            handler = handler,
                        )

                        TitleText(
                            text = stringResource(strings.description),
                            modifier = Modifier.fillMaxWidth().padding(vertical = dimens.smallPadding)
                        )

                        ksoupHtmlParser.write(offer.value.description ?: "")
                        ksoupHtmlParser.end()
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(colors.white)
                                .padding(dimens.smallPadding)
                        ) {
                            Text(
                                descriptionsDecodeHtmlString.value,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                                color = colors.darkBodyTextColor
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
    onBuyNowClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = (offer.currentPricePerItem?: "") + " "
                            + stringResource(strings.currencySign),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.titleTextColor,
                    fontWeight = FontWeight.Bold
                )

                SimpleTextButton(
                    text = stringResource(strings.buyNow),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    modifier = Modifier
                        .padding(horizontal = dimens.mediumPadding),
                    textColor = colors.alwaysWhite,
                    onClick = onBuyNowClick,
                )

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


