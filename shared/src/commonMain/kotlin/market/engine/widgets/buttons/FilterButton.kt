package market.engine.widgets.buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.dimens

@Composable
fun FilterButton(
    text: String,
    color: ButtonColors,
    fontSize: TextUnit = MaterialTheme.typography.bodySmall.fontSize,
    onClick: () -> Unit,
    onCancelClick: @Composable () -> Unit = @Composable {},
) {
    TextButton(
        onClick = { onClick() },
        colors = color,
    ) {
        Row(
            modifier = Modifier.wrapContentWidth().padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = fontSize,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 150.dp)
                    .padding(end = 2.dp)
            )

            onCancelClick()
        }
    }
}
