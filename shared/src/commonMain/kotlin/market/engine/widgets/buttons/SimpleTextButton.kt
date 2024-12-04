package market.engine.widgets.buttons

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import market.engine.core.globalData.ThemeResources.colors

@Composable
fun SimpleTextButton(
    text: String,
    backgroundColor: Color = colors.white,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    textColor: Color = colors.black,
    textStyle : TextStyle = MaterialTheme.typography.titleSmall,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.textButtonColors(
            containerColor = backgroundColor
        ),
        shape = shape,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}
