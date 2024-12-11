package market.engine.presentation.user.feedbacks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.dimens

@Composable
fun FeedbackFilter(
    currentFilter: String,
    onFilterSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filters = listOf(
        "Все (Заказы)",
        "Положительные",
        "Негативные",
        "Нейтральные"
    )

    Box {
        Text(
            text = currentFilter,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(dimens.smallPadding)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(text = filter)
                    },
                    onClick = {
                    onFilterSelected(filter)
                    expanded = false
                })
            }
        }
    }
}
