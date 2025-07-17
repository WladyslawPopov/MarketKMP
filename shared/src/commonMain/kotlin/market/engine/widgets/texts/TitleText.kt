package market.engine.widgets.texts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun TitleText(
    text : String,
    color : Color = colors.black,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier,
        maxLines = 3,
        softWrap = true
    )
}
