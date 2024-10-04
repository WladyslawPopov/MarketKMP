package market.engine.widgets.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens

@Composable
fun TitleText(text : String,modifier: Modifier = Modifier, onClick: (() -> Unit) = {}) {
    Text(
        text = text,
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        color = colors.black,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier.padding(horizontal = dimens.smallPadding)
            .clickable {
                onClick()
            }
    )
}
