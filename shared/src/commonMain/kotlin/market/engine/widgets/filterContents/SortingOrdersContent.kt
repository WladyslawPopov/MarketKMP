package market.engine.widgets.filterContents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOrdersContent(
    initSort: Sort?,
    onClose: (Sort?) -> Unit,
) {
    var sort by remember { mutableStateOf(initSort?.copy()) }

    val sortSections = listOf(
        stringResource(strings.sortCreationDate) to listOf(
            Sort("created_ts", "asc", stringResource(strings.sortCreationDate) + stringResource(strings.sortModeOldestFirst), null, null),
            Sort("created_ts", "desc", stringResource(strings.sortCreationDate) + stringResource(strings.sortModeNewestFirst), null, null)
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
