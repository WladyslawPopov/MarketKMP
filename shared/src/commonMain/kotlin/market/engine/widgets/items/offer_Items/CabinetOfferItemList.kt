package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.onClickOfferOperationItem
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.buttons.PromoBuyBtn
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.ilustrations.HorizontalImageViewer
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CabinetOfferItemList(
    item : OfferItem,
    baseViewModel: BaseViewModel,
    onItemClick: () -> Unit = {},
    isSelection : Boolean = false,
    updateTrigger : Int = 0,
    goToProposal : (ProposalType) -> Unit= { _ -> },
    onUpdateOfferItem : ((Long) -> Unit)? = null,
    refreshPage : (() -> Unit)? = null,
    onSelectionChange: ((Boolean) -> Unit)? = null,
    goToCreateOffer : (CreateOfferType) -> Unit = { _ -> },
    goToDynamicSettings : (String, Long?) -> Unit = { _, _ -> },
) {
    if (updateTrigger < 0) return

    val pagerState = rememberPagerState(
        pageCount = { item.images.size },
    )

    val isOpenPopup = remember { mutableStateOf(false) }
    val isOpenPromoPopup = remember { mutableStateOf(false) }

    val showOperationsDialog = remember { mutableStateOf("") }
    val title = remember { mutableStateOf(AnnotatedString("")) }
    val fields = remember { mutableStateOf< ArrayList<Fields>>(arrayListOf()) }

    val menuList = remember {
        mutableStateOf<List<MenuItem>>(emptyList())
    }

    Card(
        colors = if (!item.isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = MaterialTheme.shapes.small,
        onClick = {
            onItemClick()
        }
    ) {
        if (onUpdateOfferItem != null) {
            HeaderOfferBar(
                offer = item,
                isSelected = isSelection,
                onUpdateTrigger = updateTrigger,
                baseViewModel = baseViewModel,
                onSelectionChange = onSelectionChange,
                onUpdateOfferItem = onUpdateOfferItem,
                refreshPage = refreshPage
            )
        }

        Row(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            val imageSize =
                if (isBigScreen.value){
                    250.dp
                } else {
                    165.dp
                }


            Column(
                modifier = Modifier.width(imageSize).padding(dimens.smallPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Box(
                    modifier = Modifier.size(imageSize),
                ) {
                    if (item.images.isNotEmpty()){
                        HorizontalImageViewer(
                            images = item.images,
                            pagerState = pagerState,
                        )
                    }else{
                        Image(
                            painter = painterResource(drawables.noImageOffer),
                            contentDescription = null,
                            modifier = Modifier.size(imageSize)
                        )
                    }

                    if (item.videoUrls?.isNotEmpty() == true) {
                        SmallImageButton(
                            drawables.iconYouTubeSmall,
                            modifierIconSize = Modifier.size(dimens.mediumIconSize),
                            modifier = Modifier.align(Alignment.TopStart),
                        ){

                        }
                    }

                    if (item.discount > 0) {
                        val pd = "-" + item.discount.toString() + "%"

                        DiscountBadge(pd)
                    }
                }

                SimpleTextButton(
                    text = stringResource(strings.actionsLabel),
                    textStyle = MaterialTheme.typography.labelSmall,
                    textColor = colors.actionTextColor,
                    backgroundColor = colors.grayLayout,
                    leadIcon = {
                        Icon(
                            painter = painterResource(drawables.shareMenuIcon),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.extraSmallIconSize),
                            tint = colors.actionTextColor
                        )
                    },
                ) {
                    baseViewModel.getOfferOperations(item.id) { listOperations ->
                        menuList.value = buildList {
                            addAll(listOperations.map { operation ->
                                MenuItem(
                                    id = operation.id ?: "",
                                    title = operation.name ?: "",
                                    onClick = {
                                        operation.onClickOfferOperationItem(
                                            item,
                                            baseViewModel,
                                            title,
                                            fields,
                                            showOperationsDialog,
                                            onUpdateOfferItem,
                                            goToProposal,
                                            goToCreateOffer,
                                            goToDynamicSettings,
                                        )
                                    }
                                )
                            })
                        }
                        isOpenPopup.value = true
                    }
                }

                PopUpMenu(
                    openPopup = isOpenPopup.value,
                    menuList = menuList.value,
                    onClosed = { isOpenPopup.value = false }
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    verticalAlignment = Alignment.Top
                ) {
                    TitleText(item.title, modifier = Modifier.weight(1f))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(drawables.locationIcon),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.extraSmallIconSize),
                    )
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                if (!item.isPrototype) {
                    var sessionEnd = stringResource(strings.offerSessionInactiveLabel)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(drawables.iconClock),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.extraSmallIconSize),
                        )

                        Text(
                            text = buildString {
                                if (item.session != null)
                                    append((item.session?.end ?: "").convertDateWithMinutes())
                                else
                                    append(sessionEnd)
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var typeString = ""
                    var colorType = colors.titleTextColor

                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        when (item.type) {
                            "buy_now" -> {
                                typeString = stringResource(strings.buyNow)
                                colorType = colors.buyNowColor

                                Image(
                                    painter = painterResource(drawables.iconCountBoxes),
                                    contentDescription = stringResource(strings.numberOfItems),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.quantity.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var buyer = item.buyer?.login ?: ""
                                var color = colors.grayText

                                if (!item.isPrototype) {
                                    if (item.quantity < 2) {
                                        if (item.buyer?.login != "" && item.buyer?.login != null) {
                                            buyer = item.buyer?.login ?: ""
                                            color = colors.ratingBlue
                                        }
                                    }

                                    Text(
                                        text = buyer,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color
                                    )
                                }
                            }

                            "ordinary_auction" -> {
                                typeString = stringResource(strings.ordinaryAuction)

                                Image(
                                    painter = painterResource(drawables.iconGroup),
                                    contentDescription = stringResource(strings.numberOfBids),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText

                                if (item.bids?.isNotEmpty() == true) {
                                    bids = item.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }

                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }

                            "auction_with_buy_now" -> {
                                typeString = stringResource(strings.blitzAuction)
                                colorType = colors.auctionWithBuyNow

                                Image(
                                    painter = painterResource(drawables.iconGroup),
                                    contentDescription = stringResource(strings.numberOfBids),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText
                                if (item.bids?.isNotEmpty() == true) {
                                    bids = item.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }
                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }

                        if (item.safeDeal) {
                            Image(
                                painter = painterResource(drawables.safeDealIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                        }
                    }

                    if (item.seller.id == UserData.login) {
                        item.promoOptions?.let {
                            PromoRow(it, false){

                            }
                        }
                    }

                    if (UserData.login != item.seller.id) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Text(
                                text = item.seller.login ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.brightBlue,
                            )

                            if ((item.seller.rating ?: 0) > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            colors.ratingBlue,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(dimens.extraSmallPadding)
                                ) {
                                    Text(
                                        text = item.seller.rating.toString(),
                                        color = colors.alwaysWhite,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            if (item.seller.isVerified) {
                                Image(
                                    painter = painterResource(drawables.verifiedIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.smallIconSize)
                                )
                            }
                        }
                    }

                    if (item.note != null) {
                        Row(
                            modifier = Modifier.background(
                                colors.white,
                                MaterialTheme.shapes.small
                            ).padding(dimens.smallPadding),
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
                                text = item.note ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.black,
                                modifier = Modifier.weight(1f, !isBigScreen.value)
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

                    if (UserData.login == item.seller.id && item.state == "active") {
                        val currency = stringResource(strings.currencySign)
                        PromoBuyBtn {
                            baseViewModel.getOfferOperations(item.id, "promo") { listOperations ->
                                menuList.value = buildList {
                                    addAll(listOperations.map { operation ->
                                        MenuItem(
                                            id = operation.id ?: "",
                                            title = "${(operation.name ?: "")} (${operation.price*-1}$currency)",
                                            onClick = {
                                                baseViewModel.getOperationFields(item.id, operation.id ?: "", "offers"){ t, f ->
                                                    title.value = buildAnnotatedString {
                                                        append(t)
                                                        withStyle(
                                                            SpanStyle(
                                                                color = colors.notifyTextColor,
                                                            )
                                                        ) {
                                                            append(" ${operation.price}$currency")
                                                        }
                                                    }
                                                    fields.value.clear()
                                                    fields.value.addAll(f)
                                                    showOperationsDialog.value = operation.id ?: ""
                                                }
                                            }
                                        )
                                    })
                                }
                                isOpenPromoPopup.value = true
                            }
                        }
                    }

                    PopUpMenu(
                        openPopup = isOpenPromoPopup.value,
                        menuList = menuList.value,
                        onClosed = { isOpenPromoPopup.value = false }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(item.price)
                            append(" ${stringResource(strings.currencySign)}")
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.priceTextColor,
                    )
                }
            }
        }

        if (item.relistingMode != null && UserData.login == item.seller.id && onUpdateOfferItem != null) {
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
                    item.relistingMode?.name ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        OfferOperationsDialogs(
            offer = item,
            showDialog = showOperationsDialog,
            viewModel = baseViewModel,
            title = title,
            fields = fields,
            updateItem = {
                onUpdateOfferItem?.invoke(it)
            }
        )
    }
}
