package market.engine.widgets.checkboxs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText

@Composable
fun DynamicCheckbox(
    field: Fields,
    modifier: Modifier = Modifier
) {
    val isMandatory = remember {
        mutableStateOf(field.validators?.any { it.type == "mandatory" } == true)
    }

    val initialSelected = remember { mutableStateOf(field.data?.jsonPrimitive?.intOrNull ?: 0) }

    val error = remember { mutableStateOf(processInput(field.errors)) }

    val onClickListener : (Boolean) -> Unit = {
        initialSelected.value = if (it) 1 else 0
        field.data = JsonPrimitive(if (it) 1 else 0)
    }

    Column(modifier = modifier) {
        // A single checkbox row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClickListener(initialSelected.value != 0)
                }
        ) {
            ThemeCheckBox(
                isSelected = initialSelected.value != 0,
                onSelectionChange = {
                    onClickListener(it)
                },
                Modifier
            )

            DynamicLabel(
                text = field.longDescription ?: field.shortDescription ?: "",
                isMandatory = isMandatory.value
            )
        }

        if (error.value != null) {
            ErrorText(text = error.value ?: "", modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
