package market.engine.widgets.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens

@Composable
fun TitleText(text : String) {
    Text(
        text = text,
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        color = colors.black,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = dimens.smallPadding)
    )
}
