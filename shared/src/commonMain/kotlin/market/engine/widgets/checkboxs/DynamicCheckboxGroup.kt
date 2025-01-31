package market.engine.widgets.checkboxs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
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
                val choiceCode = choice.code?.intOrNull ?: 0

                // A single checkbox row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClickListener(choiceCode)
                        }
                ) {
                    ThemeCheckBox(
                        isSelected = selectedItems.value.contains(choiceCode),
                        onSelectionChange = {
                            onClickListener(choiceCode)
                        },
                        Modifier
                    )

                    Text(
                        text = choice.name.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black,
                        modifier = Modifier.padding(start = dimens.smallPadding).fillMaxWidth(0.5f)
                    )
                }
            }
        }

        if (error.value != null) {
            ErrorText(text = error.value ?: "", modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
