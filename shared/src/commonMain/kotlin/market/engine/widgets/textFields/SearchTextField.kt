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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTextField(
    search : MutableState<String>,
    focusRequester: FocusRequester,
    onUpdateHistory: (String) -> Unit,
    goToListing: () -> Unit,
){
    TextField(
        value = search.value,
        onValueChange = {
            search.value = it
            onUpdateHistory(it)
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
            if (search.value.isNotEmpty()) {
                SmallIconButton(
                    icon = drawables.cancelIcon,
                    contentDescription = stringResource(strings.actionClose),
                    color = colors.steelBlue,
                    modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                ) {
                    search.value = ""
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
