package market.engine.widgets.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = dimens.largeText,
    alignment: Alignment? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = alignment ?: Alignment.Center
    ) {
        TextButton(
            onClick = {
                onClick()
            },
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.transparentGrayColor,
                contentColor = colors.actionTextColor,
                disabledContainerColor = colors.grayLayout,
                disabledContentColor = colors.steelBlue
            ),
            shape = MaterialTheme.shapes.small
        ){
            Text(
                text = text,
                fontSize = fontSize,
                color = if (enabled) colors.actionTextColor else colors.steelBlue,
            )
        }
    }
}
