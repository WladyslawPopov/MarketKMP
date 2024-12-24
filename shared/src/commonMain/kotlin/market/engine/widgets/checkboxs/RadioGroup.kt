package market.engine.widgets.checkboxs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun <T : Any>RadioGroup(
    options:  List<Pair<T, String>>,
    selectedOption: T?,
    onOptionSelected: (Boolean, T) -> Unit
) {
    Column(
        modifier = Modifier.padding(dimens.mediumPadding)
    ) {
        options.forEach { filter ->
            val (filterKey, filterText) = filter
            val isChecked = selectedOption == filterKey
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = { onOptionSelected(isChecked, filterKey) })
            ) {
                RadioButton(
                    isChecked,
                    {
                        onOptionSelected(isChecked, filterKey)
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = colors.inactiveBottomNavIconColor,
                        unselectedColor = colors.black
                    )
                )
                Spacer(modifier = Modifier.width(dimens.smallPadding))
                Text(
                    filterText,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(dimens.smallPadding))
            }
        }
    }
}
