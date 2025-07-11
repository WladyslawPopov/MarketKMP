package market.engine.widgets.textFields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.utils.processInput
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutlinedTextInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester? = null,
    isMandatory: Boolean = false,
    maxSymbols: Int? = null,
    maxNumber: Int? = null,
    error: String? = null,
    suffix: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    isEmail: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val counter = remember { mutableStateOf(maxSymbols) }

    Column(
        modifier = modifier
    ) {
        if (maxSymbols != null) {
            Text(
                "${stringResource(strings.charactersLeftLabel)}: ${counter.value}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End),
                color = colors.grayText
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = {
                if (maxNumber == null) {
                    if (maxSymbols != null) {
                        if(maxSymbols >= it.text.length) {
                            counter.value = maxSymbols - it.text.length
                            onValueChange(it)
                        }
                    }else{
                        onValueChange(it)
                    }
                } else {
                    if ((it.text.toIntOrNull() ?:0) >= maxNumber) {
                        onValueChange(TextFieldValue(maxNumber.toString()))
                    }else{
                        onValueChange(it)
                    }
                }
            },
            placeholder = {
                Text(
                    text = placeholder ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.grayText
                )
            },
            label = {
                DynamicLabel(
                    text = label,
                    isMandatory = isMandatory
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isEmail) KeyboardType.Email else keyboardType,
                capitalization = if(isEmail || isPassword) KeyboardCapitalization.Unspecified else KeyboardCapitalization.Sentences
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    SmallIconButton(
                        if (passwordVisible) drawables.eyeClose else drawables.eyeOpen,
                        colors.black
                    ) {
                        passwordVisible = !passwordVisible
                    }
                }
            } else null,
            suffix = if (suffix != null){
                 {
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.black
                    )
                }
            }else null,
            supportingText = {
                Text(
                    processInput(error) ?: error ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.notifyTextColor
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colors.white,
                focusedContainerColor = colors.white,
                unfocusedBorderColor = colors.black,
                focusedBorderColor = colors.textA0AE,
                unfocusedTextColor = colors.black,
                focusedTextColor = colors.black,
                selectionColors = TextSelectionColors(
                    handleColor = colors.promoHighlight,
                    backgroundColor = colors.promoHighlight.copy(alpha = 0.4f)
                )
            ),
            modifier = if(focusRequester != null) Modifier
                .widthIn(max = 600.dp).fillMaxWidth()
                .focusRequester(focusRequester) else
                    Modifier.widthIn(max = 600.dp).fillMaxWidth(),
            maxLines = 4,
            singleLine = singleLine,
            enabled = enabled
        )
    }
}
