package market.engine.widgets.buttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.constants.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun StringButton(
    string : String,
    color : Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = {
            onClick()
        },
        modifier = modifier
    ) {
        Text(
            text = string,
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
