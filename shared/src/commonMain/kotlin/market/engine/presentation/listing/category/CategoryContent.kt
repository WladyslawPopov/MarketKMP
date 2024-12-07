package market.engine.presentation.listing.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun CategoryContent(
    searchData: SD,
    listingData: LD,
    onClose: () -> Unit,
    goListing: () -> Unit,
    goToSearch: () -> Unit,
    onClearSearchClick: () -> Unit,
) {
    val categoryViewModel : CategoryViewModel = koinViewModel()
    val catDef = stringResource(strings.categoryMain)

    val title = remember { mutableStateOf(searchData.searchCategoryName ?: catDef) }

    val isLoadingCategory = categoryViewModel.isShowProgress.collectAsState()
    val isErrorCategory = categoryViewModel.errorMessage.collectAsState()
    val categories = categoryViewModel.responseCategory.collectAsState()
    

    val errorCategory : (@Composable () -> Unit)? = if (isErrorCategory.value.humanMessage != "") {
        { onError(isErrorCategory.value) { categoryViewModel.getCategory(searchData, listingData)} }
    }else{
        null
    }

    LaunchedEffect(Unit) {
        title.value = searchData.searchCategoryName ?: catDef
    }

    val noFound : (@Composable () -> Unit)? =
        if (categories.value.isEmpty()) {
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
                        categoryViewModel.getCategory(searchData, listingData)
                    }
                } else {
                    showNoItemLayout {
                        categoryViewModel.getCategory(searchData, listingData)
                    }
                }
            }
        }else{
            null
        }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BaseContent(
            topBar = {
                CategoryAppBar(
                    isShowNav = searchData.searchCategoryID != 1L,
                    title = title.value,
                    searchData = searchData,
                    onSearchClick = goToSearch,
                    onClearSearchClick = onClearSearchClick,
                    onBeakClick = {
                        if (searchData.searchCategoryID != 1L) {
                            onCatBack(catDef, searchData, listingData, title, categoryViewModel)
                        }
                    },
                    onCloseClick = {
                        onClose()
                    }
                )
            },
            isLoading = isLoadingCategory.value,
            onRefresh = {
                categoryViewModel.getCategory(searchData, listingData)
            },
            toastItem = categoryViewModel.toastItem,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(top = dimens.extraSmallPadding, bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    FiltersBar(
                        listingData,
                        searchData,
                        isShowFilters = false,
                        onSearchClick = goToSearch,
                        onRefresh = {
                            searchData.isRefreshing = true
                            categoryViewModel.getCategory(searchData, listingData)
                        }
                    )
                }

                when{
                    noFound != null -> item { noFound() }
                    errorCategory != null -> item { errorCategory() }
                    else ->{
                        items(categories.value) { category ->
                            Spacer(modifier = Modifier.height(dimens.smallSpacer))

                            NavigationDrawerItem(
                                label = {
                                    Box(
                                        modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            category.name ?: "",
                                            color = colors.black,
                                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                            lineHeight = dimens.largeText
                                        )
                                    }
                                },
                                onClick = {
                                    title.value = category.name ?: catDef
                                    searchData.searchCategoryID = category.id
                                    searchData.searchCategoryName = category.name
                                    searchData.searchParentID = category.parentId
                                    searchData.searchIsLeaf = category.isLeaf
                                    searchData.isRefreshing = true

                                    if (!category.isLeaf) {
                                        categoryViewModel.getCategory(searchData, listingData)
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
        }


        AcceptedPageButton(
            strings.categoryEnter,
            Modifier.fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .padding(dimens.smallPadding),
        ) {
            searchData.isRefreshing = true
            goListing()
        }
    }
}

fun onCatBack(
    catDef : String,
    searchData: SD,
    listingData: LD,
    title: MutableState<String>,
    categoryViewModel: CategoryViewModel,
){
    val categoryOperations : CategoryOperations = getKoin().get()
    categoryViewModel.viewModelScope.launch {
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

                    categoryViewModel.getCategory(searchData, listingData)
                }
            }
        }
    }
}
