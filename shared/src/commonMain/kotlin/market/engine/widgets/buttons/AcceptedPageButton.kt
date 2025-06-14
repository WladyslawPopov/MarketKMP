package market.engine.widgets.buttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun AcceptedPageButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor : Color = colors.inactiveBottomNavIconColor,
    onClick: () -> Unit,
) {

    TextButton(
        onClick = {
            onClick()
        },
        colors = colors.themeButtonColors.copy(containerColor = containerColor),
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        enabled = enabled
    ){
        Text(
            text = text,
            color = colors.alwaysWhite,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
