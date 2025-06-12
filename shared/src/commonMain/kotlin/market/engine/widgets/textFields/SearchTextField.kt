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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    openSearch: Boolean,
    search : TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    goToListing: () -> Unit,
    onClearSearch: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(openSearch) {
        if (openSearch) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    TextField(
        value = search,
        onValueChange = { newValue ->
            onValueChange(newValue)
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
            if (search.text.isNotEmpty()) {
                SmallIconButton(
                    icon = drawables.cancelIcon,
                    contentDescription = stringResource(strings.actionClose),
                    color = colors.steelBlue,
                    modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                ) {
                    onClearSearch()
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
