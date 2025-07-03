package market.engine.fragments.root.login

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun O2AuthContent(
    auth2ContentState: Auth2ContentState
) {
    val focusManager = LocalFocusManager.current
    val userEmail = auth2ContentState.obfuscatedIdentity
    val humanMessage = auth2ContentState.humanMessage
    val leftTime = auth2ContentState.lastRequestByIdentity

    val leftTimer = auth2ContentState.leftTimerState.collectAsState()
    val codeState = auth2ContentState.codeState.collectAsState()

    Column(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }.background(colors.white).fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Column(
            modifier = Modifier.padding(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            Text(
                stringResource(strings.enterLogin),
                style = MaterialTheme.typography.titleLarge,
                color = colors.black,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.padding(dimens.mediumPadding)
            )

            Text(
                humanMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = colors.grayText
            )

            OutlinedTextInputField(
                value = TextFieldValue(text = userEmail ?: ""),
                onValueChange = {},
                label = "",
                keyboardType = KeyboardType.Email,
                isEmail = true,
                enabled = false
            )

            if(leftTime != null) {
                OutlinedTextField(
                    value = codeState.value,
                    onValueChange = {
                        if (it.text.length <= 4)
                            auth2ContentState.onCodeChange(it)
                        else
                            focusManager.clearFocus()
                    },
                    prefix = {
                        Text(
                            text = stringResource(strings.codeLabel),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.black,
                            modifier = Modifier.padding(end = dimens.smallPadding)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = colors.white,
                        focusedContainerColor = colors.white,
                        unfocusedBorderColor = colors.black,
                        focusedBorderColor = colors.textA0AE,
                        unfocusedTextColor = colors.black,
                        focusedTextColor = colors.black
                    ),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                )
            }

            AcceptedPageButton(
                stringResource(strings.submitO2AuthLabel),
                enabled = leftTimer.value == 0 || leftTime == null,
            ) {
                auth2ContentState.onCodeSubmit()
            }

            if (leftTimer.value > 0 && leftTime != null){
                Text(
                    buildString {
                        append(stringResource(strings.leftTimeLabel))
                        append(" ")
                        append(leftTimer.value.toString())
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.positiveGreen
                )
            }
        }
    }
}
