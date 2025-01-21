package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import market.engine.common.clipBoardEvent
import market.engine.common.openEmail
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.ToastType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.utils.convertDateWithMinutes
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dialogs.CreateOrderDialog
import market.engine.widgets.dialogs.OrderDetailsDialog
import market.engine.widgets.dialogs.ReportDialog
import market.engine.widgets.dropdown_menu.getOrderOperations
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOrderItem(
    typeGroup: DealTypeGroup,
    order: Order,
    trigger : Int,
    onUpdateItem: () -> Unit,
    goToUser: (Long) -> Unit,
    goToOffer: (Offer) -> Unit,
    goToMessenger: () -> Unit,
    baseViewModel: BaseViewModel,
) {
    val typeDef = DealTypeGroup.BUY
    val maxNotExpandedItems = 2
    val idString = stringResource(strings.idCopied)

    val recomposeTrigger = rememberUpdatedState(trigger)

    val maxItems = remember { mutableStateOf(maxNotExpandedItems) }
    val clickExpand = {
        maxItems.value = if (maxItems.value == maxNotExpandedItems) order.suborders.size else maxNotExpandedItems
    }

    val showMenu = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.background(colors.white, MaterialTheme.shapes.medium).fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ){
        //header
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){

            val text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.titleTextColor)){
                    append(stringResource(strings.orderLabel))
                }
                append(" #${order.id}")
            }
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.actionTextColor,
                modifier = Modifier.clickable {
                    clipBoardEvent(order.id.toString())

                    baseViewModel.showToast(
                        ToastItem(
                            isVisible = true,
                            message = idString,
                            type = ToastType.SUCCESS
                        )
                    )
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

                SmallIconButton(
                    drawables.menuIcon,
                    colors.steelBlue,
                ){
                    showMenu.value = true
                }

                AnimatedVisibility(
                    showMenu.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    getOrderOperations(
                        order,
                        baseViewModel,
                        onUpdateMenuItem = {
                            onUpdateItem()
                        },
                        onClose = {
                            showMenu.value = false
                        }
                    )
                }
            }
        }

        //user and price info
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
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
                    goToUser(
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
                    withStyle(SpanStyle(color = colors.titleTextColor)) {
                        append(order.successFee.toString())
                        append(" ")
                        append(stringResource(strings.currencyCode))
                    }
                    if (order.refundWasMade == true) {
                        append("\n")
                        withStyle(SpanStyle(color = colors.titleTextColor)) {
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
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
        ) {
            Text(
                stringResource(strings.offersParticipantsLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayText,
            )
//            order.suborders = buildList {
//                repeat(10) {
//                    add(order.suborders[0])
//                }
//            }
            order.suborders.forEachIndexed { index, offer ->
                if (index < maxItems.value) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.extraSmallPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DynamicLabel(
                            "${index + 1})",
                            false,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(dimens.smallSpacer))
                        OfferPartItem(
                            offer = offer,
                        ) {
                            goToOffer(offer)
                        }
                    }
                }
            }

            if (order.suborders.size > maxNotExpandedItems) {
                Row(
                    modifier = Modifier.clickable {
                        clickExpand()
                    }.fillMaxWidth(),
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

        if(myFeedback != null || feedbackToMe != null) {
            val myFeedbackLabel = if (typeGroup != typeDef) {
                stringResource(strings.myFeedbacksLabel)
            } else {
                stringResource(strings.toMeFeedbacksLabel)
            }

            val toMeFeedbackLabel = if (typeGroup != typeDef) {
                stringResource(strings.toMeFeedbacksLabel)
            } else {
                stringResource(strings.myFeedbacksLabel)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (myFeedback != null) {
                        val showReportDialog = remember { mutableStateOf(false) }

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
                            modifier = Modifier.padding(dimens.smallPadding)
                        ) {
                            showReportDialog.value = true
                        }

                        ReportDialog(
                            isDialogOpen = showReportDialog.value,
                            order = order,
                            mood = myFeedback.type ?: "",
                            text = myFeedback.comment ?: "",
                            type = myFeedbackLabel,
                            onDismiss = {
                                showReportDialog.value = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (feedbackToMe != null) {
                        val showReportDialog = remember { mutableStateOf(false) }

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
                            modifier = Modifier.padding(dimens.smallPadding)
                        ) {
                            showReportDialog.value = true
                        }

                        ReportDialog(
                            isDialogOpen = showReportDialog.value,
                            order = order,
                            mood = feedbackToMe.type ?: "",
                            text = feedbackToMe.comment ?: "",
                            type = toMeFeedbackLabel,
                            onDismiss = {
                                showReportDialog.value = false
                            }
                        )
                    }
                }
            }
        }

        //tags
        if (order.marks?.isNotEmpty() == true) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
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
                        if(order.marks?.indexOf(it) != order.marks?.lastIndex)
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
                modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
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
                        //copy track id
                        clipBoardEvent(order.trackId.toString())

                        baseViewModel.showToast(
                            ToastItem(
                                isVisible = true,
                                message = idString,
                                type = ToastType.SUCCESS
                            )
                        )
                    }
                )
            }
        }

        // letter and delivery btn
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mes = if (typeGroup == typeDef) {
                stringResource(strings.writeBuyerLabel)
            } else {
                stringResource(strings.writeSellerLabel)
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val showDialog = remember { mutableStateOf(false) }
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
                    textStyle = MaterialTheme.typography.labelSmall,
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite
                ) {
                    showDialog.value = true
                }

                CreateOrderDialog(
                    showDialog.value,
                    order,
                    type = typeGroup,
                    onDismiss = {
                        showDialog.value = false
                    },
                    onSuccess = {
                        //go to messenger
                        showDialog.value = false
                        goToMessenger()
                    },
                    baseViewModel = baseViewModel
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val showDialog = remember { mutableStateOf(false) }
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
                    textStyle = MaterialTheme.typography.bodySmall,
                    textColor = colors.alwaysWhite
                ) {
                    showDialog.value = true
                }

                OrderDetailsDialog(
                    isDialogOpen = showDialog.value,
                    order = order,
                    onDismiss = {
                        showDialog.value = false
                    }
                )
            }
        }
    }
}
