package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.stringResource

@Composable
fun PriceFilter(
    filters: ArrayList<Filter>,
    onFiltersUpdated: () -> Unit
) {
    val priceFrom = rememberSaveable { mutableStateOf(filters.find { it.key == "current_price" && it.operation == "gte" }?.value ?: "") }
    val priceTo =  rememberSaveable { mutableStateOf(filters.find { it.key == "current_price" && it.operation == "lte" }?.value ?: "") }

    val price = stringResource(strings.priceParameterName)
    val currency = stringResource(strings.currencyCode)
    val from = stringResource(strings.fromAboutParameterName)
    val to = stringResource(strings.toAboutParameterName)

    Column(
        modifier = Modifier.fillMaxWidth(0.8f),
    ) {
        Text(
            text = price,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        Row(
            modifier = Modifier
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Price "from" TextField
            TextFieldWithState(
                label = from,
                textState = priceFrom,
                onTextChange = { newText ->
                    priceFrom.value = newText
                    if (newText.isNotBlank()) {
                        filters.find { filter -> filter.key == "current_price" && filter.operation == "gte" }?.apply {
                            value = newText
                            interpretation = "$price $from - $newText $currency"
                        }
                    } else {
                        filters.find { filter -> filter.key == "current_price" && filter.operation == "gte" }?.apply {
                            value = ""
                            interpretation = null
                        }
                    }
                    onFiltersUpdated()
                },
                modifier = Modifier.widthIn(max = 250.dp).weight(1f),
                isNumber = true
            )

            Text(
                text = "-",
                modifier = Modifier.padding(horizontal = dimens.mediumPadding),
                style = MaterialTheme.typography.bodySmall
            )

            // Price "to" TextField
            TextFieldWithState(
                label = to,
                textState = priceTo,
                onTextChange = { newText ->
                    priceTo.value = newText
                    if (newText.isNotBlank()) {
                        filters.find { filter -> filter.key == "current_price" && filter.operation == "lte" }?.apply {
                            value = newText
                            interpretation = "$price $to - $newText $currency"
                        }
                    } else {
                        filters.find { filter -> filter.key == "current_price" && filter.operation == "lte" }?.apply {
                            value = ""
                            interpretation = null
                        }
                    }
                    onFiltersUpdated()
                },
                modifier = Modifier.widthIn(max = 250.dp).weight(1f),
                isNumber = true
            )
        }
    }
}
