package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.CreateOfferType
import market.engine.core.utils.convertDateWithMinutes
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.rows.HeaderOfferItem
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.bars.OfferItemStatuses
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.rows.UserSimpleRow
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferItem(
    offer: Offer,
    isGrid : Boolean,
    baseViewModel: BaseViewModel,
    isSelection : Boolean = false,
    onUpdateOfferItem : ((offer: Offer) -> Unit)? = null,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    onFavouriteClick: (suspend (Offer) -> Boolean)? = null,
    goToCreateOffer : (CreateOfferType) -> Unit = { _ -> },
    onItemClick: () -> Unit = {}
) {
    var isPromo = false

    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

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
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        modifier = Modifier
            .clickable {
                if (isPromo){
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
            Row {
                HeaderOfferItem(
                    offer = offer,
                    isSelected = isSelection,
                    onSelectionChange = onSelectionChange,
                    onUpdateOfferItem = onUpdateOfferItem,
                    goToCreateOffer = goToCreateOffer,
                    baseViewModel = baseViewModel
                )
            }
        }

        if (isGrid){
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
                    baseViewModel,
                    onFavouriteClick,
                )
            }
        }else{
            Row(
                modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                contentStructure(
                    offer,
                    isGrid,
                    onUpdateOfferItem != null,
                    baseViewModel,
                    onFavouriteClick,
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
                    tint = colors.inactiveBottomNavIconColor
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
    isShowPromo : Boolean = false,
    baseViewModel: BaseViewModel,
    onFavouriteClick: (suspend (Offer) -> Boolean)? = null,
){
    val imageUrl = when {
        offer.images?.isNotEmpty() == true -> offer.images?.firstOrNull()?.urls?.small?.content
        offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
        else -> null
    }

    Box(
        modifier = Modifier
            .padding(dimens.smallPadding)
            .wrapContentSize(),
        contentAlignment = Alignment.TopStart
    ) {
        LoadImage(
            url = imageUrl ?: "",
            size = if(isGrid) 200.dp else 160.dp
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
            content(offer, baseViewModel, isShowPromo, onFavouriteClick)
        }
    }else{
        content(offer, baseViewModel, isShowPromo, onFavouriteClick)
    }
}


@Composable
fun content(
    offer: Offer,
    baseViewModel : BaseViewModel,
    isShowPromo : Boolean = false,
    onFavouriteClick: (suspend (Offer) -> Boolean)? = null
){
    val isFavorites = remember { mutableStateOf(offer.isWatchedByMe) }

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TitleText(text = offer.title ?: "", modifier = Modifier.weight(if (onFavouriteClick != null) 0.8f else 1f))

        if (onFavouriteClick != null) {
            SmallIconButton(
                icon = if (isFavorites.value) drawables.favoritesIconSelected
                else drawables.favoritesIcon,
                color = colors.inactiveBottomNavIconColor,
                modifierIconSize = Modifier.size(dimens.smallIconSize),
                modifier = Modifier.align(Alignment.Top).weight(0.2f)
            ){
                baseViewModel.viewModelScope.launch {
                    isFavorites.value = onFavouriteClick(offer)
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

    OfferItemStatuses(offer)


    if (offer.sellerData?.id == UserData.login && isShowPromo) {
        PromoRow(offer, false){

        }
    }

    Spacer(modifier = Modifier.height(dimens.smallSpacer))
    Row(
        Modifier.padding(dimens.smallPadding).fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        offer.sellerData?.let {
            UserSimpleRow(
                it,
            )
        }
    }

    Spacer(modifier = Modifier.height(dimens.smallSpacer))

    val priceText = buildAnnotatedString {
        append(offer.currentPricePerItem ?: "")
        append(" ${stringResource(strings.currencySign)}")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = priceText,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.titleTextColor,
        )
    }
}
