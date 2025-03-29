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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTextField(
    openSearch: MutableState<Boolean>,
    search : MutableState<TextFieldValue>,
    onUpdateHistory: (String) -> Unit,
    goToListing: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(openSearch.value) {
        if (openSearch.value) {
            if (search.value.text.isNotEmpty()) {
                search.value = search.value.copy(
                    selection = TextRange(search.value.text.length)
                )
            }
            delay(300)
            focusRequester.requestFocus()
        }
    }

    TextField(
        value = search.value,
        onValueChange = { newValue ->
            search.value = newValue
            onUpdateHistory(newValue.text)
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
            imeAction = ImeAction.Search,
            capitalization = KeyboardCapitalization.Sentences
        ),
        singleLine = true,
        keyboardActions = KeyboardActions(
            onSearch = {
                goToListing()
            },
        ),
        trailingIcon = {
            if (search.value.text.isNotEmpty()) {
                SmallIconButton(
                    icon = drawables.cancelIcon,
                    contentDescription = stringResource(strings.actionClose),
                    color = colors.steelBlue,
                    modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                ) {
                    search.value = TextFieldValue()
                    onUpdateHistory("")
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

            focusedPlaceholderColor = colors.grayText,
            unfocusedPlaceholderColor = colors.grayText,
            disabledPlaceholderColor = colors.transparent
        ),
    )
}
