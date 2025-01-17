package market.engine.widgets.textFields

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun TextFieldWithState(
    label: String,
    textState: MutableState<String>,
    modifier: Modifier,
    isNumber: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    onTextChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false
) {
    TextField(
        value = textState.value,
        onValueChange = {
            textState.value = it
            onTextChange(it)
        },
        label = { Text(label, style = textStyle) },
        modifier = modifier.wrapContentSize().padding(dimens.smallPadding),
        singleLine = true,
        maxLines = 1,
        readOnly = readOnly,
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
        leadingIcon = leadingIcon,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(
            keyboardType = if(isNumber) KeyboardType.Number else KeyboardType.Text
        )
    )
}
