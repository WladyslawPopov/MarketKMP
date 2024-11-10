package market.engine.widgets.texts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens

@Composable
fun TitleText(
    text : String,
    color : Color = colors.black,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var title = text
    if (text.length > 30){
       title = text.substring(0,30) + "..."
    }

    Text(
        text = title,
        fontSize = if (text.length > 28) MaterialTheme.typography.bodySmall.fontSize
                    else MaterialTheme.typography.titleMedium.fontSize,
        color = color,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier.padding(horizontal = dimens.smallPadding)
            .clickable(
                enabled = onClick != null
            ) {
                if (onClick != null) {
                    onClick()
                }
            },
        softWrap = false
    )
}
