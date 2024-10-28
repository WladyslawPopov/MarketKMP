package market.engine.widgets.items

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.baseFilters.LD
import org.jetbrains.compose.resources.stringResource

@Composable
fun PriceFilter(
    listingData: State<LD>,
    onFiltersUpdated: () -> Unit
) {
    val focusManager: FocusManager = LocalFocusManager.current
    val filters = listingData.value.filters

    var priceFrom by rememberSaveable { mutableStateOf(filters?.find { it.key == "current_price" && it.operation == "gte" }?.value ?: "") }
    var priceTo by rememberSaveable { mutableStateOf(filters?.find { it.key == "current_price" && it.operation == "lte" }?.value ?: "") }

    val price = stringResource(strings.priceParameterName)
    val currency = stringResource(strings.currencyCode)
    val from = stringResource(strings.fromAboutParameterName)
    val to = stringResource(strings.toAboutParameterName)

    Column(
        modifier = Modifier
            .padding(dimens.mediumPadding)
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
    ) {
        Text(
            text = price,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        Row(
            modifier = Modifier
                .padding(horizontal = dimens.mediumPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Price "from" TextField
            TextField(
                value = priceFrom,
                onValueChange = { newText ->
                    priceFrom = newText
                    if (newText.isNotBlank()) {
                        filters?.find { filter -> filter.key == "current_price" && filter.operation == "gte" }?.apply {
                            value = newText
                            interpritation = "$price $from - $newText $currency"
                        }
                    } else {
                        filters?.find { filter -> filter.key == "current_price" && filter.operation == "gte" }?.apply {
                            value = ""
                            interpritation = null
                        }
                    }
                    onFiltersUpdated()
                },
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        from,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(
                text = "-",
                modifier = Modifier.padding(horizontal = dimens.mediumPadding),
                style = MaterialTheme.typography.bodySmall
            )

            // Price "to" TextField
            TextField(
                value = priceTo,
                onValueChange = { newText ->
                    priceTo = newText
                    if (newText.isNotBlank()) {
                        filters?.find { filter -> filter.key == "current_price" && filter.operation == "lte" }?.apply {
                            value = newText
                            interpritation = "$price $to - $newText $currency"
                        }
                    } else {
                        filters?.find { filter -> filter.key == "current_price" && filter.operation == "lte" }?.apply {
                            value = ""
                            interpritation = null
                        }
                    }
                    onFiltersUpdated()
                },
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        to,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
