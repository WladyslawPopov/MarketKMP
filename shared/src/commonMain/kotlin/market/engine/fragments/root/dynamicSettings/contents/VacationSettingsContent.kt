package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.DateBtn
import market.engine.widgets.dialogs.DateDialog
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.HeaderAlertText
import org.jetbrains.compose.resources.stringResource

@Composable
fun VacationSettingsContent(
    fields : ArrayList<Fields>,
    onConfirm : () -> Unit,
) {
    val from = stringResource(strings.fromAboutTimeLabel)
    val to = stringResource(strings.toAboutTimeLabel)

    val fromThisDateTextState = remember {
        mutableStateOf(
            buildString {
                append(from)
                append(" ")
                append(
                    (fields.find {
                        it.key == "from_time"
                    }?.data?.jsonPrimitive?.content ?: "").convertDateWithMinutes()
                )
            }
        )
    }

    val toThisDateTextState = remember {
        mutableStateOf(
            buildString {
                append(to)
                append(" ")
                append(
                    (fields.find {
                        it.key == "to_time"
                    }?.data?.jsonPrimitive?.content ?: "").convertDateWithMinutes()
                )
            }
        )
    }

    val showDateDialog = remember { mutableStateOf("") }

    HeaderAlertText(
        rememberRichTextState().setHtml(stringResource(strings.vacationHeaderSettingsLabel)).annotatedString
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateBtn(
            fromThisDateTextState.value,
            Modifier.weight(1f)
        ) {
            showDateDialog.value = "from"
        }
        DateBtn(
            toThisDateTextState.value,
            Modifier.weight(1f)
        ) {
            showDateDialog.value = "to"
        }
    }

    DateDialog(
        isSelectableDates = true,
        showDialog = showDateDialog.value.isNotBlank(),
        onDismiss = {
            showDateDialog.value = ""
        },
        onSucceed = { futureTimeInSeconds ->
            if (showDateDialog.value == "from") {
                fields.find {
                    it.key == "from_time"
                }?.data = JsonPrimitive(futureTimeInSeconds)
                fromThisDateTextState.value = buildString {
                    append(from)
                    append(" ")
                    append(futureTimeInSeconds.toString().convertDateWithMinutes())
                }
            } else {
                fields.find {
                    it.key == "to_time"
                }?.data = JsonPrimitive(futureTimeInSeconds)
                toThisDateTextState.value = buildString {
                    append(to)
                    append(" ")
                    append(futureTimeInSeconds.toString().convertDateWithMinutes())
                }
            }

            showDateDialog.value = ""
        }
    )

    fields.find {
        it.key == "comment"
    }?.let {
        DynamicInputField(
            field = it,
            singleLine = false
        )
    }

    fields.find {
        it.key == "enabled"
    }?.let { field ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isEnabled = remember { mutableStateOf(field.data?.jsonPrimitive?.boolean ?: false) }

            Switch(
                checked = isEnabled.value,
                onCheckedChange = {
                    isEnabled.value = !isEnabled.value
                    field.data = JsonPrimitive(isEnabled.value)
                },
                colors = SwitchDefaults.colors(
                    checkedBorderColor = colors.transparent,
                    checkedThumbColor = colors.positiveGreen,
                    checkedTrackColor = colors.transparentGrayColor,
                    uncheckedBorderColor = colors.transparent,
                    uncheckedThumbColor = colors.negativeRed,
                    uncheckedTrackColor = colors.transparentGrayColor,
                ),
            )
        }
    }

    AcceptedPageButton(
        strings.actionConfirm
    ) {
        onConfirm()
    }
}
