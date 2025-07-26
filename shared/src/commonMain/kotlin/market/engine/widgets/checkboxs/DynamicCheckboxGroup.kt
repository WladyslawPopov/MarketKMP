package market.engine.widgets.checkboxs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText

@Composable
fun DynamicCheckboxGroup(
    field: Fields,
    showRating : Boolean = false,
    modifier: Modifier = Modifier,
    onValueChange: (Fields) -> Unit,
) {
    val isMandatory = remember(field.validators) {
        field.validators?.any { it.type == "mandatory" } == true
    }

    val initialSelected = remember(field.data) {
        val selectedCodes = mutableListOf<Long>()

        try {
            field.data?.jsonArray?.forEach { item ->
                item.jsonPrimitive.longOrNull?.let { selectedCodes.add(it) }
            }
        } catch (_ : Exception){

        }

        selectedCodes.toList()
    }

    val error = remember(field) { processInput(field.errors) }

    val onClickListener : (Long) -> Unit = { choiceCode ->
        val currentSet = initialSelected.toMutableList()
        if (currentSet.contains(choiceCode)) {
            currentSet.remove(choiceCode)
        } else {
            currentSet.add(choiceCode)
        }

        onValueChange(field.copy(
            data = buildJsonArray {
                currentSet.forEach {
                    add(JsonPrimitive(it))
                }
            }
        ))
    }

    Column(modifier = modifier) {

        DynamicLabel(
            text = field.longDescription ?: field.shortDescription.orEmpty(),
            isMandatory = isMandatory,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            field.choices?.forEach { choice ->
                CheckBoxRow(
                    isSelected = initialSelected.contains(choice.code?.longOrNull),
                    choice = choice,
                    showRating = showRating,
                    onClickListener = onClickListener
                )

                choice.extendedFields?.let { extendedField ->
                    SetUpDynamicFields(fields = extendedField){
                        onValueChange(it)
                    }
                }
            }
        }

        if (error != null) {
            ErrorText(text = error, modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
