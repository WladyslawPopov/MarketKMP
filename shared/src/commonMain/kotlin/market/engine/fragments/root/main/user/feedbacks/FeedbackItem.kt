package market.engine.fragments.root.main.user.feedbacks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.networkObjects.Reports
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackItem(
    report: Reports,
    isMyFeedbacks: Boolean = false,
    onClickReporter: (Long) -> Unit = {},
    onClickOrder: (Long, DealTypeGroup) -> Unit,
    onClickSnapshot: (Long) -> Unit = {},
) {
    var showAnswer by remember { mutableStateOf(false) }

    val repTypeIcon =  remember {
        when (report.type) {
            "positive" -> drawables.likeIcon
            "negative" -> drawables.dislikeIcon
            else -> drawables.likeIcon// neutral
        }
    }
    
    val reColorType = remember {
        when (report.type) {
            "positive" -> colors.positiveGreen
            "negative" -> colors.negativeRed
            else -> colors.grayText// neutral
        }
    }
  
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.white, shape = MaterialTheme.shapes.small)
            .padding(dimens.smallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
           modifier = Modifier.clip(MaterialTheme.shapes.small).clickable {
               onClickReporter(report.fromUser?.id ?: 1L)
           },
           verticalAlignment = Alignment.CenterVertically,
           horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            Card(
                shape = CircleShape
            ) {
                LoadImage(
                    url = report.fromUser?.avatar?.thumb?.content ?:"",
                    isShowLoading = false,
                    isShowEmpty = false,
                    size = 40.dp
                )
            }

            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = report.fromUser?.login ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.actionTextColor,
                )

                // role + rating
                val roleLabel = if (report.fromUser?.role == "buyer")
                    stringResource(strings.buyerParameterName) else
                    stringResource(strings.sellerLabel)
                Text(
                    text = "(${report.fromUser?.rating})",
                    style = MaterialTheme.typography.titleSmall,
                )

                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.titleSmall,
                )
            }


            Icon(
                painter = painterResource(repTypeIcon),
                contentDescription = null,
                modifier = Modifier.size(dimens.mediumIconSize),
                tint = reColorType
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer),
            horizontalAlignment = Alignment.Start
        ) {

            if (!report.comment.isNullOrEmpty()) {
                Text(
                    text = report.comment,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Text(
                text = report.feedbackTs.toString().convertDateWithMinutes(),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.End)
            )

            Text(
                text = buildAnnotatedString {
                    append(stringResource(strings.orderLabel))
                    withStyle(
                        if (isMyFeedbacks || report.fromUser?.id == UserData.login)
                            SpanStyle(
                                color = colors.actionTextColor,
                            )
                        else
                            SpanStyle(
                                color = colors.grayText,
                            )
                    ){
                        append(" #${report.orderId}")
                    }
                },
                style = MaterialTheme.typography.titleSmall,
                color = colors.black,
                modifier = if (isMyFeedbacks || report.fromUser?.id == UserData.login)
                    Modifier.clickable {
                        val type = if (
                            (report.toUser?.role == "buyer" && report.toUser.id == UserData.login) ||
                            (report.fromUser?.role == "buyer" && report.fromUser.id == UserData.login)
                        )
                            DealTypeGroup.BUY
                        else
                            DealTypeGroup.SELL

                        onClickOrder(report.orderId ?: 1L, type)
                    }
                else
                    Modifier
            )

            report.offerSnapshot?.let { snap ->
                if (snap.pricePerItem?.toDouble() == 0.0) return@let
                val titleText = buildAnnotatedString {
                    if ((snap.title?.length ?: 0) <= 20) {
                        append(snap.title ?: "")
                        withStyle(
                            SpanStyle(
                                color = colors.priceTextColor,
                            )
                        ) {
                            append("  ${snap.pricePerItem} ${stringResource(strings.currencySign)}")
                        }
                    } else {
                        append("${(snap.title?.take(18) ?: "")}...  ")
                        withStyle(
                            SpanStyle(
                                color = colors.priceTextColor,
                            )
                        ) {
                            append(
                                "${snap.pricePerItem} ${stringResource(strings.currencySign)}"
                            )
                        }
                    }
                }

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.actionTextColor,
                    modifier = Modifier.clickable { onClickSnapshot(snap.id ?: 1L) }
                )
            }
        }

        report.responseFeedback?.comment?.let { response ->
            if (showAnswer) {
                Text(
                    text = response,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            val label = if (showAnswer) stringResource(strings.hideAnswerLabel) else
                stringResource(strings.showAnswerLabel)

            SimpleTextButton(
                text = label,
                modifier = Modifier.align(Alignment.End),
                textStyle = MaterialTheme.typography.bodyMedium,
                textColor = colors.actionTextColor,
                backgroundColor = colors.grayLayout,
                onClick = { showAnswer = !showAnswer }
            )
        }
    }
}
