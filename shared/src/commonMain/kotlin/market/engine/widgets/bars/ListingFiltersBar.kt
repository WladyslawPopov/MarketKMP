package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import org.jetbrains.compose.resources.stringResource

@Composable
fun ListingFiltersBar(
    listingData: State<LD>,
    searchData: State<SD>,
    isShowFilters: Boolean = true,
    onChangeTypeList: (Int) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onRefresh: () -> Unit,
) {
    val isShowFiltersTittle = remember { mutableStateOf( "") }
    val typeList = remember { mutableStateOf(listingData.value.listingType) }

    val countFilters = listingData.value.filters?.filter { it.interpritation != null }?.size

    val itemFilter = NavigationItem(
        title = stringResource(strings.filter),
        icon = drawables.filterIcon,
        tint = colors.black,
        hasNews = false,
        badgeCount = if (countFilters != null && countFilters > 0) countFilters else null,
    )

    val itemSort = NavigationItem(
        title = stringResource(strings.sort),
        icon = drawables.sortIcon,
        tint = colors.black,
        hasNews = false,
        badgeCount = null
    )

    val itemGallery = NavigationItem(
        title = "",
        icon = drawables.iconWidget,
        tint = colors.black,
        hasNews = false,
        badgeCount = null
    )

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
        val userDef = stringResource(strings.searchUsersSearch)
        val auction = stringResource(strings.ordinaryAuction)
        val buyNow = stringResource(strings.buyNow)

        Row(
            modifier = Modifier
                .fillMaxWidth().padding(start = dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(if (isShowFilters) 0.6f else 1f).clip(shape = MaterialTheme.shapes.medium),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                val filters = if(isShowFilters)
                    listingData.value.filters?.filter { it.interpritation != null
                            && it.interpritation != auction
                            && it.interpritation != buyNow
                    } else listingData.value.filters?.filter { it.interpritation != null }

                if (filters != null) {
                    if (filters.isNotEmpty()){
                        isShowFiltersTittle.value = filterString
                    }

                    items(filters.toList()) { filter ->
                        if (filter.interpritation != null &&
                            filter.interpritation != stringResource(strings.ordinaryAuction) &&
                            filter.interpritation != stringResource(strings.buyNow)
                        ) {
                            ActiveFilterListing(
                                text = filter.interpritation!!,
                                removeFilter = {
                                    listingData.value.filters?.remove(filter)
                                    onRefresh()
                                }
                            )
                        }
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
                Row(
                    modifier = Modifier,
                ) {
                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            onFilterClick()
                        }
                    ) {
                        getBadgedBox(item = itemFilter)
                    }

                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {

                        }
                    ) {
                        getBadgedBox(item = itemSort)
                    }

                    IconButton(
                        modifier = Modifier.size(50.dp),
                        onClick = {
                            typeList.value = if (typeList.value == 0) 1 else 0
                            listingData.value.listingType = typeList.value
                            onChangeTypeList(typeList.value)
                        }
                    ) {
                        when (typeList.value){
                            0 ->{
                                itemGallery.icon = drawables.iconWidget
                            }
                            1 ->{
                                itemGallery.icon = drawables.iconSliderHorizontal
                            }
                        }
                        getBadgedBox(item = itemGallery)
                    }
                }
            }
        }
    }
}
