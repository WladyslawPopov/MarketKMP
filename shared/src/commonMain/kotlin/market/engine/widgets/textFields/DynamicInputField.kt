package market.engine.widgets.textFields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.checkNumberKeyBoard
import market.engine.core.utils.processInput
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicInputField(
    field: Fields,
    modifier: Modifier = Modifier
) {

    val textState = remember {
        mutableStateOf(field.data?.jsonPrimitive?.content ?: "")
    }

    val error = remember {  mutableStateOf(processInput(field.errors)) }

    val isError = remember {
        mutableStateOf(error.value == null)
    }

    val maxSymbols = field.validators?.firstOrNull()?.parameters?.max

    val counter = remember { mutableStateOf(maxSymbols) }

    val isMandatory = remember {
        mutableStateOf(
        field.validators?.find { it.type == "mandatory" } != null
        )
    }

    TextField(
        value = textState.value,
        onValueChange = {
            if (maxSymbols != null && maxSymbols > it.length) {
                counter.value = maxSymbols - it.length
            }
            field.data = JsonPrimitive(it)
            textState.value = it
        },
        label = {
            DynamicLabel(
                text = field.longDescription ?: field.shortDescription ?: "",
                isMandatory = isMandatory.value
            )
        },
        suffix = {
            if (maxSymbols != null) {
                Text(
                    "${stringResource(strings.charactersLeftLabel)}: ${counter.value}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        modifier = modifier,
        singleLine = true,
        maxLines = 1,
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

            focusedPlaceholderColor = colors.steelBlue,
            unfocusedPlaceholderColor = colors.steelBlue,
            disabledPlaceholderColor = colors.transparent
        ),
        textStyle = MaterialTheme.typography.titleSmall,
        keyboardOptions = KeyboardOptions(
            keyboardType = checkNumberKeyBoard(field)
        ),
        isError = isError.value,
        supportingText = {
            Text(
                error.value ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = colors.notifyTextColor
            )
        }
    )
}

