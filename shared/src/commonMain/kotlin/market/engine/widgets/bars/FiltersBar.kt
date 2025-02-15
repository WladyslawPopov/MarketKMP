package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.getBadgedBox
import org.jetbrains.compose.resources.stringResource


private fun constructActiveFiltersTitle(
    filters: List<Filter>?,
    searchData: SD,
    sort: Sort?,
    filtersTitle: String = "",
    searchTitle: String = "",
    sortTitle: String = ""
): String {
    var isShowSearch = false
    val titles = mutableListOf<String>()

    filters?.takeIf { it.isNotEmpty() }?.let {
        titles.add(filtersTitle)
    }

    if (searchData.userSearch && searchData.userLogin != null && !isShowSearch) {
        titles.add(searchTitle)
        isShowSearch = true
    }

    if (searchData.searchString.isNotEmpty() && !isShowSearch) {
        titles.add(searchTitle)
        isShowSearch = true
    }

    if (searchData.searchFinished  && !isShowSearch) {
        titles.add(searchTitle)
    }

    if (sort != null) {
        titles.add(sortTitle)
    }

    return titles.joinToString(", ")
}

private fun filterListingFilters(
    filters: ArrayList<Filter>
): List<Filter> {
    return filters.filter { filter ->
         filter.interpretation != "" && filter.interpretation != null
    }
}

@Composable
fun FiltersBar(
    searchData: SD,
    listingData: LD,
    updateTrigger : Int,
    searchString : MutableState<String>? = null,
    searchUserLogin : MutableState<String?>? = null,
    searchFinished : MutableState<Boolean>? = null,
    isShowFilters: Boolean = true,
    isShowGrid: Boolean = false,
    onChangeTypeList: (Int) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    val filters = filterListingFilters(listingData.filters)
    val filterString = stringResource(strings.filter)
    val sortTitle = stringResource(strings.sort)
    val searchTitle = stringResource(strings.searchTitle)
    val userDef = stringResource(strings.searchUsersSearch)

    // Construct active filters title
    val activeFiltersTitle = mutableStateOf(constructActiveFiltersTitle(filters, searchData, listingData.sort, filterString, searchTitle, sortTitle))

    val itemFilter = remember(filters) {
        NavigationItem(
            title = strings.filter,
            string = filterString,
            icon = drawables.filterIcon,
            tint = colors.black,
            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
            badgeCount = if(filters.isNotEmpty()) filters.size else null,
        )
    }

    val itemSort = remember(listingData.sort) {
        NavigationItem(
            title = strings.sort,
            string = sortTitle,
            icon = drawables.sortIcon,
            tint = colors.black,
            hasNews = listingData.sort != null,
            badgeCount = null
        )
    }

    val itemGallery = if (isShowGrid)
        NavigationItem(
            title = strings.menuTitle,
            icon = drawables.iconWidget,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            isVisible = true
        )
    else null

    Column {
        if (activeFiltersTitle.value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = dimens.smallPadding, start = dimens.mediumPadding)
            ) {
                Text(
                    text = activeFiltersTitle.value,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.black,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (updateTrigger >=0)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(if (isShowFilters) 0.67f else 1f)
                    .clip(MaterialTheme.shapes.medium),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                items(filters, key = { it.interpretation ?: it.value }) { filter ->
                    filter.interpretation?.let { text->
                        ActiveFilterListing(
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
                        ActiveFilterListing(
                            text = searchUserLogin?.value ?: userDef,
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

                if (searchString?.value?.isNotEmpty() == true) {
                    item(key = "search_string") {
                        ActiveFilterListing(
                            text = searchString.value,
                            removeFilter = {
                                searchData.searchString = ""
                                onRefresh()
                            },
                        ){
                            onSearchClick()
                        }
                    }
                }

                if (searchFinished?.value == true) {
                    item(key = "search_finished") {
                        ActiveFilterListing(
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
                        ActiveFilterListing(
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
                ) {
                    IconButton(
                        modifier = Modifier.width(50.dp),
                        onClick = onFilterClick
                    ) {
                        getBadgedBox(item = itemFilter)
                    }

                    IconButton(
                        modifier = Modifier.width(50.dp),
                        onClick = onSortClick
                    ) {
                        getBadgedBox(item = itemSort)
                    }

                    itemGallery?.let { galleryItem ->
                        IconButton(
                            modifier = Modifier.width(30.dp),
                            onClick = {
                                val newType = if (listingData.listingType == 0) 1 else 0
                                onChangeTypeList(newType)
                            }
                        ) {
                            galleryItem.icon = if (listingData.listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal
                            getBadgedBox(item = galleryItem)
                        }
                    }
                }
            }
        }
    }
}

