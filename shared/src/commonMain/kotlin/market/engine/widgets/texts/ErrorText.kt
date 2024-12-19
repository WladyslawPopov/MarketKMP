package market.engine.widgets.texts

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun ErrorText(
    text : String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.notifyTextColor,
        )
    }
}
