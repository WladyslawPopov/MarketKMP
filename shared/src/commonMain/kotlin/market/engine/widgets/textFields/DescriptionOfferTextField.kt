package market.engine.widgets.textFields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.bars.RichTextStyleBar
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescriptionTextField(
    field: Fields,
    richTextState: RichTextState
) {
    val errorState = rememberUpdatedState(field.errors)

    val isMandatory = remember {
        mutableStateOf(
            (field.validators?.find { it.type == "mandatory" }) != null
        )
    }

    Column(
        modifier = Modifier.background(
            colors.white,
            MaterialTheme.shapes.small
        )
            .clip(MaterialTheme.shapes.small).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RichTextStyleBar(
            modifier = Modifier
                .fillMaxWidth(),
            state = richTextState,
        )

        Spacer(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.98f)
                .background(colors.grayLayout)
        )

        RichTextEditor(
            state = richTextState,
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 500.dp),
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DynamicLabel(
                        text = field.shortDescription ?: field.longDescription ?:  "",
                        isMandatory = isMandatory.value
                    )
                }
            },
            placeholder = {
                Text(
                    stringResource(strings.descriptionPlaceholderLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayText
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences
            ),
            colors = RichTextEditorDefaults.richTextEditorColors(
                focusedIndicatorColor = colors.transparent,
                unfocusedIndicatorColor = colors.transparent,
                disabledIndicatorColor = colors.transparent,
                errorIndicatorColor = colors.transparent,
            ),
            supportingText = {
                ErrorText(
                    processInput(errorState.value) ?: "",
                    modifier = Modifier.padding(dimens.smallPadding)
                )
            }
        )
    }
}
