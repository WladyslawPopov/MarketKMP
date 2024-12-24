package market.engine.widgets.texts

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun DynamicLabel(
    text : String,
    isMandatory : Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium
) {
    val label = buildAnnotatedString {
        append(text)
        if (isMandatory) {
            withStyle(SpanStyle(color = colors.notifyTextColor)){
                append(" *")
            }
        }
    }
    Row(
        modifier = modifier
    ) {
        Text(
            label,
            style = style
        )
    }
}
