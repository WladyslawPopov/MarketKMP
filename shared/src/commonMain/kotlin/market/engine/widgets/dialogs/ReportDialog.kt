package market.engine.widgets.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Order
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReportDialog(
    isDialogOpen: Boolean,
    order: Order,
    mood: String,
    text: String,
    type: String,
    onDismiss: () -> Unit,
) {
    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    if (isDialogOpen) {
        val def = stringResource(strings.toMeFeedbacksLabel)

        val eventParameters = mapOf(
            "order_id" to order.id.toString(),
            "buyer_id" to order.buyerData?.id.toString(),
            "seller_id" to order.sellerData?.id.toString(),
        )

        LaunchedEffect(Unit) {
            if (type == def) {
                analyticsHelper.reportEvent("click_review_to_seller", eventParameters)
            } else {
                analyticsHelper.reportEvent("click_review_to_buyer", eventParameters)
            }
        }

        val moodColor = when (mood) {
            "positive" -> colors.positiveGreen
            "neutral"  -> colors.grayText
            "negative" -> colors.negativeRed
            else       -> colors.grayText
        }

        val moodLabel = when (mood) {
            "positive" -> stringResource(strings.feedbackTypePositiveLabel)
            "neutral"  -> stringResource(strings.feedbackTypeNeutralLabel)
            "negative" -> stringResource(strings.feedbackTypeNegativeLabel)
            else -> ""
        }

        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text(type, style = MaterialTheme.typography.titleSmall, color = colors.black)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                ) {
                    Text(
                        text = moodLabel,
                        color = moodColor,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {

            },
            containerColor = colors.white,
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.inactiveBottomNavIconColor
                ) {
                    onDismiss()
                }
            }
        )
    }
}
