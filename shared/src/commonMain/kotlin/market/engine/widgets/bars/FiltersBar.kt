package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.rows.LazyRowWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersBar(
    searchData: SD,
    listingData: LD,
    updateTrigger : Int,
    isShowFilters: Boolean = true,
    isShowGrid: Boolean = false,
    onChangeTypeList: (Int) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    if (updateTrigger < 0) return

    val filters = listingData.filters.filter { it.interpretation != "" && it.interpretation != null }
    val userDef = stringResource(strings.searchUsersSearch)

    val itemFilter = NavigationItem(
        title = stringResource(strings.filter),
        icon = drawables.filterIcon,
        tint = colors.black,
        hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
        badgeCount = if(filters.isNotEmpty()) filters.size else null,
        onClick = {
            onFilterClick()
        }
    )

    val itemSort = NavigationItem(
        title = stringResource(strings.sort),
        icon = drawables.sortIcon,
        tint = colors.black,
        hasNews = listingData.sort != null,
        badgeCount = null,
        onClick = {
            onSortClick()
        }
    )

    val itemGallery = if (isShowGrid)
        NavigationItem(
            title = stringResource(strings.menuTitle),
            icon = if (listingData.listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            isVisible = true,
            onClick = {
                val newType = if (listingData.listingType == 0) 1 else 0
                onChangeTypeList(newType)
            }
        )
    else null

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
            items(filters, key = { it.interpretation ?: it.value }) { filter ->
                filter.interpretation?.let { text->
                    ActiveFilterListingItem(
                        text = text,
                        removeFilter = {
                            filters.find { it.key == filter.key && it.operation == filter.operation }?.value = ""
                            filters.find { it.key == filter.key && it.operation == filter.operation }?.interpretation = null
                            onRefresh()
                        },
                    ){
                        onFilterClick()
                    }
                }
            }

            if (searchData.userSearch && searchData.userLogin != null) {
                item(key = "search_user") {
                    ActiveFilterListingItem(
                        text = stringResource(strings.searchUsersSearch) + ": " + (searchData.userLogin ?: userDef),
                        removeFilter = {
                            searchData.userLogin = null
                            searchData.userSearch = false
                            searchData.searchFinished = false
                            onRefresh()
                        },
                    ){
                        onSearchClick()
                    }
                }
            }

            if (searchData.searchString.isNotEmpty()) {
                item(key = "search_string") {
                    ActiveFilterListingItem(
                        text = stringResource(strings.searchTitle) + ": " + searchData.searchString,
                        removeFilter = {
                            searchData.searchString = ""
                            onRefresh()
                        },
                    ){
                        onSearchClick()
                    }
                }
            }

            if (searchData.searchFinished) {
                item(key = "search_finished") {
                    ActiveFilterListingItem(
                        text = stringResource(strings.searchUserFinishedStringChoice),
                        removeFilter = {
                            searchData.searchFinished = false
                            onRefresh()
                        },
                    ){
                        onSearchClick()
                    }
                }
            }

            if (listingData.sort != null) {
                item(key = "sort") {
                    ActiveFilterListingItem(
                        text = listingData.sort?.interpretation ?: "",
                        removeFilter = {
                            listingData.sort = null
                            onRefresh()
                        },
                    ){
                        onSortClick()
                    }
                }
            }
        }

        if (isShowFilters) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End)
            ) {
                BadgedButton(itemFilter)
                BadgedButton(itemSort)
                if (itemGallery != null) {
                    BadgedButton(itemGallery)
                }
            }
        }
    }
}

