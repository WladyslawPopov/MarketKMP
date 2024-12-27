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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.checkNumberKeyBoard
import market.engine.core.utils.checkValidation
import market.engine.core.utils.processInput
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicInputField(
    field: Fields,
    modifier: Modifier = Modifier,
    label : String? = null,
    suffix : String? = null,
    mandatory: Boolean? = null,
    singleLine: Boolean = true,
) {

    LaunchedEffect(Unit){
        if (field.data != null) {
            field.data = checkValidation(field, field.data?.jsonPrimitive?.content ?: "")
        }
    }

    val textState = remember {
        mutableStateOf(field.data?.jsonPrimitive?.content ?: "")
    }

    val errorState = rememberUpdatedState(field.errors)

    val maxSymbols = field.validators?.firstOrNull()?.parameters?.max

    val counter = remember { mutableStateOf(maxSymbols) }

    val isMandatory = remember {
      mutableStateOf(
          (mandatory ?: field.validators?.find { it.type == "mandatory" }) != null
        )
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
                if (maxSymbols != null) {
                    if ( maxSymbols >= it.length) {
                        counter.value = maxSymbols - it.length
                        field.data = JsonPrimitive(it)
                        textState.value = it
                    }
                }else{
                    field.data = checkValidation(field, it)
                    textState.value = it
                }

            },
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DynamicLabel(
                        text = label ?: field.shortDescription ?: field.longDescription ?:  "",
                        isMandatory = isMandatory.value
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
            },
            modifier = modifier,
            singleLine = singleLine,
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
            textStyle = MaterialTheme.typography.titleSmall,
            keyboardOptions = KeyboardOptions(
                keyboardType = checkNumberKeyBoard(field)
            ),
            supportingText = {
                Text(
                    processInput(errorState.value) ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.notifyTextColor
                )
            }
        )
    }
}

