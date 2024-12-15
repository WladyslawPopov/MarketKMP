package market.engine.widgets.filterContents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.presentation.base.BaseContent
import market.engine.presentation.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryContent(
    baseViewModel: BaseViewModel,
    isFilters: Boolean = false,
    isCreateOffer: Boolean = false,
    searchData: SD = SD(),
    listingData: LD = LD(),
    goListing: () -> Unit = {},
) {
    val catDef = stringResource(strings.categoryMain)

    val isLoading = baseViewModel.isShowProgress.collectAsState()
    val categories = baseViewModel.responseCategory.collectAsState()

    val title = remember {
        mutableStateOf(searchData.searchCategoryName?:catDef)
    }

    val refresh = {
        baseViewModel.setLoading(true)
        baseViewModel.getCategories(searchData, listingData, (isFilters || isCreateOffer))
        title.value = searchData.searchCategoryName ?: catDef
    }

    val noFound : (@Composable () -> Unit)? =
        if (categories.value.isEmpty()) {
            @Composable {
                if (searchData.userSearch || searchData.searchString?.isNotEmpty() == true) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel),
                    ) {
                        searchData.clear()
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

    val isSelected = remember { mutableStateOf(1L) }

    BaseContent(
        topBar = null,
        onRefresh = {
            refresh()
        },
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationArrowButton {
                        if (searchData.searchCategoryID == 1L){
                            goListing()
                        }else{
                            baseViewModel.onCatBack(searchData, refresh)
                        }
                    }

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    TextAppBar(
                        title.value
                    )
                }

                if (searchData.searchCategoryID != 1L) {
                    SimpleTextButton(
                        stringResource(strings.resetLabel),
                        textColor = colors.actionTextColor,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    ) {
                        searchData.clearCategory()
                        searchData.isRefreshing = true
                        title.value = catDef

                        refresh()
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = dimens.extraSmallPadding, bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    noFound != null -> item { noFound() }
                    else -> {
                        items(categories.value) { category ->
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
                                    searchData.isRefreshing = true

                                    title.value = category.name ?: catDef

                                    if (!category.isLeaf) {
                                        refresh()
                                    } else {
                                        if (!isFilters) {
                                            goListing()
                                        }else{
                                            isSelected.value = category.id
                                        }
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
                                    if (!(isFilters || isCreateOffer)) {
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
                                selected = if(isFilters || isCreateOffer) isSelected.value == category.id else category.isLeaf
                            )
                        }
                    }
                }
            }
        }

       val btn =  if (isFilters || isCreateOffer){
           strings.actionAcceptFilters
       }else{
          strings.categoryEnter
       }

        AcceptedPageButton(
            btn,
            Modifier.wrapContentWidth()
                .padding(dimens.smallPadding)
                .align(Alignment.BottomCenter),
            enabled = !(isCreateOffer && !searchData.searchIsLeaf)
        ) {
            goListing()
        }

        Spacer(modifier = Modifier.height(dimens.mediumSpacer))
    }
}


