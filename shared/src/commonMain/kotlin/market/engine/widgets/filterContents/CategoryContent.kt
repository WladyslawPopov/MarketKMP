package market.engine.widgets.filterContents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Category
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun CategoryContent(
    title: MutableState<String>,
    searchData: SD,
    listingData: LD,
    categories : List<Category>,
    isLoading : Boolean,
    refresh: () -> Unit,
    goListing: () -> Unit,
    goToSearch: () -> Unit,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val catDef = stringResource(strings.categoryMain)

    val noFound : (@Composable () -> Unit)? =
        if (categories.isEmpty()) {
            @Composable {
                if (listingData.filters.any { it.interpritation != null && it.interpritation != "" } ||
                    searchData.userSearch || searchData.searchString?.isNotEmpty() == true
                ) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel),
                    ) {
                        searchData.clear()
                        listingData.filters.clear()
                        listingData.filters.addAll(EmptyFilters.getEmpty())
                        refresh()
                    }
                } else {
                    showNoItemLayout {
                        refresh()
                    }
                }
            }
        }else{
            null
        }


    BaseContent(
        topBar = null,
        onRefresh = {
            refresh()
        },
        error = null,
        noFound = null,
        isLoading = isLoading,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimens.extraSmallPadding, bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                FiltersBar(
                    listingData,
                    searchData,
                    isShowFilters = false,
                    onSearchClick = goToSearch,
                    onRefresh = {
                        searchData.isRefreshing = true
                        refresh()
                    }
                )
            }

            item {
                if (searchData.searchCategoryID != 1L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        NavigationArrowButton {
                            onCatBack(catDef, searchData, title, scope, refresh)
                        }

                        SimpleTextButton(
                            stringResource(strings.resetLabel),
                        ){
                            searchData.clearCategory()
                            searchData.isRefreshing = true
                            refresh()
                        }
                    }

                }
            }

            when {
                noFound != null -> item { noFound() }
                else -> {
                    items(categories) { category ->
                        Spacer(modifier = Modifier.height(dimens.smallSpacer))

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    category.name ?: "",
                                    color = colors.black,
                                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                    lineHeight = dimens.largeText
                                )
                            },
                            onClick = {
                                searchData.searchCategoryID = category.id
                                searchData.searchCategoryName = category.name
                                searchData.searchParentID = category.parentId
                                searchData.searchIsLeaf = category.isLeaf

                                title.value = category.name ?: catDef
                                searchData.isRefreshing = true

                                if (!category.isLeaf) {
                                    refresh()
                                } else {
                                    goListing()
                                }
                            },
                            icon = {
                                getCategoryIcon(category.name)?.let {
                                    Image(
                                        painterResource(it),
                                        contentDescription = null,
                                        modifier = Modifier.size(dimens.smallIconSize)
                                    )
                                }
                            },
                            badge = {
                                Badge(
                                    containerColor = colors.steelBlue
                                ) {
                                    Text(
                                        text = category.estimatedActiveOffersCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(dimens.extraSmallPadding),
                                        color = colors.white
                                    )
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = colors.selected,
                                unselectedContainerColor = colors.white,
                                selectedIconColor = colors.grayLayout,
                                unselectedIconColor = colors.white,
                                selectedTextColor = colors.grayLayout,
                                selectedBadgeColor = colors.grayLayout,
                                unselectedTextColor = colors.white,
                                unselectedBadgeColor = colors.white
                            ),
                            shape = MaterialTheme.shapes.small,
                            selected = category.isLeaf
                        )
                    }
                }
            }
        }

        AcceptedPageButton(
            strings.categoryEnter,
            Modifier.wrapContentWidth()
                .padding(dimens.smallPadding)
                .align(Alignment.BottomCenter),
        ) {
            goListing()
        }

        Spacer(modifier = Modifier.height(dimens.mediumSpacer))
    }
}

fun onCatBack(
    catDef : String,
    searchData: SD,
    title: MutableState<String>,
    scope: CoroutineScope,
    refresh: () -> Unit
){
    val categoryOperations : CategoryOperations = getKoin().get()
    scope.launch {
        withContext(Dispatchers.IO) {
            if (searchData.searchCategoryID != 1L) {
                val response = categoryOperations.getCategoryInfo(
                    searchData.searchParentID ?: 1L
                )
                withContext(Dispatchers.Main) {
                    val catInfo = response.success
                    if (catInfo != null) {
                        searchData.searchCategoryName = catInfo.name
                        searchData.searchCategoryID = catInfo.id
                        searchData.searchParentID = catInfo.parentId
                        searchData.searchIsLeaf = catInfo.isLeaf == true
                        searchData.isRefreshing = true

                        title.value = catInfo.name ?: catDef
                    }

                   refresh()
                }
            }
        }
    }
}
