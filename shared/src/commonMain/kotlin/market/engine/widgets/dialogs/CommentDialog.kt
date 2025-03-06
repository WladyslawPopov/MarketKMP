package market.engine.widgets.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.functions.OrderOperations
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun CommentDialog(
    isDialogOpen: Boolean,
    orderID: Long,
    orderType: Int = 0,
    commentTextDefault: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    baseViewModel: BaseViewModel,
) {
    val orderOperations: OrderOperations = koinInject()
    val analyticsHelper: AnalyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val commentText = remember { mutableStateOf(
        TextFieldValue(commentTextDefault)) }

    val charsLeft = 200 - commentText.value.text.length

    val feedbackType = remember { mutableStateOf(1) }

    val commentForYouLabel = stringResource(strings.commentForYouLabel)
    val setTrackIdLabel = stringResource(strings.setTrackIdLabel)
    val setReportBuyersLabel = stringResource(strings.setReportBuyersLabel)
    val setReportSellersLabel = stringResource(strings.setReportSellersLabel)

    val feedbacksTypePositiveLabel = stringResource(strings.feedbackTypePositiveLabel)
    val feedbacksTypeNeutralLabel = stringResource(strings.feedbackTypeNeutralLabel)
    val feedbacksTypeNegativeLabel = stringResource(strings.feedbackTypeNegativeLabel)


    LaunchedEffect(isDialogOpen){
        snapshotFlow{isDialogOpen}.collect{
            if(it)
                commentText.value = TextFieldValue(commentTextDefault)
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text(
                    text = when (orderType) {
                            0 -> setReportBuyersLabel
                            1 -> setReportSellersLabel
                            -1 -> commentForYouLabel
                            -2 -> setTrackIdLabel
                            else -> commentForYouLabel
                        }
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    item {
                        if (orderType >= 0) {
                            Row(
                                modifier = Modifier.clickable {
                                    feedbackType.value = 1
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            ) {
                                RadioButton(
                                    selected = feedbackType.value == 1, // positive
                                    onClick = {
                                        feedbackType.value = 1
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.inactiveBottomNavIconColor,
                                        unselectedColor = colors.black
                                    )
                                )
                                Text(
                                    text = feedbacksTypePositiveLabel,
                                    color = colors.positiveGreen
                                )
                            }
                            Row(
                                modifier = Modifier.clickable {
                                    feedbackType.value = 2
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            ) {
                                RadioButton(
                                    selected = feedbackType.value == 2, // neutral
                                    onClick = {
                                        feedbackType.value = 2
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.inactiveBottomNavIconColor,
                                        unselectedColor = colors.black
                                    )
                                )
                                Text(
                                    text = feedbacksTypeNeutralLabel,
                                    modifier = Modifier,
                                    color = colors.grayText
                                )
                            }
                            Row(
                                modifier = Modifier.clickable {
                                    feedbackType.value = 0
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            ) {
                                RadioButton(
                                    selected = feedbackType.value == 0, // negative
                                    onClick = {
                                        feedbackType.value = 0
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.inactiveBottomNavIconColor,
                                        unselectedColor = colors.black
                                    )
                                )
                                Text(
                                    text = feedbacksTypeNegativeLabel,
                                    modifier = Modifier,
                                    color = colors.negativeRed
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextInputField(
                            value = commentText.value,
                            onValueChange = {
                                if (charsLeft > 0){
                                    commentText.value = it
                                }
                            },
                            label =  if (orderType == -2) stringResource(strings.trackIdLabel)
                            else stringResource(strings.messageLabel),
                            maxSymbols = charsLeft,
                            singleLine = false
                        )
                    }

                }
            },
            containerColor = colors.white,
            confirmButton = {
                val errString = stringResource(strings.operationFailed)
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                    enabled = commentText.value.text.isNotEmpty()
                ) {
                    val scope = baseViewModel.viewModelScope

                    if (orderType < 0) {
                        when(orderType) {
                            -1 -> {
                                // set_comment
                                val body = HashMap<String, String>()
                                body["comment"] = commentText.value.text
                                scope.launch(Dispatchers.IO) {
                                    val res = orderOperations.postSetComment(orderID, body)
                                    val buffer = res.success
                                    val error = res.error
                                    withContext(Dispatchers.Main) {
                                        if (buffer != null && buffer.operationResult?.result == "ok") {
                                            onSuccess()
                                        } else if (buffer != null) {
                                            baseViewModel.showToast(
                                                errorToastItem.copy(
                                                    message = errString
                                                )
                                            )
                                        } else {
                                            error?.let { baseViewModel.onError(it) }
                                        }
                                    }
                                }
                            }
                            -2 -> {
                                // set_track_id
                                val body = HashMap<String, String>()
                                body["track_id"] = commentText.value.text

                                scope.launch(Dispatchers.IO) {
                                    val res = orderOperations.postProvideTrackId(orderID, body)
                                    val buffer = res.success
                                    val error = res.error
                                    withContext(Dispatchers.Main) {
                                        if (buffer != null && buffer.operationResult?.result == "ok") {
                                            onSuccess()
                                        } else if (buffer != null) {
                                            baseViewModel.showToast(
                                                errorToastItem.copy(
                                                    message = errString
                                                )
                                            )
                                        } else {
                                            error?.let { baseViewModel.onError(it) }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val body = HashMap<String, String>()
                        body["feedback_type"] = feedbackType.value.toString()
                        body["comment"] = commentText.value.text

                        scope.launch(Dispatchers.IO) {
                            val response = when (orderType) {
                                0 -> {
                                    // give_feedback_to_buyer
                                    orderOperations.postGiveFeedbackToBuyer(orderID, body)
                                }
                                1 -> {
                                    // give_feedback_to_seller
                                    orderOperations.postGiveFeedbackToSeller(orderID, body)
                                }
                                else -> {
                                    // fallback
                                    orderOperations.postGiveFeedbackToBuyer(orderID, body)
                                }
                            }
                            val buf = response.success
                            val err = response.error
                            withContext(Dispatchers.Main) {
                                if (buf != null && buf.operationResult?.result == "ok") {
                                    val eventMap = mapOf(
                                        "order_id" to orderID.toString(),
                                    )
                                    val eventName = when(orderType) {
                                        0 -> "sent_review_to_buyer"
                                        1 -> "sent_review_to_seller"
                                        else -> "sent_review"
                                    }
                                    analyticsHelper.reportEvent(eventName, eventMap)

                                    onSuccess()
                                    onDismiss()
                                } else {
                                    err?.let { baseViewModel.onError(it) }
                                    onDismiss()
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.steelBlue,
                    textColor = colors.alwaysWhite
                ) {
                    onDismiss()
                }
            }
        )
    }
}
