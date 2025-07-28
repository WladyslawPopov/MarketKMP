package market.engine.widgets.filterContents.sorts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.filterContents.ContentSort
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOrdersContent(
    initSort: Sort?,
    modifier: Modifier = Modifier,
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
        modifier = modifier,
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
