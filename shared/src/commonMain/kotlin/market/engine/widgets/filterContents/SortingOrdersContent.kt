package market.engine.widgets.filterContents

import androidx.compose.runtime.MutableState
import market.engine.core.data.baseFilters.LD
import androidx.compose.runtime.Composable
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOrdersContent(
    isRefreshing: MutableState<Boolean>,
    listingData: LD,
    onClose: () -> Unit,
) {
    val sortSections = listOf(
        stringResource(strings.sortCreationDate) to listOf(
            Sort("created_ts", "asc", stringResource(strings.sortCreationDate) + stringResource(strings.sortModeOldestFirst), null, null),
            Sort("created_ts", "desc", stringResource(strings.sortCreationDate) + stringResource(strings.sortModeNewestFirst), null, null)
        ),
    )

    ContentSort(
        currentSort = listingData.sort,
        sortSections = sortSections,
        selectItem = {
            isRefreshing.value = true
            listingData.sort = it
        },
        onClear = {
            isRefreshing.value = true
            listingData.sort = null
        },
        onClose = onClose
    )
}
