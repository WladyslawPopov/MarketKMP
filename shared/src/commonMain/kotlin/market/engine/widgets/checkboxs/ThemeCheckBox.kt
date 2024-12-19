package market.engine.widgets.checkboxs

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun ThemeCheckBox(
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Checkbox(
        checked = isSelected,
        onCheckedChange = { onSelectionChange(it) },
        modifier = modifier,
        colors = CheckboxDefaults.colors(
            checkedColor = colors.inactiveBottomNavIconColor,
            uncheckedColor = colors.textA0AE,
            checkmarkColor = colors.alwaysWhite
        )
    )
}
