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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
         filter.interpritation != "" && filter.interpritation != null
    }
}

@Composable
fun FiltersBar(
    listingData: LD,
    searchData: SD,
    isShowFilters: Boolean = true,
    isShowGrid: Boolean = false,
    onChangeTypeList: (Int) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    val filters = rememberUpdatedState(filterListingFilters(listingData.filters))
    val filterString = stringResource(strings.filter)
    val sortTitle = stringResource(strings.sort)
    val searchTitle = stringResource(strings.searchTitle)
    val userDef = stringResource(strings.searchUsersSearch)

    // Derived filters based on isShowFilters

    val search = remember { mutableStateOf(searchData) }

    // Construct active filters title
    val activeFiltersTitle = remember {
        mutableStateOf(constructActiveFiltersTitle(filters.value, searchData, listingData.sort, filterString, searchTitle, sortTitle))
    }

    LaunchedEffect(Unit){
        search.value = searchData
    }

    val itemFilter = remember(filters.value) {
        NavigationItem(
            title = strings.filter,
            string = filterString,
            icon = drawables.filterIcon,
            tint = colors.black,
            hasNews = filters.value.find { it.interpritation?.isNotEmpty() == true } != null,
            badgeCount = if(filters.value.isNotEmpty()) filters.value.size else null,
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
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(if (isShowFilters) 0.67f else 1f)
                    .clip(MaterialTheme.shapes.medium),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                items(filters.value) { filter ->
                    filter.interpritation?.let { text->
                        ActiveFilterListing(
                            text = text,
                            removeFilter = {
                                filters.value.find { it.key == filter.key && it.operation == filter.operation }?.value = ""
                                filters.value.find { it.key == filter.key && it.operation == filter.operation }?.interpritation = null
                                onRefresh()
                            },
                        ){
                            onFilterClick()
                        }
                    }
                }

                if (search.value.userSearch && search.value.userLogin != null) {
                    item(key = "search_user") {
                        ActiveFilterListing(
                            text = search.value.userLogin ?: userDef,
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

                if (search.value.searchString.isNotEmpty()) {
                    item(key = "search_string") {
                        ActiveFilterListing(
                            text = search.value.searchString,
                            removeFilter = {
                                searchData.searchString = ""
                                onRefresh()
                            },
                        ){
                            onSearchClick()
                        }
                    }
                }

                if (search.value.searchFinished) {
                    item(key = "search_finished") {
                        ActiveFilterListing(
                            text = stringResource(strings.searchUserFinishedStringChoice),
                            removeFilter = {
                                search.value.searchFinished = false
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
                            text = listingData.sort?.interpritation ?: "",
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
                FiltersIconButtons(
                    itemFilter = itemFilter,
                    itemSort = itemSort,
                    itemGallery = itemGallery,
                    onFilterClick = onFilterClick,
                    onSortClick = onSortClick,
                    onChangeTypeList = { newType ->
                        onChangeTypeList(newType)
                    },
                    listingType = listingData.listingType
                )
            }
        }
    }
}

@Composable
fun FiltersIconButtons(
    itemFilter: NavigationItem,
    itemSort: NavigationItem,
    itemGallery: NavigationItem?,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onChangeTypeList: (Int) -> Unit,
    listingType: Int
) {
    Row {
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
                    val newType = if (listingType == 0) 1 else 0
                    onChangeTypeList(newType)
                }
            ) {
                galleryItem.icon = if (listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal
                getBadgedBox(item = galleryItem)
            }
        }
    }
}
