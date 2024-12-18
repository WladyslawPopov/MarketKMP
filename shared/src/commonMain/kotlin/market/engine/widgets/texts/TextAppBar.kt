package market.engine.widgets.texts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun TextAppBar(
    text : String,
    color : Color = colors.black,
    modifier: Modifier = Modifier
) {
    val title = if(text.length > 20){
        text.substring(0,20) + "..."
    }else{
        text
    }

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier,
        maxLines = 1,
        softWrap = true
    )
}
