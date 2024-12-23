package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.BaseViewModel
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
    searchData: SD,
    listingData: LD,
    searchCategoryName : MutableState<String>,
    searchCategoryId : MutableState<Long>,
    searchParentID : MutableState<Long?>,
    searchIsLeaf : MutableState<Boolean>,
    isRefreshingFromFilters: MutableState<Boolean>,
    complete: () -> Unit = {},
) {
    val focus = LocalFocusManager.current
    val catDef = if (isCreateOffer || isFilters) stringResource(strings.selectCategory) else stringResource(strings.categoryMain)
    if (searchCategoryName.value == ""){
        searchCategoryName.value = catDef
    }

    val isLoading = baseViewModel.isShowProgress.collectAsState()
    val categories = baseViewModel.responseCategory.collectAsState()

    val refresh = {
        val sd = searchData.copy(
            searchCategoryID = searchCategoryId.value,
            searchCategoryName = searchCategoryName.value,
            searchParentID = searchParentID.value,
            searchIsLeaf = searchIsLeaf.value
        )
        baseViewModel.setLoading(true)
        baseViewModel.getCategories(sd, listingData, (isFilters || isCreateOffer))
    }

    LaunchedEffect(isRefreshingFromFilters.value){
        if (isRefreshingFromFilters.value){
            refresh()
            isRefreshingFromFilters.value = false
        }
    }

    val noFound : (@Composable () -> Unit)? =
        if (categories.value.isEmpty()) {
            @Composable {
                if (searchData.userSearch || searchData.searchString.isNotEmpty() || listingData.filters.any { it.interpritation != null }
                ) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel),
                    ) {
                        searchData.clear()
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
                    AnimatedVisibility(
                        visible = searchCategoryId.value != 1L,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        NavigationArrowButton {
                            focus.clearFocus()
                            baseViewModel.viewModelScope.launch {
                               val newCat = baseViewModel.onCatBack(searchParentID.value ?: 1L)
                                if (newCat != null) {
                                    searchCategoryId.value = newCat.id
                                    searchCategoryName.value = newCat.name ?: catDef
                                    searchParentID.value = newCat.parentId
                                    searchIsLeaf.value = newCat.isLeaf
                                    refresh()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    TextAppBar(
                        searchCategoryName.value
                    )
                }

                if (searchData.searchCategoryID != 1L) {
                    SimpleTextButton(
                        stringResource(strings.resetLabel),
                        textColor = colors.actionTextColor,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    ) {
                        searchCategoryId.value = 1L
                        searchCategoryName.value = catDef
                        refresh()
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
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
                                    focus.clearFocus()
                                    searchCategoryId.value = category.id
                                    searchCategoryName.value = category.name ?: catDef
                                    searchParentID.value = category.parentId
                                    searchIsLeaf.value = category.isLeaf

                                    if (!category.isLeaf) {
                                        refresh()
                                    } else {
                                        if (!isFilters && !isCreateOffer) {
                                            isRefreshingFromFilters.value = true
                                            complete()
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

        val btn = when{
            isFilters -> strings.actionAcceptFilters
            isCreateOffer -> strings.continueLabel
            else -> strings.categoryEnter
        }

        AcceptedPageButton(
            btn,
            Modifier.fillMaxWidth()
                .padding(dimens.mediumPadding).align(Alignment.BottomCenter),
            enabled = !(isCreateOffer && isSelected.value == 1L)
        ) {
            isRefreshingFromFilters.value = true
            complete()
        }
    }
}
