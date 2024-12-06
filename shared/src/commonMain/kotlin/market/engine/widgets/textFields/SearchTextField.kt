package market.engine.widgets.textFields

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTextField(
    search : TextFieldValue,
    focusRequester: FocusRequester,
    onUpdateHistory: (String) -> Unit,
    goToListing: () -> Unit,
){
    var textState by remember { mutableStateOf(search) }

    val currentSearchState by rememberUpdatedState(newValue = search)

    LaunchedEffect(currentSearchState) {
        textState = currentSearchState.copy(selection = TextRange(currentSearchState.text.length))
    }

    TextField(
        value = textState,
        onValueChange = {
            textState = it.copy(selection = TextRange(it.text.length))
            onUpdateHistory(it.text)
        },
        placeholder = {
            Text(
                text = stringResource(strings.selectSearchTitle),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .wrapContentSize()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        singleLine = true,
        keyboardActions = KeyboardActions(
            onSearch = {
                goToListing()
            }
        ),
        trailingIcon = {
            if (textState.text.isNotEmpty()) {
                SmallIconButton(
                    icon = drawables.cancelIcon,
                    contentDescription = stringResource(strings.actionClose),
                    color = colors.steelBlue,
                    modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                ) {
                    textState = TextFieldValue("")  // Очищаем текст
                    onUpdateHistory(textState.text)
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium,
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
    )
}
