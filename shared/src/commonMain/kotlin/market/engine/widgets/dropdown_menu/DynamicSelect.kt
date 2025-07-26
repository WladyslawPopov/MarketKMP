package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.processInput
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicSelect(
    field: Fields,
    modifier: Modifier = Modifier,
    onValueChange: (Fields) -> Unit,
) {
    val selectDef = stringResource(strings.chooseAction)

    val isMandatory = remember(field.validators) {
        field.validators?.find { it.type == "mandatory" } != null
    }

    val textSelect = remember(field.data) {
        val data = field.data
        field.choices?.find { choice->
            choice.code == data
        }?.name ?: selectDef
    }

    val error = processInput(field.errors)

    Column(
        modifier = modifier
    ){
        DynamicLabel(
            text = field.shortDescription ?: field.longDescription ?: "" ,
            isMandatory = isMandatory,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        getDropdownMenu(
            selectedText = textSelect,
            selects = field.choices?.map { it.name ?: "" } ?: emptyList(),
            onItemClick = { item ->
                val newField = field.copy(
                    data = field.choices?.find { it.name == item }?.code
                )
                onValueChange(newField)
            },
            onClearItem = {
                val newField = field.copy(
                    data = null
                )
                onValueChange(newField)
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
