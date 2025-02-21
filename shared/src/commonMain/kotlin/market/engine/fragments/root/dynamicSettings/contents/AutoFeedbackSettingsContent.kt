package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.textFields.DynamicInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun AutoFeedbackSettingsContent(
    fields: ArrayList<Fields>,
    onConfirm: () -> Unit
) {
    fields.forEach { field ->
        when (field.key) {
            "auto_feedback_enabled" -> {
                val isEnabled = remember { mutableStateOf(field.data?.jsonPrimitive?.booleanOrNull ?: false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Text(
                            stringResource(strings.autoFeedbackTitle),
                            color = colors.black,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            stringResource(strings.autoFeedbackSubtitle),
                            color = colors.grayText,
                            style = MaterialTheme.typography.bodySmall
                        )

                        AnimatedVisibility(
                            visible = isEnabled.value
                        ){
                            fields.find { it.key == "comment" }?.let {
                                DynamicInputField(it, singleLine = false)
                            }
                        }
                    }


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

            "auto_feedback_after_exceeded_days_limit_enabled" -> {
                val isEnabled = remember { mutableStateOf(field.data?.jsonPrimitive?.booleanOrNull ?: false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Text(
                            stringResource(strings.autoFeedbackAfterExceededTimeTitle),
                            color = colors.black,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            stringResource(strings.autoFeedbackAfterExceededTimeSubtitle),
                            color = colors.grayText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

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
        }
    }

    AcceptedPageButton(
        strings.actionConfirm
    ){
        onConfirm()
    }
}
