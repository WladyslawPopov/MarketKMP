package market.engine.widgets.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

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
    val commentText = remember { mutableStateOf(
        TextFieldValue(commentTextDefault)) }

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
                LazyColumnWithScrollBars(
                    heightMod = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
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
                                commentText.value = it
                            },
                            label = if (orderType == -2) stringResource(strings.trackIdLabel)
                            else stringResource(strings.messageLabel),
                            maxSymbols = 200,
                            singleLine = false
                        )
                    }
                }
            },
            containerColor = colors.white,
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                    enabled = commentText.value.text.isNotEmpty()
                ) {
                    if (orderType < 0) {
                        when (orderType) {
                            -1 -> {
                                //set comment
                                val body = HashMap<String, JsonElement>()
                                body["comment"] = JsonPrimitive(commentText.value.text)

                                baseViewModel.postOperationAdditionalData(
                                    orderID,
                                    "set_comment",
                                    "orders",
                                    body,
                                    onSuccess = {
                                        onSuccess()
                                    }
                                )
                            }

                            -2 -> {
                                // set_track_id
                                val body = HashMap<String, JsonElement>()
                                body["track_id"] = JsonPrimitive(commentText.value.text)

                                baseViewModel.postOperationAdditionalData(
                                    orderID,
                                    "provide_track_id",
                                    "orders",
                                    body = body,
                                    onSuccess = {
                                        onSuccess()
                                    }
                                )
                            }
                        }
                    } else {
                        val body = HashMap<String, JsonElement>()
                        body["feedback_type"] = JsonPrimitive(feedbackType.value)
                        body["comment"] = JsonPrimitive(commentText.value.text)

                        baseViewModel.postOperationAdditionalData(
                            orderID,
                            when (orderType) {
                                0 -> {
                                    // give_feedback_to_buyer
                                    "give_feedback_to_buyer"
                                }
                                1 -> {
                                    // give_feedback_to_seller
                                    "give_feedback_to_seller"
                                }
                                else -> {
                                    // fallback
                                    "give_feedback_to_buyer"
                                }
                            },
                            "orders",
                            body = body,
                            onSuccess = {
                                onSuccess()
                                onDismiss()
                            }
                        )
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
