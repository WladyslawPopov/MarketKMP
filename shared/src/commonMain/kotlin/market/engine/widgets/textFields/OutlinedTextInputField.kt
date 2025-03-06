package market.engine.widgets.textFields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutlinedTextInputField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isMandatory: Boolean = false,
    maxSymbols: Int? = null,
    maxNumber: Int? = null,
    error: String? = null,
    suffix: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    isEmail: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val counter = remember { mutableStateOf(maxSymbols) }

    Column {
        if (maxSymbols != null) {

            Text(
                "${stringResource(strings.charactersLeftLabel)}: ${counter.value}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.End)
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
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) painterResource(drawables.eyeClose) else painterResource(
                                drawables.eyeOpen
                            ),
                            contentDescription = ""
                        )
                    }
                }
            },
            suffix = {
                if (suffix != null) {
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.black
                    )
                }
            },
            supportingText = {
                Text(
                    processInput(error) ?: error ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.notifyTextColor
                )
            },
            modifier = Modifier
                .widthIn(500.dp)
                .focusRequester(focusRequester),
            maxLines = 4,
            singleLine = singleLine
        )
    }
}
