package market.engine.widgets.checkboxs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.longOrNull
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Choices
import market.engine.widgets.texts.DynamicLabel

@Composable
fun CheckBoxRow(
    isSelected: Boolean,
    choice : Choices,
    showRating : Boolean = false,
    isMandatory : Boolean = false,
    onClickListener: (Long) -> Unit,
) {
    val choiceCode = remember(choice) { choice.code?.longOrNull ?: 0 }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        modifier = Modifier
            .clickable {
                onClickListener(choiceCode)
            }
            .fillMaxWidth()
    ) {
        ThemeCheckBox(
            isSelected = isSelected,
            onSelectionChange = {
                onClickListener(choiceCode)
            },
            modifier = Modifier
        )

        DynamicLabel(
            text = if(showRating)"${choice.name} (${choice.weight})" else choice.name.orEmpty(),
            modifier = Modifier.fillMaxWidth(0.85f),
            isMandatory = isMandatory
        )
    }
}
