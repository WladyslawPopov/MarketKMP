package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.LD
import market.engine.core.globalData.SD
import market.engine.core.items.NavigationItem
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ListingFiltersBar(
    listingData: State<LD>,
    searchData: State<SD>,
    isShowFilters: Boolean = true,
    onRefresh: () -> Unit,
) {
    val isShowFiltersTittle = remember { mutableStateOf( "") }

    Column {
        if (isShowFiltersTittle.value != "") {
            Box(
                modifier = Modifier.padding(top = dimens.smallPadding, start = dimens.mediumPadding)
            ) {
                Text(
                    text = isShowFiltersTittle.value,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.black,
                )
            }
        }
        val filterString = stringResource(strings.filter)
        val searchString = stringResource(strings.searchTitle)
        val  userDef = stringResource(strings.searchUsersSearch)

        Row(
            modifier = Modifier
                .fillMaxWidth().padding(start = dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(if (isShowFilters) 0.7f else 1f).clip(shape = MaterialTheme.shapes.medium),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listingData.value.filters
                if (filters != null) {
                    if (filters.isNotEmpty()){
                        isShowFiltersTittle.value = filterString
                    }

                    items(filters.toList()) { filter ->

                    }
                }
                if (searchData.value.userSearch) {
                    if (searchData.value.userLogin != null) {
                        isShowFiltersTittle.value = searchString
                        item {
                            ActiveFilterListing(
                                text = searchData.value.userLogin ?: userDef,
                                removeFilter = {
                                    searchData.value.userLogin = null
                                    searchData.value.userSearch = false
                                    searchData.value.searchFinished = false
                                    onRefresh()
                                }
                            )
                        }
                    }
                }

                if (searchData.value.searchString?.isNotEmpty() == true) {
                    isShowFiltersTittle.value = searchString
                    item {
                        ActiveFilterListing(
                            text = searchData.value.searchString ?: searchString,
                            removeFilter = {
                                searchData.value.searchString = ""
                                onRefresh()
                            }
                        )
                    }
                }

                if (searchData.value.searchFinished){
                    isShowFiltersTittle.value = searchString
                    item {
                        ActiveFilterListing(
                            text = stringResource(strings.searchUserFinishedStringChoice),
                            removeFilter = {
                                searchData.value.searchFinished = false
                                onRefresh()
                            }
                        )
                    }
                }
            }
            if (isShowFilters) {
                val itemFilter = NavigationItem(
                    title = stringResource(strings.filter),
                    icon = drawables.filterIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = 5
                )

                val itemSort = NavigationItem(
                    title = stringResource(strings.sort),
                    icon = drawables.sortIcon,
                    tint = colors.black,
                    hasNews = false,
                    badgeCount = null
                )
                Row(
                    modifier = Modifier,
                ) {
                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            { }
                        }
                    ) {
                        getBadgedBox(item = itemFilter)
                    }

                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            { }
                        }
                    ) {
                        getBadgedBox(item = itemSort)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFilterListing(
    text : String,
    removeFilter : () -> Unit,
){
    FilterChip(
        modifier = Modifier.padding(horizontal = dimens.extraSmallPadding),
        selected = false,
        onClick = { },
        label = {
            Row(
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        trailingIcon = {
            SmallIconButton(
                drawables.cancelIcon,
                color = colors.black,
                modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                modifier = Modifier
            ) {
                removeFilter()
            }
        },
        border = null,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.white,
            labelColor = colors.black,
            selectedContainerColor = colors.selected,
            selectedLabelColor = colors.black
        )
    )
}
