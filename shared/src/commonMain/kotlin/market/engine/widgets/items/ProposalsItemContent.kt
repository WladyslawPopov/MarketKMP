package market.engine.widgets.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.constants.countProposalMax
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Proposals
import market.engine.core.utils.checkValidation
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getRemainingMinutesTime
import market.engine.fragments.root.main.proposalPage.ProposalViewModel
import market.engine.theme.Colors
import market.engine.theme.Strings
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.checkboxs.DynamicRadioButtons
import market.engine.widgets.rows.UserRow
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToLong

@Composable
fun ProposalsItemContent(
    offer: Offer,
    proposals: Proposals,
    type: ProposalType,
    viewModel: ProposalViewModel,
    goToUser: (Long) -> Unit,
    refresh: () -> Unit
) {
    val user = proposals.buyerInfo ?: offer.sellerData
    val countLeft = offer.currentQuantity

    val hourLabel = stringResource(strings.hourLabel)
    val minutesLabel = stringResource(strings.minutesLabel)

    Card(
        shape = MaterialTheme.shapes.small,
        colors = colors.cardColors,
    ) {
        if (proposals.proposals?.isNotEmpty() == true) {

            val showHistory = remember { mutableStateOf(false) }

            val proposalsToDisplay = if (proposals.proposals.size > 1 && !showHistory.value) {
                proposals.proposals.reversed().take(1)
            } else {
                proposals.proposals
            }

            proposalsToDisplay.forEachIndexed { index, proposal ->
                val typeLabel = remember { mutableStateOf("") }
                val subStatus = remember { mutableStateOf("") }
                val showBody = remember { mutableStateOf(false) }
                val showEnd = remember { mutableStateOf(false) }
                val mayAnsTo =
                    remember { proposal.tsToEndAnswer.toString().convertDateWithMinutes() }
                val endingTime = remember {
                    val total = getRemainingMinutesTime(proposal.tsToEndAnswer)
                    if (total > 1) {
                        val hours = total / 60
                        val minutes = total % 60
                        "$hours$hourLabel $minutes$minutesLabel"
                    } else {
                        ""
                    }
                }
                val statusColor = remember { mutableStateOf(colors.notifyTextColor) }
                val statusIcon = remember { mutableStateOf(drawables.iconClock) }
                val iconColor = remember { mutableStateOf(colors.notifyTextColor) }

                val proposalLabel = remember { mutableStateOf(buildAnnotatedString {  }) }

                val commentText = remember { mutableStateOf("") }

                when (type) {
                    ProposalType.MAKE_PROPOSAL -> {
                        when (proposal.clas) {
                            "proposal_accept" -> {
                                showEnd.value = false
                                showBody.value = false
                                statusColor.value = colors.positiveGreen
                                statusIcon.value = drawables.likeIcon
                                iconColor.value = colors.positiveGreen

                                if (!proposal.isResponserProposal) {
                                    typeLabel.value = stringResource(strings.proposalAcceptLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                } else {
                                    typeLabel.value = stringResource(strings.sellerProposalAcceptLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.sellerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                }
                            }
                            "proposal_unanswered" -> {
                                showEnd.value = true
                                if (!proposal.isResponserProposal) {
                                    showBody.value = false
                                    statusColor.value = colors.yellowSun
                                    statusIcon.value = drawables.iconClock
                                    iconColor.value = colors.yellowSun
                                    typeLabel.value = stringResource(strings.yourProposeWaitAnswer)
                                    subStatus.value = stringResource(strings.waitAnswerSeller)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                } else {
                                    showBody.value = true
                                    statusColor.value = colors.brightPurple
                                    statusIcon.value = drawables.iconClock
                                    iconColor.value = colors.notifyTextColor
                                    typeLabel.value = stringResource(strings.sellerWaitYourAnswer)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.sellerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                }
                            }
                            "proposal_reject" -> {
                                showEnd.value = false
                                showBody.value = (proposal.createdTs == proposals.proposals.lastOrNull()?.createdTs &&
                                        proposalsToDisplay.size < countProposalMax)
                                statusIcon.value = drawables.dislikeIcon
                                statusColor.value = colors.negativeRed
                                iconColor.value = colors.negativeRed

                                if (!proposal.isResponserProposal) {
                                    typeLabel.value = buildString {
                                        append(stringResource(strings.proposalRejectLabel))
                                        append(" ")
                                        append(stringResource(strings.withCommentProposalLabel))
                                        append(" ")
                                        append(proposal.sellerComment)
                                    }
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                } else {
                                    typeLabel.value = buildString {
                                        append(stringResource(strings.sellerProposalRejectLabel))
                                        append(" ")
                                        append(stringResource(strings.withCommentProposalLabel))
                                        append(" ")
                                        append(proposal.buyerComment)
                                    }
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.sellerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                }
                            }
                            "proposal_left_unanswered" -> {
                                showEnd.value = false
                                showBody.value = (proposal.createdTs == proposals.proposals.lastOrNull()?.createdTs &&
                                        countLeft < countProposalMax)
                                statusColor.value = colors.negativeRed
                                statusIcon.value = drawables.dislikeIcon
                                iconColor.value = colors.negativeRed
                                if (proposal.isResponserProposal) {
                                    typeLabel.value = stringResource(strings.proposalLeftUnansweredLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.sellerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                } else {
                                    typeLabel.value = stringResource(strings.leftUnansweredLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.buyerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                }
                            }
                        }
                    }
                    ProposalType.ACT_ON_PROPOSAL -> {
                        when (proposal.clas) {
                            "proposal_accept" -> {
                                showEnd.value = false
                                showBody.value = false
                                statusColor.value = colors.positiveGreen
                                statusIcon.value = drawables.likeIcon
                                iconColor.value = colors.positiveGreen
                                if (!proposal.isResponserProposal) {
                                    typeLabel.value = stringResource(strings.sellerProposalAcceptLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.buyerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                } else {
                                    typeLabel.value = stringResource(strings.proposalAcceptLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                }
                            }
                            "proposal_unanswered" -> {
                                showEnd.value = true
                                if (!proposal.isResponserProposal) {
                                    showBody.value = true
                                    statusColor.value = colors.brightPurple
                                    statusIcon.value = drawables.iconClock
                                    iconColor.value = colors.notifyTextColor
                                    typeLabel.value = stringResource(strings.buyerWaitYourAnswer)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.buyerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                } else {
                                    showBody.value = false
                                    statusColor.value = colors.yellowSun
                                    statusIcon.value = drawables.iconClock
                                    iconColor.value = colors.yellowSun
                                    typeLabel.value = stringResource(strings.waitAnswerBuyer)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                }
                            }
                            "proposal_reject" -> {
                                showEnd.value = false
                                showBody.value = false
                                statusColor.value = colors.negativeRed
                                statusIcon.value = drawables.dislikeIcon
                                iconColor.value = colors.negativeRed
                                if (!proposal.isResponserProposal) {
                                    typeLabel.value = buildString {
                                        append(stringResource(strings.sellerProposalRejectLabel))
                                        append(" ")
                                        append(stringResource(strings.withCommentProposalLabel))
                                        append(" ")
                                        append(proposal.sellerComment)
                                    }
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.buyerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                } else {
                                    typeLabel.value = buildString {
                                        append(stringResource(strings.proposalRejectLabel))
                                        append(" ")
                                        append(stringResource(strings.withCommentProposalLabel))
                                        append(" ")
                                        append(proposal.buyerComment)
                                    }
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                }
                            }
                            "proposal_left_unanswered" -> {
                                showEnd.value = false
                                showBody.value = false
                                statusColor.value = colors.negativeRed
                                statusIcon.value = drawables.dislikeIcon
                                iconColor.value = colors.negativeRed
                                if (proposal.isResponserProposal) {
                                    typeLabel.value = stringResource(strings.proposalLeftUnansweredLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.yourProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.sellerComment.orEmpty()
                                } else {
                                    typeLabel.value = stringResource(strings.leftUnansweredLabel)
                                    proposalLabel.value = formatProposalLabel(
                                        prefixRes = strings.buyerProposeLabel,
                                        price = proposal.price,
                                        quantity = proposal.quantity.toString(),
                                        colors = colors,
                                        strings = strings
                                    )
                                    commentText.value = proposal.buyerComment.orEmpty()
                                }
                            }
                        }
                    }
                }

                val discountLabel = buildAnnotatedString {
                    if ((offer.currentPricePerItem?.toDouble()
                            ?: 0.0) > (proposal.price?.toDouble() ?: 0.0)
                    ) {
                        val discount = ((((offer.currentPricePerItem?.toDouble()
                            ?: 0.0) - (proposal.price?.toDouble()
                            ?: 0.0)) / (offer.currentPricePerItem?.toDouble()
                            ?: 0.0)) * 100).toInt()

                        append(stringResource(strings.discountPrice))
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = colors.priceTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("$discount %")
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .drawBehind {
                            val stripeWidth = dimens.smallPadding.toPx()
                            drawRect(
                                color = statusColor.value,
                                topLeft = Offset(0f, 0f),
                                size = Size(stripeWidth, size.height)
                            )
                        }.padding(dimens.mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(statusIcon.value),
                            null,
                            tint = iconColor.value
                        )

                        Text(
                            text = typeLabel.value,
                            color = statusColor.value,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )

                        val btnPos = if (!showHistory.value) 0 else proposals.proposals.size - 1

                        if (index == btnPos && proposals.proposals.size > 1) {
                            SimpleTextButton(
                                stringResource(if (!showHistory.value) strings.historyLabel else strings.actionClose),
                                textColor = colors.greenWaterBlue,
                                backgroundColor = colors.transparentGrayColor,
                                textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            ) {
                                showHistory.value = !showHistory.value
                            }
                        }
                    }

                    user?.let { user ->
                        UserRow(
                            user,
                            Modifier.clickable {
                                goToUser(user.id)
                            }.fillMaxWidth()
                        )
                    }

                    Text(
                        proposalLabel.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black
                    )

                    Text(
                        discountLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.commentLabel),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.grayText
                        )
                        val comment = if (!proposal.isResponserProposal) {
                            proposal.buyerComment
                        } else {
                            proposal.sellerComment
                        }

                        Text(
                            comment ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.black,
                        )
                    }

                    if (showBody.value) {
                        getBody(offer.id, type, proposals.buyerInfo?.id ?: 1L, viewModel, refresh)
                    }

                    if (showEnd.value) {
                        if (subStatus.value != "") {
                            Text(
                                subStatus.value,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.black
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    dimens.smallPadding,
                                    Alignment.End
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(strings.mayAnswerTo),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.grayText
                                )

                                Text(
                                    mayAnsTo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.titleTextColor
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    dimens.smallPadding,
                                    Alignment.End
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(strings.leftLabel),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.grayText
                                )

                                Text(
                                    endingTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.titleTextColor
                                )
                            }
                        }
                    }

                }
            }
        } else {
            if (type == ProposalType.MAKE_PROPOSAL) {
                Column(
                    modifier = Modifier
                        .padding(dimens.mediumPadding)
                        .fillMaxWidth(),
                ) {
                    getBody(offer.id, type, proposals.buyerInfo?.id ?: 0L, viewModel, refresh)
                }
            }
        }
    }
}

@Composable
fun getBody(
    offerId: Long,
    proposalType: ProposalType,
    buyerId : Long,
    viewModel: ProposalViewModel,
    refresh : () -> Unit
){
    val fieldsState = remember { mutableStateOf(viewModel.rememberFields.value[buyerId]) }
    val fields = fieldsState.value

    LaunchedEffect(fieldsState.value){
        if (fieldsState.value == null) {
            fieldsState.value = viewModel.getFieldsProposal(offerId, buyerId, if(proposalType == ProposalType.MAKE_PROPOSAL) "make_proposal" else "act_on_proposal")
            viewModel.rememberFields.value.remove(buyerId)
            viewModel.rememberFields.value[buyerId] = fieldsState.value
        }else{
            fieldsState.value?.find { it.key == "quantity" }?.data = JsonPrimitive(1)
        }
    }

    if (fields != null) {
        val quantityTextState = remember {
            mutableStateOf(
                TextFieldValue(
                    text = (fields.find { it.key == "quantity" }?.data?.jsonPrimitive?.intOrNull
                        ?: 1).toString()
                )
            )
        }
        val priceTextState = remember {
            mutableStateOf(
                TextFieldValue(
                    text = fields.find { it.key == "price" }?.data?.jsonPrimitive?.content ?: ""
                )
            )
        }
        val commentTextState = remember {
            mutableStateOf(
                TextFieldValue(
                    text = fields.find { it.key == "comment" }?.data?.jsonPrimitive?.content ?: ""
                )
            )
        }

        val currency = stringResource(strings.currencyCode)
        val forLabel = stringResource(strings.forLabel)
        val counterLabel = stringResource(strings.countsSign)

        val errorState = remember { mutableStateOf<String?>(null) }

        val selectedChoice = remember { mutableStateOf(viewModel.rememberChoice.value[buyerId] ?: 0) }

        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {

            fields.forEach { field ->
                when (field.key) {
                    "type" -> {
                        if(buyerId != 0L) {
                            DynamicRadioButtons(field) { isChecked, choice ->
                                if (isChecked) {
                                    selectedChoice.value = choice
                                } else {
                                    selectedChoice.value = 0
                                }
                                viewModel.rememberChoice.value[buyerId] = selectedChoice.value

                                if (selectedChoice.value != 2) {
                                    priceTextState.value = TextFieldValue(text = "")
                                    commentTextState.value = TextFieldValue(text = "")
                                    quantityTextState.value = TextFieldValue(text = "")
                                    errorState.value = null

                                    fields.find { it.key == "comment" }?.data = null
                                    fields.find { it.key == "price" }?.data = null
                                    fields.find { it.key == "quantity" }?.data = null
                                }
                            }
                        }
                    }
                    "comment" -> {
                        AnimatedVisibility (selectedChoice.value == 2) {
                            val maxSymbols = field.validators?.firstOrNull()?.parameters?.max

                            val labelString = buildString {
                                when {
                                    field.shortDescription != null -> append(field.shortDescription)
                                    field.longDescription != null -> append(field.longDescription)
                                }
                            }

                            OutlinedTextInputField(
                                value = commentTextState.value,
                                onValueChange = {
                                    field.data = JsonPrimitive(it.text)
                                    commentTextState.value = it
                                },
                                singleLine = false,
                                maxSymbols = maxSymbols,
                                label = labelString,
                                keyboardType = KeyboardType.Text,
                            )
                        }
                    }
                    "price", "quantity" -> {
                        AnimatedVisibility(selectedChoice.value == 2) {
                            val maxSymbols = field.validators?.firstOrNull()?.parameters?.max
                            val maxNumber =
                                field.validators?.find { it.type == "between" }?.parameters?.max

                            val labelString = buildString {
                                when {
                                    field.shortDescription != null -> append(field.shortDescription)
                                    field.longDescription != null -> append(field.longDescription)
                                }
                                if (maxNumber != null) {
                                    append("( ${stringResource(strings.totalLabel)}")
                                    append(" $maxNumber )")
                                }
                            }

                            OutlinedTextInputField(
                                value = if (field.key == "price") priceTextState.value else quantityTextState.value,
                                onValueChange = {
                                    if (field.key == "price") {
                                        priceTextState.value = it
                                    } else {
                                        quantityTextState.value = it
                                    }

                                    field.data = checkValidation(field, it.text)

                                    val price = priceTextState.value.text.toDoubleOrNull() ?: 1.0
                                    val count = quantityTextState.value.text.toIntOrNull() ?: 1

                                    errorState.value = buildString {
                                        append((price / count).roundToLong())
                                        append(" ")
                                        append(currency)
                                        append(" ")
                                        append(forLabel)
                                        append(" ")
                                        append(1)
                                        append(" ")
                                        append(counterLabel)
                                    }
                                },
                                maxSymbols = maxSymbols,
                                maxNumber = maxNumber,
                                isMandatory = true,
                                label = labelString,
                                suffix = stringResource(if (field.key == "price") strings.currencyCode else strings.countsSign),
                                keyboardType = KeyboardType.Number,
                                error = if (field.key == "price") errorState.value else null
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.smallPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextButton(
                    stringResource(strings.actionConfirm),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                    enabled = !viewModel.isShowProgress.value && (selectedChoice.value != 2 || priceTextState.value.text.isNotBlank())
                ) {
                    if (fields.find { it.key == "type" }?.data == null){
                        fields.find { it.key == "type" }?.data = JsonPrimitive(selectedChoice.value)
                    }
                    if (buyerId != 1L){
                        fields.find { it.key == "buyer_id" }?.data = JsonPrimitive(buyerId)
                    }

                    viewModel.confirmProposal(
                        offerId,
                        proposalType,
                        fields = fields,
                        onSuccess = {
                            fields.clear()
                            viewModel.rememberChoice.value[buyerId] = 0
                            viewModel.rememberFields.value[buyerId] = fields
                            refresh()
                        },
                        onError = { fields ->
                            fieldsState.value = fields
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun formatProposalLabel(
    prefixRes: StringResource,
    price: String?,
    quantity: String,
    colors: Colors,
    strings: Strings
): AnnotatedString {
    return buildAnnotatedString {
        append(stringResource(prefixRes))
        append(" ")
        withStyle(style = SpanStyle(color = colors.priceTextColor)) {
            append(price)
            append(stringResource(strings.currencySign))
        }
        append(" ${stringResource(strings.forLabel)} ")
        withStyle(style = SpanStyle(color = colors.priceTextColor)) {
            append(quantity)
            append(stringResource(strings.countsSign))
        }
    }
}
