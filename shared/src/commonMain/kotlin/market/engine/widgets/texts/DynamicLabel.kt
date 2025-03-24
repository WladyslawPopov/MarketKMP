package market.engine.widgets.texts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.common.openUrl
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun DynamicLabel(
    text : String,
    isMandatory : Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium
) {
    val htmlText = rememberRichTextState()
    var url = remember { "" }

    val annotatedString = remember {
        buildAnnotatedString {
            append(htmlText.setHtml(text).annotatedString.text)
            if (isMandatory) {
                withStyle(SpanStyle(color = colors.negativeRed)){
                    append(" *")
                }
            }
            val urlPattern = Regex("${SAPI.SERVER_BASE}[\\w./?=#-]+")
            urlPattern.findAll(text).forEach { matchResult ->
                url = matchResult.value
                val start = matchResult.range.first
                val end = matchResult.range.last + 1

                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = start,
                    end = end
                )

                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = colors.brightBlue
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }
    val modClick = remember {
        if (url.isNotBlank()) {
            Modifier.clickable {
                openUrl(url)
            }
        }else{
            Modifier
        }
    }

    Row(
        modifier = modifier
    ) {
        Text(
            annotatedString,
            style = style,
            modifier = modClick
        )
    }
}
