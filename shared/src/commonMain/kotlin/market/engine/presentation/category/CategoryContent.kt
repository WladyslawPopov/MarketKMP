package market.engine.presentation.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import market.engine.core.globalObjects.searchData
import market.engine.core.types.CategoryScreenType
import market.engine.widgets.texts.TitleText
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.exceptions.onError
import market.engine.presentation.base.BaseContent
import market.engine.widgets.buttons.StringButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryContent(
    component: CategoryComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.searchData.collectAsState()
    val categoryType = searchData.value.categoryType

    when(categoryType){
        CategoryScreenType.LISTING -> {
            component.goToListing()
        }
        CategoryScreenType.SEARCH -> {
            component.goToSearch()
        }
        CategoryScreenType.CATEGORY -> {
            val modelState = component.model.subscribeAsState()
            val model = modelState.value

            val isLoading = model.isLoading.collectAsState()
            val isError = model.isError.collectAsState()
            val categories = model.categories.collectAsState()

            val isShowNav = remember { mutableStateOf(false) }

            if (searchData.value.searchCategoryID != null){
                component.updateCategoryList(searchData.value.searchCategoryID!!)
            }

            val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
                { onError(model.isError.value) { component.onRefresh() } }
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
                    topBar = {
                        val title = if ( searchData.value.fromSearch) {
                            if (searchData.value.searchString != "") {
                                searchData.value.searchString ?: stringResource(strings.selectSearchTitle)
                            }else{
                                stringResource(strings.selectSearchTitle)
                            }
                        }else stringResource(strings.selectSearchTitle)

                        CategoryAppBar(
                            if(!isLoading.value) title else "",
                            isShowNav,
                            modifier,
                            searchData = searchData,
                            onSearchClick = {
                                component.goToSearch()
                            }
                        ) {
                            if (searchData.value.searchCategoryID != 1L) {
                                isShowNav.value = true
                                searchData.value.searchCategoryID = searchData.value.searchParentID ?: 1L
                                searchData.value.searchCategoryName = searchData.value.searchParentName ?: ""
                                component.onRefresh()
                            } else {
                                isShowNav.value = false
                            }

                        }
                    },
                    onRefresh = { component.onRefresh() },
                ){
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(400.dp,2000.dp)
                            .padding(top = dimens.mediumPadding, bottom = 60.dp),

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight().padding(dimens.smallPadding)
                            ) {
                                TitleText(
                                    searchData.value.searchCategoryName?: stringResource(strings.categoryMain),
                                    modifier.align(Alignment.CenterStart)
                                )

                                if (searchData.value.searchCategoryID != 1L) {
                                    StringButton(
                                        stringResource(strings.resetLabel),
                                        colors.solidGreen,
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        onClick = {
                                            searchData.value.searchCategoryID = 1L
                                            component.onRefresh()
                                        }
                                    )
                                }
                            }
                        }
                        items(categories.value){ category ->
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
                                    Badge {
                                        Text(
                                            text = category.estimatedActiveOffersCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(dimens.extraSmallPadding)
                                        )
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                    .background(colors.white),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = colors.grayLayout,
                                    unselectedContainerColor = colors.white,
                                    selectedIconColor = colors.grayLayout,
                                    unselectedIconColor = colors.white,
                                    selectedTextColor = colors.grayLayout,
                                    selectedBadgeColor = colors.grayLayout,
                                    unselectedTextColor = colors.white,
                                    unselectedBadgeColor = colors.white
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                selected = true
                            )

                            Spacer(modifier = Modifier.height(dimens.smallPadding))
                        }
                    }
                }

                TextButton(
                    onClick = {
                        component.goToListing()
                    },
                    colors = colors.themeButtonColors,
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                        .padding(dimens.smallPadding),
                    shape = MaterialTheme.shapes.small

                ){
                    Text(
                        text = stringResource(strings.categoryEnter),
                        color = colors.alwaysWhite,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                        lineHeight = dimens.largeText
                    )
                }
            }
        }
    }
}
