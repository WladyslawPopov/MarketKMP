package market.engine.widgets.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import market.engine.common.openEmail
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.MyOrderItemState
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.parseToOfferItem
import market.engine.widgets.items.offer_Items.OfferPartItem
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dialogs.OrderOperationsDialog
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOrderItem(
    data : MyOrderItemState,
    updateItem: Long?
) {
    val typeDef = DealTypeGroup.BUY
    val maxNotExpandedItems = 2
    val order = data.order

    val orderRepository = data.orderRepository
    val events = orderRepository.events
    val typeGroup = orderRepository.typeGroup

    LaunchedEffect(updateItem) {
        if (updateItem == order.id) {
            orderRepository.updateItem(order)
        }
    }

    val maxItems = remember { mutableStateOf(maxNotExpandedItems) }

    val clickExpand = {
        maxItems.value = if (maxItems.value == maxNotExpandedItems) order.suborders.size else maxNotExpandedItems
    }

    val showMenu = remember { mutableStateOf(false) }

    val idOrderText = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.actionItemColors)) {
                append(stringResource(strings.orderLabel))
            }
            append(" #${order.id}")
        }

    val menuList = orderRepository.menuList.collectAsState()

    AnimatedVisibility(order.owner != 1L, enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier.background(colors.white, MaterialTheme.shapes.medium).fillMaxWidth()
                .padding(
                    dimens.smallPadding
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        )
        {
            //header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Text(
                    idOrderText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.actionTextColor,
                    modifier = Modifier.clickable {
                        orderRepository.copyOrderId()
                    }.fillMaxWidth(0.5f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Text(
                        order.createdTs.toString().convertDateWithMinutes(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )

                    Column {
                        SmallIconButton(
                            drawables.menuIcon,
                            colors.black,
                        ) {
                            showMenu.value = true
                        }

                        PopUpMenu(
                            openPopup = showMenu.value,
                            menuList = menuList.value,
                            onClosed = { showMenu.value = false }
                        )
                    }
                }
            }

            //user and price info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            )
            {
                val textUser = buildAnnotatedString {
                    if (typeGroup == typeDef)
                        append(stringResource(strings.buyerParameterName))
                    else
                        append(stringResource(strings.sellerLabel))

                    append(": ")

                    withStyle(SpanStyle(color = colors.actionTextColor)) {
                        if (typeGroup == typeDef) {
                            append(order.buyerData?.login)
                            append(" (${order.buyerData?.rating})")
                        } else {
                            append(order.sellerData?.login)
                            append(" (${order.sellerData?.rating})")
                        }
                    }
                }
                //login
                Text(
                    textUser,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText,
                    modifier = Modifier.clickable {
                        events.onGoToUser(
                            if (typeGroup == typeDef)
                                order.buyerData?.id ?: 0
                            else order.sellerData?.id ?: 0
                        )
                    }
                )
                // email
                val email = if (typeGroup == typeDef)
                    order.buyerData?.email ?: ""
                else order.sellerData?.email ?: ""

                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.actionTextColor,
                    modifier = Modifier.clickable {
                        openEmail(email)
                    }
                )

                //price
                val textPrice = buildAnnotatedString {
                    append(stringResource(strings.totalCostLabel))
                    append(": ")
                    withStyle(SpanStyle(color = colors.positiveGreen)) {
                        append(order.total)
                        append(" ")
                        append(stringResource(strings.currencyCode))
                    }
                }
                Text(
                    textPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText,
                    modifier = Modifier.align(Alignment.End)
                )
                //commissions
                if (order.successFee != 0f) {
                    val textCommissions = buildAnnotatedString {
                        append(stringResource(strings.feeOrderLabel))
                        append(": ")
                        withStyle(SpanStyle(color = colors.priceTextColor)) {
                            append(order.successFee.toString())
                            append(" ")
                            append(stringResource(strings.currencyCode))
                        }
                        if (order.refundWasMade == true) {
                            append("\n")
                            withStyle(SpanStyle(color = colors.priceTextColor)) {
                                append(stringResource(strings.feeWasRefund))
                            }
                        }
                    }
                    Text(
                        textCommissions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.grayText,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            //offers parts
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
            )
            {
                Text(
                    stringResource(strings.offersParticipantsLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText,
                )

                order.suborders.forEachIndexed { index, offer ->
                    if (index < maxItems.value) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DynamicLabel(
                                "${index + 1})",
                                false,
                                style = MaterialTheme.typography.titleSmall,
                            )

                            OfferPartItem(
                                offer = offer.parseToOfferItem(),
                                modifier = Modifier.weight(1f)
                            ) {
                                events.onGoToOffer(offer)
                            }
                        }
                    }
                }

                if (order.suborders.size > maxNotExpandedItems) {
                    Row(
                        modifier = Modifier.background(
                            colors.primaryColor.copy(alpha = 0.5f),
                            MaterialTheme.shapes.small
                        )
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                clickExpand()
                            }.fillMaxWidth()
                            .padding(dimens.smallSpacer),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallIconButton(
                            if (maxItems.value == maxNotExpandedItems) drawables.iconArrowDown else drawables.iconArrowUp,
                            color = colors.inactiveBottomNavIconColor,
                            modifierIconSize = Modifier.size(dimens.mediumIconSize),
                        ) {
                            clickExpand()
                        }
                    }
                }
            }

            //feedbacks
            val myFeedback = if (typeGroup != typeDef) {
                order.feedbacks?.b2s
            } else {
                order.feedbacks?.s2b
            }
            val feedbackToMe = if (typeGroup != typeDef) {
                order.feedbacks?.s2b
            } else {
                order.feedbacks?.b2s
            }

            if (myFeedback != null || feedbackToMe != null) {
                val myFeedbackLabel = stringResource(strings.myFeedbacksLabel)
                val toMeFeedbackLabel = stringResource(strings.toMeFeedbacksLabel)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (myFeedback != null) {
                            SimpleTextButton(
                                myFeedbackLabel,
                                trailingIcon = {
                                    Icon(
                                        painterResource(
                                            when (myFeedback.type) {
                                                "positive" -> {
                                                    drawables.likeIcon
                                                }

                                                "neutral" -> {
                                                    drawables.likeIcon
                                                }

                                                "negative" -> {
                                                    drawables.dislikeIcon
                                                }

                                                else -> {
                                                    drawables.likeIcon
                                                }
                                            }
                                        ),
                                        "",
                                        tint = when (myFeedback.type) {
                                            "positive" -> {
                                                colors.positiveGreen
                                            }

                                            "neutral" -> {
                                                colors.grayText
                                            }

                                            "negative" -> {
                                                colors.negativeRed
                                            }

                                            else -> {
                                                colors.positiveGreen
                                            }
                                        },
                                        modifier = Modifier.size(dimens.smallIconSize)
                                    )
                                },
                            ) {
                                orderRepository.showReportDialog(myFeedbackLabel)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (feedbackToMe != null) {
                            SimpleTextButton(
                                toMeFeedbackLabel,
                                trailingIcon = {
                                    Icon(
                                        painterResource(
                                            when (feedbackToMe.type) {
                                                "positive" -> {
                                                    drawables.likeIcon
                                                }

                                                "neutral" -> {
                                                    drawables.likeIcon
                                                }

                                                "negative" -> {
                                                    drawables.dislikeIcon
                                                }

                                                else -> {
                                                    drawables.likeIcon
                                                }
                                            }
                                        ),
                                        "",
                                        tint = when (feedbackToMe.type) {
                                            "positive" -> {
                                                colors.positiveGreen
                                            }

                                            "neutral" -> {
                                                colors.grayText
                                            }

                                            "negative" -> {
                                                colors.negativeRed
                                            }

                                            else -> {
                                                colors.positiveGreen
                                            }
                                        },
                                        modifier = Modifier.size(dimens.smallIconSize)
                                    )
                                },
                            ) {
                                orderRepository.showReportDialog(toMeFeedbackLabel)
                            }
                        }
                    }
                }
            }

            //tags
            if (order.marks?.isNotEmpty() == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(drawables.tagIcon),
                        contentDescription = "",
                        tint = colors.textA0AE,
                        modifier = Modifier.size(dimens.smallIconSize)
                    )

                    Text(
                        stringResource(strings.tagsLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black,
                    )

                    val builder = buildAnnotatedString {
                        order.marks?.forEach {
                            append(it.title)
                            if (order.marks?.indexOf(it) != order.marks?.lastIndex)
                                append(", ")
                        }
                    }

                    Text(
                        builder.text.dropLast(1),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.titleTextColor,
                    )
                }
            }

            //track id
            if (order.trackId?.isNotBlank() == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(drawables.trackIcon),
                        contentDescription = "",
                        tint = colors.textA0AE,
                        modifier = Modifier.size(dimens.smallIconSize)
                    )

                    Text(
                        stringResource(strings.trackIdLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black,
                    )

                    Text(
                        order.trackId ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.actionTextColor,
                        modifier = Modifier.clickable {
                            orderRepository.copyTrackId()
                        }
                    )
                }
            }
            if (order.comment?.isNotBlank() == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(strings.dialogComment),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black
                    )
                    Text(
                        text = order.comment.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.grayText
                    )
                }
            }

            // letter and delivery btn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                val mes = if (typeGroup == typeDef) {
                    stringResource(strings.writeBuyerLabel)
                } else {
                    stringResource(strings.writeSellerLabel)
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimpleTextButton(
                        mes,
                        leadIcon = {
                            Icon(
                                painterResource(drawables.mail),
                                contentDescription = "",
                                tint = colors.alwaysWhite,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                        },
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontSize = dimens.mediumText
                        ),
                        backgroundColor = colors.steelBlue,
                        textColor = colors.alwaysWhite
                    ) {
                        orderRepository.sendMessage()
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimpleTextButton(
                        stringResource(strings.infoDeliveryLabel),
                        leadIcon = {
                            Icon(
                                painterResource(drawables.trackIcon),
                                contentDescription = "",
                                tint = colors.alwaysWhite,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                        },
                        backgroundColor = colors.steelBlue,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontSize = dimens.mediumText
                        ),
                        textColor = colors.alwaysWhite
                    ) {
                        orderRepository.openOrderDetails()
                    }
                }
            }

            OrderOperationsDialog(
                orderRepository
            )
        }
    }
}
