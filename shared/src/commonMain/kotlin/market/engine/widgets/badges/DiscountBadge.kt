package market.engine.widgets.badges

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens

@Composable
fun DiscountBadge(
    text : String,
) {
    Card(
        colors = CardColors(
            containerColor = colors.positiveGreen,
            contentColor = colors.alwaysWhite,
            disabledContainerColor = colors.greenColor,
            disabledContentColor = colors.alwaysWhite
        ),
        modifier = Modifier
            .padding(dimens.smallPadding)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(5.dp),
            style = MaterialTheme.typography.bodySmall,
            color = colors.alwaysWhite
        )
    }
}
