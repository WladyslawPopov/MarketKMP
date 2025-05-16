package market.engine.widgets.texts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextDecoration
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun DiscountText(
    price: String,
) {
    Text(
        text = price + " ${stringResource(strings.currencySign)}",
        style = MaterialTheme.typography.titleMedium,
        color = colors.positiveGreen,
        textDecoration = TextDecoration.LineThrough
    )
}

