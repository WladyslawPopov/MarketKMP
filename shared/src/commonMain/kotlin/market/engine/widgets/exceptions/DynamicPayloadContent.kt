package market.engine.widgets.exceptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.textFields.DynamicInputField

@Composable
fun DynamicPayloadContent(
    fields: List<Fields>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        fields.forEach { field ->
            when (field.widgetType) {
                "input" -> {
                    DynamicInputField(
                        field,
                        Modifier.fillMaxWidth()
                    )
                }
                "checkbox_group" -> {
                    DynamicCheckboxGroup(
                        field,
                        Modifier.fillMaxWidth()
                    )
                }
                "select" -> {
                    DynamicSelect(
                        field,
                        Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

