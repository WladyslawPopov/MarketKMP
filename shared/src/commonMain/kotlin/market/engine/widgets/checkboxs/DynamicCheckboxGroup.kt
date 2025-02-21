package market.engine.widgets.checkboxs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText

@Composable
fun DynamicCheckboxGroup(
    field: Fields,
    modifier: Modifier = Modifier
) {
    val isMandatory = remember {
        mutableStateOf(field.validators?.any { it.type == "mandatory" } == true)
    }

    val initialSelected = remember {
        val selectedCodes = mutableListOf<Int>()
        field.data?.jsonArray?.forEach { item ->
            item.jsonPrimitive.intOrNull?.let { selectedCodes.add(it) }
        }
        selectedCodes.toList()
    }

    val selectedItems = remember { mutableStateOf(initialSelected) }

    val error = remember { mutableStateOf(processInput(field.errors)) }

    val onClickListener : (Int) -> Unit = { choiceCode ->
        val currentSet = selectedItems.value.toMutableList()
        if (currentSet.contains(choiceCode)) {
            currentSet.remove(choiceCode)
        } else {
            currentSet.add(choiceCode)
        }

        selectedItems.value = currentSet.toList()
        field.data = buildJsonArray {
            selectedItems.value.forEach {
                add(JsonPrimitive(it))
            }
        }
    }

    Column(modifier = modifier) {

        DynamicLabel(
            text = field.longDescription ?: field.shortDescription.orEmpty(),
            isMandatory = isMandatory.value,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            field.choices?.forEach { choice ->
                CheckBoxRow(
                    isSelected = selectedItems.value.contains(choice.code?.intOrNull),
                    choice = choice,
                    onClickListener = onClickListener
                )
            }
        }

        if (error.value != null) {
            ErrorText(text = error.value ?: "", modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
