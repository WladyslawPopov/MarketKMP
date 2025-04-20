package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Choices
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicSelect(
    field: Fields,
    modifier: Modifier = Modifier,
    itemClick: ((Choices?) -> Unit)? = null
) {
    val selectDef = stringResource(strings.chooseAction)

    val isMandatory = remember {
        mutableStateOf(
            field.validators?.find { it.type == "mandatory" } != null
        )
    }

    val textSelect = remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit){
        val data = field.data?.jsonPrimitive?.intOrNull
        if (data != null){
            if (itemClick != null) {
                itemClick(field.choices?.find { it.code?.intOrNull == data })
            }
        }

        val name = field.choices?.find { choice->
            (choice.code?.intOrNull) == data
        }?.name ?: selectDef

        textSelect.value = name
    }

    val error = processInput(field.errors)

    Column(
        modifier = modifier
    ){
        DynamicLabel(
            text = field.shortDescription ?: field.longDescription ?: "" ,
            isMandatory = isMandatory.value,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        getDropdownMenu(
            selectedText = textSelect.value,
            selects = field.choices?.map { it.name ?: "" } ?: emptyList(),
            onItemClick = { item ->
                val choice = field.choices?.find { it.name == item }
                field.data = choice?.code
                textSelect.value = item
                itemClick?.invoke(choice)
            },
            onClearItem = {
                field.data = null
                textSelect.value = selectDef
                itemClick?.invoke(null)
            }
        )

        if (error != null){
            ErrorText(
                text = error,
                modifier = Modifier.padding(dimens.smallPadding)
            )
        }
    }
}
