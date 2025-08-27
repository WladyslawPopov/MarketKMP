package market.engine.widgets.checkboxs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.texts.ErrorText

@Composable
fun DynamicCheckbox(
    field: Fields,
    modifier: Modifier = Modifier,
    onValueChange: (Fields) -> Unit
) {
    val isMandatory = remember(field.validators) {
        field.validators?.any { it.type == "mandatory" } == true
    }

    val initialSelected = remember(field.data) {
        field.data?.jsonPrimitive
    }

    val error = remember(field.errors) { processInput(field.errors) }

    val onClickListener : (JsonPrimitive?) -> Unit = remember(initialSelected) {
        { select ->
            onValueChange(field.copy(
                data = if(initialSelected == null)
                    select ?:
                    if(field.validators?.firstOrNull()?.type == "boolean") JsonPrimitive(true)
                    else JsonPrimitive( 0)
                else null
            ))
        }
    }

    val choices = remember(field) {
        Choices(
            name = field.shortDescription ?: field.longDescription ?: "",
            code = field.data?.jsonPrimitive,
        )
    }

    Column(modifier = modifier) {
        CheckBoxRow(
            isSelected = initialSelected != null,
            choice = choices,
            isMandatory = isMandatory,
        ){
            onClickListener(it)
        }

        if (error != null) {
            ErrorText(text = error, modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
