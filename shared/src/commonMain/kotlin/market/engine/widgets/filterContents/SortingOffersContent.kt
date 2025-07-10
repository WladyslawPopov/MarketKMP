package market.engine.widgets.filterContents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOffersContent(
    initSort: Sort?,
    isCabinet: Boolean,
    modifier: Modifier = Modifier,
    onClose: (Sort?) -> Unit,
) {
    var sort by remember { mutableStateOf(initSort?.copy()) }

    val sortSections = listOf(
        stringResource(strings.timeToEnd) to listOf(
            Sort("session_end", "asc", stringResource(strings.timeToEnd) + " : " + stringResource(strings.sortModeIncreasing), null, null),
            Sort("session_end", "desc", stringResource(strings.timeToEnd) + " : " + stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.priceParameterName) to listOf(
            Sort("current_price", "asc", stringResource(strings.priceParameterName) + " : " + stringResource(strings.sortModeIncreasing), null, null),
            Sort("current_price", "desc", stringResource(strings.priceParameterName) + " : " + stringResource(strings.sortModeDecreasing), null, null)
        ),
        if (!isCabinet) {
            stringResource(strings.popularityParameterName) to listOf(
                Sort("compound_popularity", "asc", stringResource(strings.popularityParameterName) + " : " + stringResource(strings.sortModeIncreasing), null, null),
                Sort("compound_popularity", "desc",  stringResource(strings.popularityParameterName) + " : " + stringResource(strings.sortModeDecreasing), null, null)
            )
        } else {
            stringResource(strings.viewsParams) to listOf(
                Sort("views", "asc", stringResource(strings.viewsParams) + " : " + stringResource(strings.sortModeIncreasing), null, null),
                Sort("views", "desc", stringResource(strings.viewsParams) + " : " + stringResource(strings.sortModeDecreasing), null, null)
            )
        },
        stringResource(strings.numberOfBids) to listOf(
            Sort("popularity", "asc", stringResource(strings.numberOfBids) + " : " + stringResource(strings.sortModeIncreasing), null, null),
            Sort("popularity", "desc", stringResource(strings.numberOfBids) + " : " + stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.titleParameterName) to listOf(
            Sort("title", "asc", stringResource(strings.titleParameterName) + " : " + stringResource(strings.sortModeIncreasingAlphabetically), null, null),
            Sort("title", "desc", stringResource(strings.titleParameterName) + " : " + stringResource(strings.sortModeDecreasingAlphabetically), null, null)
        ),
        stringResource(strings.offersGroupStartTSTile) to listOf(
            Sort("session_start", "asc", stringResource(strings.offersGroupStartTSTile) + " : " + stringResource(strings.sortModeOldestFirst), null, null),
            Sort("session_start", "desc", stringResource(strings.offersGroupStartTSTile) + " : " + stringResource(strings.sortModeNewestFirst), null, null)
        ),
    )

    ContentSort(
        currentSort = sort,
        sortSections = sortSections,
        onClose = {
            onClose(sort)
        },
        selectItem = { newSort ->
            onClose(newSort)
        },
        onClear = {
            onClose(null)
        }
    )
}
