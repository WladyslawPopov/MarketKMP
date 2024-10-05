package market.engine.widgets.textFields

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.SD
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    searchString : String,
    focusRequester: FocusRequester,
    onUpdateHistory: (String) -> Unit,
    onBeakClick: () -> Unit,
){
   var search = searchString
    TextField(
        value = search,
        onValueChange = {
            search = it
            onUpdateHistory(search)
        },
        placeholder = {
            Text(
                text = stringResource(strings.selectSearchTitle),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = modifier.clip(MaterialTheme.shapes.small)
            .wrapContentSize()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        singleLine = true,
        keyboardActions = KeyboardActions(
            onSearch = {
                onBeakClick()
            }
        ),
        trailingIcon = {
            if (searchString != "") {

                SmallIconButton(
                    icon = drawables.cancelIcon,
                    contentDescription = stringResource(strings.actionClose),
                    color = colors.steelBlue,
                    modifier = modifier,
                ){
                    search = ""
                    onUpdateHistory(search)
                }
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors =  TextFieldDefaults.colors(
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
