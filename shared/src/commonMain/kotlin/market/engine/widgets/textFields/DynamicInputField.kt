package market.engine.widgets.textFields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.checkNumberKeyBoard
import market.engine.core.utils.checkValidation
import market.engine.core.utils.processInput
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicInputField(
    field: Fields,
    label : String? = null,
    suffix : String? = null,
    mandatory: Boolean? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    onValueChange: (Fields) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isPassword = remember(field) { field.widgetType == "password" }

    val textState = remember(field.data) {
        mutableStateOf(field.data?.jsonPrimitive?.content ?: "")
    }

    val errorState = rememberUpdatedState(field.errors)

    val maxSymbols = remember(field.validators) { field.validators?.firstOrNull()?.parameters?.max }

    val maxNumber = remember(field.validators) { field.validators?.find { it.type == "between" }?.parameters?.max }

    val counter = remember(maxSymbols) { mutableStateOf(maxSymbols) }

    val isMandatory = remember(field.validators, mandatory) {
        mandatory ?: (field.validators?.find { it.type == "mandatory" } != null)
    }

    Column {
        if (maxSymbols != null) {
            Text(
                "${stringResource(strings.charactersLeftLabel)}: ${counter.value}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End)
            )
        }

        TextField(
            value = textState.value,
            onValueChange = {
                val addNewValue : (String) -> Unit = { newValue ->
                    val updatedField = field.copy(
                        data = checkValidation(field, newValue)
                    )
                    onValueChange(updatedField)
                }

                if (maxNumber == null) {
                    if (maxSymbols != null) {
                        if(maxSymbols >= it.length) {
                            counter.value = maxSymbols - it.length
                            addNewValue(it)
                        }
                    }else{
                        addNewValue(it)
                    }
                }else{
                    if ((it.toIntOrNull() ?:0) >= maxNumber) {
                        addNewValue(maxNumber.toString())
                    }else{
                        addNewValue(it)
                    }
                }
            },
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelString = buildString {
                        when{
                            label != null -> append(label)
                            field.shortDescription != null -> append(field.shortDescription)
                            field.longDescription != null -> append(field.longDescription)
                        }
                        if (maxNumber!= null){
                            append(" ")
                            append(stringResource(strings.totalCostLabel))
                            append("( $maxNumber )")
                        }
                    }

                    DynamicLabel(
                        text = labelString,
                        isMandatory = isMandatory
                    )
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

                if (isPassword) {
                    SmallIconButton(
                        if (passwordVisible) drawables.eyeClose else drawables.eyeOpen,
                        colors.black
                    ) {
                        passwordVisible = !passwordVisible
                    }
                }
            },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedTextColor = colors.black,
                unfocusedTextColor = colors.black,

                focusedContainerColor = colors.white,
                unfocusedContainerColor = colors.white,

                focusedIndicatorColor = colors.transparent,
                unfocusedIndicatorColor = colors.transparent,
                disabledIndicatorColor = colors.transparent,
                errorIndicatorColor = colors.transparent,

                errorContainerColor = colors.white,

                focusedPlaceholderColor = colors.steelBlue,
                unfocusedPlaceholderColor = colors.steelBlue,
                disabledPlaceholderColor = colors.transparent
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = MaterialTheme.typography.titleMedium,
            keyboardOptions = KeyboardOptions(
                keyboardType = checkNumberKeyBoard(field),
                capitalization = KeyboardCapitalization.Sentences
            ),
            supportingText = {
                Text(
                    processInput(errorState.value) ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.notifyTextColor
                )
            },
            maxLines = 4
        )
    }
}

