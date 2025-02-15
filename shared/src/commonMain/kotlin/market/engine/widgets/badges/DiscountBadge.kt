package market.engine.widgets.badges

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun DiscountBadge(
    text : String,
) {
    Card(
        colors = CardColors(
            containerColor = colors.positiveGreen,
            contentColor = colors.alwaysWhite,
            disabledContainerColor = colors.brightGreen,
            disabledContentColor = colors.alwaysWhite
        ),
        modifier = Modifier
            .padding(dimens.smallPadding)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(5.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.alwaysWhite
        )
    }
}
