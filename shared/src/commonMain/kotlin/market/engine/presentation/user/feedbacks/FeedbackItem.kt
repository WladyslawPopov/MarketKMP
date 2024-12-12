package market.engine.presentation.user.feedbacks

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.network.networkObjects.Reports
import market.engine.core.util.convertDateWithMinutes
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbackItem(
    report: Reports,
    isMyFeedbacks: Boolean = false,
    onClickReporter: (Long) -> Unit = {},
    onClickOrder: (Long) -> Unit = {},
    onClickSnapshot: (Long) -> Unit = {},
) {
    var showAnswer by remember { mutableStateOf(false) }

    val repTypeIcon = when(report.type) {
        "positive" -> drawables.likeIcon
        "negative" -> drawables.dislikeIcon
        else -> drawables.likeIcon// neutral
    }
    
    val reColorType = when(report.type) {
        "positive" -> colors.positiveGreen
        "negative" -> colors.negativeRed
        else -> colors.grayText// neutral
    }
  
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.white, shape = MaterialTheme.shapes.medium)
            .padding(dimens.smallPadding)
    ) {
        Row(
           modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
           verticalAlignment = Alignment.CenterVertically,
           horizontalArrangement = Arrangement.SpaceAround
        ) {
            Card(
                modifier = Modifier.padding(dimens.smallPadding),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                LoadImage(
                    url = report.fromUser?.avatar?.thumb?.content ?:"",
                    isShowLoading = false,
                    isShowEmpty = false,
                    size = 40.dp
                )
            }
            Spacer(modifier = Modifier.width(dimens.smallPadding))

            Column {
                Spacer(modifier = Modifier.height(dimens.smallSpacer))
                Row(
                    modifier = Modifier.wrapContentSize().clickable { 
                        onClickReporter(report.fromUser?.id ?: 1L)
                    }
                ) {
                    Text(
                        text = report.fromUser?.login ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.actionTextColor,
                    )
                    
                    Spacer(modifier = Modifier.width(dimens.smallSpacer))
                    
                    // role + rating
                    val roleLabel = if (report.fromUser?.role == "buyer")
                        stringResource(strings.buyerParameterName) else
                            stringResource(strings.sellerLabel)
                    Text(
                        text = "(${report.fromUser?.rating}) $roleLabel",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }

                if (!report.comment.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(dimens.smallSpacer))
                    Text(
                        text = report.comment,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(modifier = Modifier.height(dimens.smallSpacer))
                Text(
                    text = report.feedbackTs.toString().convertDateWithMinutes(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.width(dimens.mediumSpacer))

                val orderText = buildAnnotatedString {
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
                }
                Spacer(modifier = Modifier.height(dimens.smallSpacer))

                Text(
                    text = orderText,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.black,
                    modifier = if (isMyFeedbacks || report.fromUser?.id == UserData.login)
                        Modifier.clickable { onClickOrder(report.orderId ?: 1L) }
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
                                    color = colors.titleTextColor,
                                )
                            ) {
                                append("  ${snap.pricePerItem} ${stringResource(strings.currencySign)}")
                            }
                        } else {
                            append("${(snap.title?.take(18) ?: "")}...  ")
                            withStyle(
                                SpanStyle(
                                    color = colors.titleTextColor,
                                )
                            ) {
                                append(
                                    "${snap.pricePerItem} ${stringResource(strings.currencySign)}"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(dimens.smallSpacer))
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.actionTextColor,
                        modifier = Modifier.clickable { onClickSnapshot(snap.id ?: 1L) }
                    )
                }
            }

            Icon(
                painter = painterResource(repTypeIcon),
                contentDescription = null,
                modifier = Modifier.size(dimens.mediumIconSize),
                tint = reColorType
            )
            Spacer(modifier = Modifier.width(dimens.smallSpacer))
        }
        
        report.responseFeedback?.comment?.let { response ->
            Spacer(modifier = Modifier.height(dimens.mediumSpacer))
            if (showAnswer) {
                Text(
                    text = response,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(dimens.smallPadding)
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
