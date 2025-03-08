package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.data.types.WindowType
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getOfferImagePreview
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.rows.UserColumn
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferItem(
    offer: Offer,
    isGrid : Boolean,
    baseViewModel: BaseViewModel,
    updateTrigger : Int,
    isSelection : Boolean = false,
    isShowFavorites : Boolean = false,
    goToProposal : (ProposalType) -> Unit= { _ -> },
    onUpdateOfferItem : ((offer: Offer) -> Unit)? = null,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    goToCreateOffer : (CreateOfferType) -> Unit = { _ -> },
    goToDynamicSettings : (String, Long?) -> Unit = { _, _ -> },
    onItemClick: () -> Unit = {}
) {
    var isPromo = false

    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    if (offer.promoOptions != null && offer.sellerData?.id != UserData.login) {
        val isBackLight = offer.promoOptions.find { it.id == "backlignt_in_listing" }
        if (isBackLight != null) {
            isPromo = true
            val eventParameters = mapOf(
                "catalog_category" to offer.catpath.lastOrNull(),
                "lot_category" to if (offer.catpath.isEmpty()) 1 else offer.catpath.firstOrNull(),
                "offer_id" to offer.id,
            )

            analyticsHelper.reportEvent("show_top_lots", eventParameters)
        }
    }

    Card(
        colors = if (!isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = MaterialTheme.shapes.small,
        onClick = {
            if (isPromo) {
                val eventParameters = mapOf(
                    "catalog_category" to offer.catpath.lastOrNull(),
                    "lot_category" to if (offer.catpath.isEmpty()) 1 else offer.catpath.firstOrNull(),
                    "offer_id" to offer.id,
                )
                analyticsHelper.reportEvent("click_top_lots", eventParameters)
            }

            onItemClick()
        }
    ) {
        if (onUpdateOfferItem != null) {
            HeaderOfferBar(
                offer = offer,
                isSelected = isSelection,
                onUpdateTrigger = updateTrigger,
                baseViewModel = baseViewModel,
                onSelectionChange = onSelectionChange,
                onUpdateOfferItem = onUpdateOfferItem,
                goToCreateOffer = goToCreateOffer,
                goToProposals = {
                    goToProposal(it)
                },
                goToDynamicSettings = { type, id ->
                    goToDynamicSettings(type, id)
                }
            )
        }

        if (isGrid) {
            Column(
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                contentStructure(
                    offer,
                    isGrid,
                    onUpdateOfferItem != null,
                    isShowFavorites,
                    baseViewModel,
                    updateTrigger,
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                contentStructure(
                    offer,
                    isGrid,
                    onUpdateOfferItem != null,
                    isShowFavorites,
                    baseViewModel,
                    updateTrigger,
                )
            }
        }

        if (offer.relistingMode != null && UserData.login == offer.sellerData?.id && onUpdateOfferItem != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(drawables.recycleIcon),
                    contentDescription = "",
                    modifier = Modifier.size(dimens.smallIconSize),
                    tint = colors.negativeRed
                )

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                Text(
                    offer.relistingMode?.name ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun contentStructure(
    offer: Offer,
    isGrid : Boolean,
    isShowPromo : Boolean,
    isShowFavourite : Boolean,
    baseViewModel: BaseViewModel,
    updateTrigger : Int,
){
    val imageSize =
        if (getWindowType() == WindowType.Big){
            if (isGrid) 300.dp else 400.dp
        } else {
            if (isGrid) 250.dp else 180.dp
        }

    Box(
        modifier = Modifier.padding(dimens.extraSmallPadding),
    ) {
        LoadImage(
            url = offer.getOfferImagePreview(),
            size = imageSize
        )

        if (offer.videoUrls?.isNotEmpty() == true) {
            SmallImageButton(
                drawables.iconYouTubeSmall,
                modifierIconSize = Modifier.size(dimens.mediumIconSize),
                modifier = Modifier.align(Alignment.TopStart),
            ){

            }
        }

        if (offer.discountPercentage > 0) {
            val pd = "-" + offer.discountPercentage.toString() + "%"

            DiscountBadge(pd)
        }
    }

    if (!isGrid) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content(offer, baseViewModel,updateTrigger,isShowPromo, isShowFavourite)
        }
    }else{
        content(offer, baseViewModel, updateTrigger,isShowPromo, isShowFavourite)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun content(
    offer: Offer,
    baseViewModel : BaseViewModel,
    updateTrigger : Int = 0,
    isShowPromo : Boolean,
    isShowFavourite : Boolean,
){
    if (updateTrigger < 0) return

    Row(
        modifier = Modifier.fillMaxWidth().padding(dimens.extraSmallPadding),
    ) {
        TitleText(text = offer.title ?: "", modifier = Modifier.weight(if (isShowFavourite) 0.8f else 1f))

        if (isShowFavourite) {
            SmallIconButton(
                icon = if (offer.isWatchedByMe) drawables.favoritesIconSelected
                else drawables.favoritesIcon,
                color = colors.inactiveBottomNavIconColor,
                modifierIconSize = Modifier.size(dimens.smallIconSize),
                modifier = Modifier.align(Alignment.Top).weight(0.2f)
            ){
                baseViewModel.addToFavorites(offer){
                    offer.isWatchedByMe = it
                    baseViewModel.updateItemTrigger.value++
                }
            }
        }
    }

    val location = buildString {
        offer.freeLocation?.let { append(it) }
        offer.region?.name?.let {
            if (isNotEmpty()) append(", ")
            append(it)
        }
    }

    if (location.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(dimens.extraSmallPadding)
        ) {
            Image(
                painter = painterResource(drawables.locationIcon),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(dimens.smallPadding)
            )
        }
    }

    if (!offer.isPrototype) {
        var sessionEnd = stringResource(strings.offerSessionInactiveLabel)
        if (offer.session != null) {
            sessionEnd = offer.session?.end?.convertDateWithMinutes() ?: ""
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(dimens.extraSmallPadding)
        ) {
            Image(
                painter = painterResource(drawables.iconClock),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
            )
            Text(
                text = sessionEnd,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(dimens.smallPadding)
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        modifier = Modifier.fillMaxWidth().padding(dimens.extraSmallPadding),
    ) {

        var typeString = ""
        var colorType = colors.titleTextColor

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            modifier = Modifier.fillMaxWidth(),
        ) {
            when (offer.saleType) {
                "buy_now" -> {
                    typeString = stringResource(strings.buyNow)
                    colorType = colors.buyNowColor

                    Image(
                        painter = painterResource(drawables.iconCountBoxes),
                        contentDescription = stringResource(strings.numberOfItems),
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Text(
                        text = offer.currentQuantity.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    var buyer = offer.buyerData?.login ?: ""
                    var color = colors.grayText
                    if (!offer.isPrototype) {
                        if (offer.currentQuantity < 2) {
                            if (offer.buyerData?.login != "" && offer.buyerData?.login != null) {
                                buyer = offer.buyerData?.login ?: ""
                                color = colors.ratingBlue
                            }
                        }
                        Text(
                            text = buyer,
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }

                "ordinary_auction" -> {
                    typeString = stringResource(strings.ordinaryAuction)

                    Image(
                        painter = painterResource(drawables.iconGroup),
                        contentDescription = stringResource(strings.numberOfBids),
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Text(
                        text = offer.numParticipants.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    var bids = stringResource(strings.noBids)
                    var color = colors.grayText
                    if (offer.bids?.isNotEmpty() == true) {
                        bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                        color = colors.ratingBlue
                    }
                    Text(
                        text = bids,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }

                "auction_with_buy_now" -> {
                    typeString = stringResource(strings.blitzAuction)
                    colorType = colors.auctionWithBuyNow

                    Image(
                        painter = painterResource(drawables.iconGroup),
                        contentDescription = stringResource(strings.numberOfBids),
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Text(
                        text = offer.numParticipants.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    var bids = stringResource(strings.noBids)
                    var color = colors.grayText
                    if (offer.bids?.isNotEmpty() == true) {
                        bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                        color = colors.ratingBlue
                    }
                    Text(
                        text = bids,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }

            if (offer.safeDeal) {
                Image(
                    painter = painterResource(drawables.safeDealIcon),
                    contentDescription = "",
                    modifier = Modifier.size(dimens.smallIconSize)
                )
            }
        }

        offer.sellerData?.let {
            if (it.id != UserData.login) {
                UserColumn(
                    it,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            text = typeString,
            style = MaterialTheme.typography.titleSmall,
            color = colorType,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    if (offer.sellerData?.id == UserData.login && isShowPromo) {
        PromoRow(offer, false){

        }
    }

    val priceText = buildAnnotatedString {
        append(offer.currentPricePerItem ?: "")
        append(" ${stringResource(strings.currencySign)}")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = priceText,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.priceTextColor,
        )
    }
}
