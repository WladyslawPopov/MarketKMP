package market.engine.presentation.user.feedbacks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Reports
import market.engine.core.util.convertDateWithMinutes
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbackItem(
    report: Reports,
    onClickReporter: (Long) -> Unit = {},
    onClickOrder: (Long) -> Unit = {},
    onClickSnapshot: (Long) -> Unit = {},
) {
    var showAnswer by remember { mutableStateOf(false) }

    val repTypeIcon = when(report.type) {
        "positive" -> drawables.miniLikeIcon 
        "negative" -> drawables.miniDislikeIcon
        else -> drawables.miniLikeIcon// neutral
    }
    
    val reColorType = when(report.type) {
        "positive" -> colors.positiveGreen
        "negative" -> colors.negativeRed
        else -> colors.grayText// neutral
    }
  
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding)
    ) {
        Row {
            
            LoadImage(
                report.fromUser?.avatar?.thumb?.content ?:"",
                size = 60.dp
            )

            Spacer(modifier = Modifier.width(dimens.smallPadding))

            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.wrapContentSize().clickable { 
                        onClickReporter(report.fromUser?.id ?: 1L)
                    }
                ) {
                    Text(
                        text = report.fromUser?.login ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.actionTextColor,
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.width(dimens.smallSpacer))
                    
                    // role + rating
                    val roleLabel = if (report.fromUser?.role == "buyer")
                        stringResource(strings.buyerParameterName) else
                            stringResource(strings.sellerLabel)
                    Text(
                        text = "(${report.fromUser?.rating}) $roleLabel",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!report.comment.isNullOrEmpty()) {
                    Text(
                        text = report.comment,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
             
                Text(
                    text = report.feedbackTs.toString().convertDateWithMinutes(),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                val orderText = "${stringResource(strings.orderLabel)} #${report.orderId}"
                Text(
                    text = orderText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { onClickOrder(report.orderId ?: 1L) }
                )
                
                report.offerSnapshot?.let { snap ->
                    val titleText = if ((snap.title?.length ?: 0) <= 20) {
                        "${snap.title}  ${snap.pricePerItem} \$"
                    } else {
                        (snap.title?.take(18) ?: "") + "...  ${snap.pricePerItem} \$"
                    }
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { onClickSnapshot(snap.id ?: 1L) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimens.smallSpacer))

            Icon(
                painter = painterResource(repTypeIcon),
                contentDescription = null,
                modifier = Modifier.size(dimens.smallIconSize),
                tint = reColorType
            )
        }
        
        report.responseFeedback?.comment?.let { response ->
            Spacer(modifier = Modifier.height(dimens.smallSpacer))
            if (showAnswer) {
                Text(text = response, style = MaterialTheme.typography.bodyMedium)
            }
            val label = if (showAnswer) strings.hideAnswerLabel else strings.showAnswerLabel
            ActionButton(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                alignment = Alignment.Center,
                fontSize = dimens.mediumText,
                onClick = { showAnswer = !showAnswer }
            )
        }
    }
}
