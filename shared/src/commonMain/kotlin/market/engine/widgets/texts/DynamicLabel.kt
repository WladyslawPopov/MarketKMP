package market.engine.widgets.texts

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun DynamicLabel(
    text : String,
    isMandatory : Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium
) {
    val htmlText = rememberRichTextState()

    val annotatedString = remember {
        buildAnnotatedString {
            val mainText = htmlText.setHtml(text).annotatedString.text
            append(mainText)
            if (isMandatory) {
                withStyle(SpanStyle(color = colors.negativeRed)){
                    append(" *")
                }
            }
        }
    }

    Row(
        modifier = modifier
    ) {
        Text(
            annotatedString,
            style = style,
            color = colors.black
        )
    }
}
