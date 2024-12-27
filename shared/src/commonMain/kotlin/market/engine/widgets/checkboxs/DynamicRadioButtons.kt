package market.engine.widgets.checkboxs


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields


@OptIn(ExperimentalSerializationApi::class)
@Composable
fun DynamicRadioButtons(
    field: Fields,
){
    val list = buildMap {
        field.choices?.forEach { 
            put(it.code?.jsonPrimitive?.intOrNull ?: 0, it.name ?: "")
        }
    }

    val selectedFilterKey = remember {
        mutableStateOf(
            field.data?.jsonPrimitive?.intOrNull ?: 0
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RadioGroup(
            list.toList(),
            selectedFilterKey.value,
        ){ isChecked, choice ->
            if(!isChecked) {
                selectedFilterKey.value = choice
                field.data = JsonPrimitive(choice)
            }else{
                selectedFilterKey.value = 0
                field.data = JsonPrimitive(null)
            }
        }
    }
}
