package market.engine.widgets.checkboxs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.texts.ErrorText

@Composable
fun DynamicCheckbox(
    field: Fields,
    modifier: Modifier = Modifier
) {
    val isMandatory = remember {
        mutableStateOf(field.validators?.any { it.type == "mandatory" } == true)
    }

    val initialSelected = remember {
        mutableStateOf(field.data?.jsonPrimitive?.booleanOrNull ?: false) }

    val error = remember { mutableStateOf(processInput(field.errors)) }

    val onClickListener : (Int) -> Unit = {
        initialSelected.value = field.data?.jsonPrimitive?.booleanOrNull != true
        field.data = JsonPrimitive(initialSelected.value)
    }


    Column(modifier = modifier) {
        val choices = Choices(
            name = field.longDescription ?: field.shortDescription ?: "",
            code = JsonPrimitive(0)
        )

        CheckBoxRow(
            isSelected = initialSelected.value,
            choice = choices,
            isMandatory = isMandatory.value,
            onClickListener = onClickListener
        )

        if (error.value != null) {
            ErrorText(text = error.value ?: "", modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
