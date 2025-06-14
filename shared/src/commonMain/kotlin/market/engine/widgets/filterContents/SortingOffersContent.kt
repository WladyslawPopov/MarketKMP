package market.engine.widgets.filterContents

import market.engine.core.data.baseFilters.LD
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOffersContent(
    listingData: LD,
    isCabinet: Boolean,
    onClose: (Boolean) -> Unit,
) {
    val isRefreshing = remember { mutableStateOf(false) }

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
        currentSort = listingData.sort,
        sortSections = sortSections,
        onClose = {
            onClose(isRefreshing.value)
        },
        selectItem = {
            isRefreshing.value = true
            listingData.sort = it
        },
        onClear = {
            isRefreshing.value = true
            listingData.sort = null
        }
    )
}
