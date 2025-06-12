package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.fragments.root.main.listing.FilterBarUiState
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.rows.LazyRowWithScrollBars

@Composable
fun FiltersBar(
    uiFilterBarUiState: FilterBarUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        LazyRowWithScrollBars(
            heightMod = Modifier
                .clip(MaterialTheme.shapes.small)
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        ) {
            items(uiFilterBarUiState.listFiltersButtons, key = { it.text }) { item ->
                ActiveFilterListingItem(
                    item
                )
            }
        }

        if (uiFilterBarUiState.isShowFilters) {
            Row(
                modifier = Modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, alignment = Alignment.End)
            ) {
                uiFilterBarUiState.listNavigation.forEach{ item ->
                    if(item.isVisible){
                        BadgedButton(item)
                    }
                }
            }
        }
    }
}

