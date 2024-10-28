package market.engine.presentation.category

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.exceptions.onError
import market.engine.presentation.base.BaseContent
import market.engine.widgets.bars.ListingFiltersBar
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.exceptions.showNoItemLayout
import org.jetbrains.compose.resources.painterResource

@Composable
fun CategoryContent(
    component: CategoryComponent,
    modifier: Modifier = Modifier
) {

    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val categoryViewModel = model.categoryViewModel

    val listingData = categoryViewModel.globalData.listingData.data.subscribeAsState()
    val searchData = categoryViewModel.searchData.subscribeAsState()
    val isLoading = categoryViewModel.isShowProgress.collectAsState()
    val isError = categoryViewModel.errorMessage.collectAsState()
    val categories = categoryViewModel.responseCategory.collectAsState()

    val isShowNav = remember { mutableStateOf(false) }

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        { onError(isError.value) { component.onRefresh() } }
    }else{
        null
    }

    LaunchedEffect(searchData){
        if (searchData.value.isRefreshing){
            component.onRefresh()
            searchData.value.isRefreshing = false
        }
    }

    val noItem : (@Composable () -> Unit)? = if (categories.value.isEmpty() && !isLoading.value){
        {
            showNoItemLayout {
                searchData.value.clear()
                listingData.value.filters = EmptyFilters.getEmpty()
                component.onRefresh()
            }
        }
    }else{
        null
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        BaseContent(
            modifier = modifier,
            isLoading = isLoading,
            error = error,
            noFound = noItem,
            topBar = {
                CategoryAppBar(
                    isShowNav,
                    modifier,
                    searchData,
                    onSearchClick = {
                        component.goToSearch()
                    },
                    onClearSearchClick = {
                        if(!isLoading.value) {
                            searchData.value.clearCategory()
                            component.onRefresh()
                        }
                    }
                ) {
                    if(!isLoading.value) {
                        if (searchData.value.searchCategoryID != 1L) {
                            isShowNav.value = true
                            searchData.value.searchCategoryID =
                                searchData.value.searchParentID ?: 1L
                            searchData.value.searchCategoryName =
                                searchData.value.searchParentName ?: ""
                            component.onRefresh()
                        } else {
                            isShowNav.value = false
                        }
                    }
                }
            },
            onRefresh = { component.onRefresh() },
        ){
            LazyColumn(
                modifier = Modifier
                    .padding(top = dimens.mediumPadding, bottom = 60.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item{
                    ListingFiltersBar(
                        listingData,
                        searchData,
                        isShowFilters = false,
                    ){
                        component.onRefresh()
                    }
                }

                items(categories.value){ category ->
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
                            searchData.value.searchCategoryID = category.id
                            searchData.value.searchCategoryName = category.name
                            searchData.value.searchParentID = category.parentId
                            searchData.value.searchIsLeaf = category.isLeaf

                            if (!category.isLeaf) {
                                isShowNav.value = true
                                component.onRefresh()
                            }else{
                                component.goToListing()
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

        AcceptedPageButton(
            strings.categoryEnter,
            Modifier.fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .padding(dimens.smallPadding),
        ){
            component.goToListing()
        }
    }
}
