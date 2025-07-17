package market.engine.widgets.items.offer_Items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.MenuItem
import market.engine.core.data.states.CabinetOfferItemState
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.buttons.OfferActionsBtn
import market.engine.widgets.buttons.PromoBuyBtn
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.ilustrations.HorizontalImageViewer
import market.engine.widgets.rows.PromoRow
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CabinetOfferItem(
    state : CabinetOfferItemState,
    updateItem : Long? = null,
    selected : Boolean = false,
    onSelected : ((Long) -> Unit)? = null
) {
    val item = state.item
    val offerRepository = state.offerRepository

    val events = offerRepository.events
    val defOptions = remember { mutableStateOf<List<MenuItem>>(emptyList()) }

    LaunchedEffect(offerRepository) {
        defOptions.value = offerRepository.getDefOperations()
    }

    val pagerState = rememberPagerState(
        pageCount = { item.images.size },
    )

    val openMenu = remember { mutableStateOf(false) }

    val openPromoMenu = remember { mutableStateOf(false) }

    val menuList = offerRepository.operationsList.collectAsState()
    val menuPromotionsList = offerRepository.promoList.collectAsState()

    LaunchedEffect(updateItem) {
        if (updateItem == item.id) {
            offerRepository.update()
        }
    }

    AnimatedVisibility(!events.isHideCabinetOffer(), enter = fadeIn(), exit = fadeOut()) {
        Card(
            colors = if (!item.isPromo) colors.cardColors else colors.cardColorsPromo,
            shape = MaterialTheme.shapes.small,
            onClick = {
                events.openCabinetOffer()
            }
        ) {
            HeaderOfferBar(
                offer = item,
                defOptions = defOptions.value,
                selected = selected,
                onSelected = onSelected,
                updateItem = updateItem
            )

            Row(
                modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                val imageSize =
                    if (isBigScreen.value) {
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
                        if (item.images.isNotEmpty()) {
                            HorizontalImageViewer(
                                images = item.images,
                                pagerState = pagerState,
                            )
                        } else {
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
                            ) {

                            }
                        }

                        if (item.discount > 0) {
                            val pd = "-" + item.discount.toString() + "%"

                            DiscountBadge(pd)
                        }
                    }

                    Column {
                        OfferActionsBtn(
                            onClick = {
                                openMenu.value = true
                            }
                        )

                        PopUpMenu(
                            openPopup = openMenu.value,
                            menuList = menuList.value,
                            onClosed = {
                                openMenu.value = false
                            }
                        )
                    }
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
                        val sessionEnd = stringResource(strings.offerSessionInactiveLabel)

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
                                        text = item.currentQuantity.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                    )

                                    var buyer = item.buyer?.login ?: ""
                                    var color = colors.grayText

                                    if (!item.isPrototype) {
                                        if (item.currentQuantity < 2) {
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
                                PromoRow(it, false) {

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

                        Column {
                            if (UserData.login == item.seller.id && item.state == "active") {
                                PromoBuyBtn {
                                    openPromoMenu.value = true
                                }
                            }

                            PopUpMenu(
                                openPopup = openPromoMenu.value,
                                menuList = menuPromotionsList.value,
                                onClosed = { openPromoMenu.value = false }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.smallPadding,
                            Alignment.End
                        ),
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

            if (item.relistingMode != null && UserData.login == item.seller.id) {
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
        }
    }

    OfferOperationsDialogs(
        offerRepository = offerRepository,
    )
}
