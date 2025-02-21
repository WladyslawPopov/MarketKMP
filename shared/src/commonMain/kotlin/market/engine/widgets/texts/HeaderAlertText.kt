package market.engine.widgets.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun HeaderAlertText(
    text : AnnotatedString,
    color : Color = colors.grayText,
) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.background(
            color = colors.solidGreen,
            shape = MaterialTheme.shapes.medium
        ).padding(dimens.mediumPadding)
    )
}
