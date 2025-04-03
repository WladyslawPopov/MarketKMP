package market.engine.widgets.filterContents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.MutableState
import market.engine.core.data.baseFilters.LD
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOffersContent(
    isRefreshing: MutableState<Boolean>,
    listingData: LD,
    onClose: () -> Unit,
) {
    val sortSections = listOf(
        stringResource(strings.timeToEnd) to listOf(
            Sort("session_end", "asc", stringResource(strings.sortModeIncreasing), null, null),
            Sort("session_end", "desc", stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.priceParameterName) to listOf(
            Sort("current_price", "asc", stringResource(strings.sortModeIncreasing), null, null),
            Sort("current_price", "desc", stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.numberOfBids) to listOf(
            Sort("popularity", "asc", stringResource(strings.sortModeIncreasing), null, null),
            Sort("popularity", "desc", stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.titleParameterName) to listOf(
            Sort("title", "asc", stringResource(strings.sortModeIncreasingAlphabetically), null, null),
            Sort("title", "desc", stringResource(strings.sortModeDecreasingAlphabetically), null, null)
        ),
        stringResource(strings.offersGroupStartTSTile) to listOf(
            Sort("session_start", "asc", stringResource(strings.sortModeOldestFirst), null, null),
            Sort("session_start", "desc", stringResource(strings.sortModeNewestFirst), null, null)
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(dimens.smallPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        FilterContentHeaderBar(
            stringResource(strings.sort),
            listingData.sort != null,
            onClosed = onClose,
            onClear = {
                isRefreshing.value = true
                listingData.sort = null
                onClose()
            }
        )

        LazyColumnWithScrollBars(
            heightMod = Modifier.fillMaxWidth(if (isBigScreen.value) 0.7f else 1f).padding(top = 60.dp),
            modifierList = Modifier,
        ) {
            // For each section, display the heading and options under it
            sortSections.forEach { (sectionTitle, sortOptions) ->
                item {
                    // Section title
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(dimens.smallPadding),
                        color = colors.textA0AE
                    )
                }

                items(sortOptions) { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isRefreshing.value = true
                                listingData.sort = sortOption
                                onClose()
                            }
                            .background(colors.white)
                            .clip(MaterialTheme.shapes.small)
                            .padding(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = sortOption.interpretation ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(dimens.smallPadding),
                            color = colors.black
                        )

                        if (listingData.sort?.key == sortOption.key && listingData.sort?.value == sortOption.value) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.inactiveBottomNavIconColor
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp), color = colors.textA0AE)
                }
            }
        }
    }
}
