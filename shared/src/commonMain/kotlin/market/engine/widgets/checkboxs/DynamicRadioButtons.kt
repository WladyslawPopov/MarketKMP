package market.engine.widgets.checkboxs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields


@Composable
fun DynamicRadioButtons(
    field: Fields,
    onValueChange: (Fields) -> Unit,
){
    val list = remember(field) {
        buildMap {
            field.choices?.sortedBy { it.weight?.jsonPrimitive?.intOrNull }?.forEach {
                put(it.code?.jsonPrimitive?.intOrNull ?: 0, it.name ?: "")
            }
        }
    }

    val selectedFilterKey = remember(field.data) {
        field.data?.jsonPrimitive?.intOrNull ?: if(field.key == "feedback_type") 1 else 0
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        list.forEach {
            val moodColor = if(field.key == "feedback_type"){
                when (it.key) {
                    1 -> colors.positiveGreen
                    0-> colors.inactiveBottomNavIconColor
                    else -> colors.black
                }
            }else{
                colors.black
            }

            RadioOptionRow(
                it.toPair(),
                selectedFilterKey,
                rbColor = moodColor,
                textColor = moodColor
            ){ isChecked, choice ->
                val newField = field.copy(
                    data = if(!isChecked) {
                        JsonPrimitive(choice)
                    }else{
                        null
                    }
                )
                onValueChange(newField)
            }
        }
    }
}
