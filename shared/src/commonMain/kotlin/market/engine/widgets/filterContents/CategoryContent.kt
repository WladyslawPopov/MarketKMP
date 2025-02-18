package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.launch
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.rows.FilterContentHeaderRow
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryContent(
    filters: ArrayList<Filter> = arrayListOf(),
    baseViewModel: BaseViewModel,
    isFilters: Boolean = false,
    isCreateOffer: Boolean = false,
    searchCategoryName : MutableState<String>,
    searchCategoryId : MutableState<Long>,
    searchParentID : MutableState<Long?>,
    searchIsLeaf : MutableState<Boolean>,
    isRefreshingFromFilters: MutableState<Boolean>,
    complete: () -> Unit = {},
) {
    val focus = LocalFocusManager.current
    val catDef = stringResource(strings.categoryMain)
    if (searchCategoryName.value == ""){
        searchCategoryName.value = catDef
    }

    val isLoading = baseViewModel.isShowProgress.collectAsState()
    val categories = baseViewModel.responseCategory.collectAsState()

    val refresh = {
        val sd = SD(
            searchCategoryID = searchCategoryId.value,
            searchCategoryName = searchCategoryName.value,
            searchParentID = searchParentID.value,
            searchIsLeaf = searchIsLeaf.value
        )
        val ld = LD(
            filters = filters
        )
        baseViewModel.setLoading(true)
        baseViewModel.getCategories(sd, ld, (isFilters || isCreateOffer))
    }

    val onBack = {
        focus.clearFocus()
        baseViewModel.viewModelScope.launch {
            val newCat = baseViewModel.onCatBack(searchParentID.value ?: 1L)
            if (newCat != null) {
                searchCategoryId.value = newCat.id
                searchCategoryName.value = newCat.name ?: catDef
                searchParentID.value = newCat.parentId
                searchIsLeaf.value = newCat.isLeaf
                refresh()
            }else{
                complete()
            }
        }
    }

    LaunchedEffect(isRefreshingFromFilters.value){
        if (isRefreshingFromFilters.value){
            refresh()
            isRefreshingFromFilters.value = false
        }
    }

    val noFound : @Composable () -> Unit = {
        if (categories.value.isEmpty()) {
            showNoItemLayout(
                textButton = if(searchCategoryId.value != 1L) stringResource(strings.resetLabel) else stringResource(strings.refreshButton),
            ) {
                if(searchCategoryId.value != 1L) {
                    searchCategoryId.value = 1L
                    searchCategoryName.value = catDef
                    searchParentID.value = null
                    searchIsLeaf.value = false
                }

                refresh()
                baseViewModel.updateItemTrigger.value++
            }
        }
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
            if (isFilters) {
                FilterContentHeaderRow(
                    title = stringResource(strings.selectCategory),
                    isShowClearBtn = searchCategoryId.value != 1L,
                    onClear = {
                        searchCategoryId.value = 1L
                        searchCategoryName.value = catDef
                        searchParentID.value = null
                        searchIsLeaf.value = false
                        refresh()
                        baseViewModel.updateItemTrigger.value++
                    },
                    onClosed = {
                        complete()
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = searchCategoryId.value != 1L,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            NavigationArrowButton {
                                onBack()
                            }
                        }

                        Spacer(modifier = Modifier.width(dimens.smallSpacer))

                        TextAppBar(
                            searchCategoryName.value,
                            modifier = Modifier.fillMaxWidth(0.7f),
                        )
                    }
                }
                item { noFound() }
                items(categories.value) { category ->
                    val item = NavigationItem(
                        title = category.name ?: catDef,
                        image = getCategoryIcon(category.name),
                        badgeCount = if (!(isFilters || isCreateOffer)) category.estimatedActiveOffersCount else null,
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
                        }
                    )
                    Spacer(modifier = Modifier.height(dimens.smallSpacer))
                    getNavigationItem(
                        item,
                        label = {
                            Text(
                                category.name ?: "",
                                color = colors.black,
                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                lineHeight = dimens.largeText
                            )
                        },
                        isSelected = if(isFilters || isCreateOffer) isSelected.value == category.id else category.isLeaf,
                        badgeColor = colors.steelBlue
                    )
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
