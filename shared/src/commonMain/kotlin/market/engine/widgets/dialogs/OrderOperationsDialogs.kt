package market.engine.widgets.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Order
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderOperationsDialogs(
    order: Order,
    title: MutableState<String>,
    showDialog: MutableState<String>,
    viewModel : BaseViewModel,
    updateItem: (Long) -> Unit,
) {
    val feedbackType = remember { mutableStateOf(1) }

    val feedbacksTypePositiveLabel = stringResource(strings.feedbackTypePositiveLabel)
    val feedbacksTypeNeutralLabel = stringResource(strings.feedbackTypeNeutralLabel)
    val feedbacksTypeNegativeLabel = stringResource(strings.feedbackTypeNegativeLabel)

    when (showDialog.value) {
        "error" -> {
            CustomDialog(
                showDialog = showDialog.value != "",
                title = buildAnnotatedString {
                    append(stringResource(strings.messageAboutError))
                },
                body = {
                    Text(title.value)
                },
                onDismiss = { showDialog.value = "" }
            )
        }
        "give_feedback_to_buyer","give_feedback_to_seller" -> {
            val commentText = mutableStateOf(TextFieldValue(stringResource(strings.defaultCommentReport)))
            CustomDialog(
                showDialog.value != "",
                title = AnnotatedString(title.value),
                body = {
                    LazyColumnWithScrollBars(
                        heightMod = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        horizontalAlignment = Alignment.Start
                    ) {
                        item {
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

                        item {
                            OutlinedTextInputField(
                                value = commentText.value,
                                onValueChange = {
                                    commentText.value = it
                                },
                                label = stringResource(strings.commentLabel),
                                maxSymbols = 200,
                                singleLine = false
                            )
                        }
                    }
                },
                onSuccessful = {
                    val body = HashMap<String, JsonElement>()
                    body["feedback_type"] = JsonPrimitive(feedbackType.value)
                    body["comment"] = JsonPrimitive(commentText.value.text)

                    viewModel.postOperationAdditionalData(
                        order.id,
                        showDialog.value,
                        "orders",
                        body = body,
                        onSuccess = {
                            updateItem(order.id)
                        }
                    )
                },
                onDismiss = {
                    showDialog.value = ""
                },
            )
        }

        "set_comment" -> {
            val commentText = mutableStateOf(TextFieldValue(""))
            CustomDialog(
                showDialog.value != "",
                title = AnnotatedString(title.value),
                body = {
                    OutlinedTextInputField(
                        value = commentText.value,
                        onValueChange = {
                            commentText.value = it
                        },
                        label = stringResource(strings.commentLabel),
                        maxSymbols = 200,
                        singleLine = false
                    )
                },
                onSuccessful = {
                    val body = HashMap<String, JsonElement>()
                    body["comment"] = JsonPrimitive(commentText.value.text)

                   viewModel.postOperationAdditionalData(
                        order.id,
                        "set_comment",
                        "orders",
                        body,
                        onSuccess = {
                            updateItem(order.id)
                            showDialog.value = ""
                        }
                    )
                },
                onDismiss = {
                    showDialog.value = ""
                },
            )
        }

        "provide_track_id" -> {
            val commentText = mutableStateOf(TextFieldValue(""))
            CustomDialog(
                showDialog.value != "",
                title = AnnotatedString(title.value),
                body = {
                    OutlinedTextInputField(
                        value = commentText.value,
                        onValueChange = {
                            commentText.value = it
                        },
                        label = stringResource(strings.trackIdLabel),
                        maxSymbols = 200,
                        singleLine = false
                    )
                },
                onSuccessful = {
                    val body = HashMap<String, JsonElement>()
                    body["track_id"] = JsonPrimitive(commentText.value.text)

                    viewModel.postOperationAdditionalData(
                        order.id,
                        "provide_track_id",
                        "orders",
                        body = body,
                        onSuccess = {
                            updateItem(order.id)
                            showDialog.value = ""
                        }
                    )
                },
                onDismiss = {
                    showDialog.value = ""
                },
            )
        }
        else -> {


        }
    }
}
