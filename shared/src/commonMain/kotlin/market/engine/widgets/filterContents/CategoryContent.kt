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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.network.networkObjects.Category
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryContent(
    searchData: SD,
    filters: ArrayList<Filter> = arrayListOf(),
    baseViewModel: BaseViewModel,
    isRefresh: MutableState<Boolean>? = null,
    isFilters: Boolean = false,
    isCreateOffer: Boolean = false,
    onBackClicked:  MutableState<Boolean>? = null,
    complete: () -> Unit = {},
) {
    val searchCategoryName = remember { mutableStateOf("") }
    val searchCategoryId = remember { mutableStateOf(0L) }
    val searchParentID = remember { mutableStateOf<Long?>(null) }
    val searchIsLeaf = remember { mutableStateOf(false) }

    val isSelected = remember { mutableStateOf(1L) }

    val catDef = if (isFilters || isCreateOffer) {
        stringResource(strings.selectCategory)
    }else{
        stringResource(strings.categoryMain)
    }

    val isLoading = remember { mutableStateOf(false) }
    val categories = remember { mutableStateOf(emptyList<Category>()) }

    val setUpNewParams: (Category) -> Unit = { newCat ->
        searchCategoryId.value = newCat.id
        searchCategoryName.value = newCat.name ?: catDef
        searchParentID.value = newCat.parentId
        searchIsLeaf.value = newCat.isLeaf
        isSelected.value = 1L
    }

    val onComplete = {
        searchData.searchCategoryID = searchCategoryId.value
        searchData.searchCategoryName = searchCategoryName.value
        searchData.searchParentID = searchParentID.value
        searchData.searchIsLeaf = searchIsLeaf.value
        searchData.isRefreshing = true
        isRefresh?.value = true
        complete()
    }

    val refresh = {
        isLoading.value = true
        val sd = searchData.copy(
            searchCategoryID = searchCategoryId.value,
            searchCategoryName = searchCategoryName.value,
            searchParentID = searchParentID.value,
            searchIsLeaf = searchIsLeaf.value
        )
        val ld = LD(
            filters = filters
        )

        baseViewModel.getCategories(sd, ld, (isFilters || isCreateOffer), onSuccess = {
            isLoading.value = false
            categories.value = it
        })
    }

    val onBack = {
        if (searchCategoryId.value != 1L) {
            isLoading.value = true
            baseViewModel.onCatBack(searchParentID.value ?: 1L) { newCat ->
                setUpNewParams(newCat)
                refresh()
            }
        }else{
            complete()
        }
    }

    val reset = {
        if(searchCategoryId.value != 1L) {
            searchCategoryId.value = 1L
            searchCategoryName.value = catDef
            searchParentID.value = null
            searchIsLeaf.value = false
        }

        refresh()
        baseViewModel.updateItemTrigger.value++
    }

    LaunchedEffect(onBackClicked?.value){
        if(onBackClicked?.value == true){
            onBack()
            onBackClicked.value = false
        }
    }

    LaunchedEffect(Unit){
        searchCategoryId.value = searchData.searchCategoryID
        searchCategoryName.value = searchData.searchCategoryName
        searchParentID.value = searchData.searchParentID
        searchIsLeaf.value = searchData.searchIsLeaf

        if(searchIsLeaf.value){
            isLoading.value = true
            baseViewModel.onCatBack(searchParentID.value ?: 1L) { newCat ->
                val cat = if(searchParentID.value == newCat.id){
                    newCat.copy(
                        id = newCat.parentId
                    )
                }else{
                    newCat
                }
                setUpNewParams(cat)
                refresh()
            }
        }else {
            refresh()
        }
    }

    val noFound : @Composable () -> Unit = {
        if (categories.value.isEmpty()) {
            showNoItemLayout(
                textButton = if(searchCategoryId.value != 1L) stringResource(strings.resetLabel)
                else stringResource(strings.refreshButton),
            ) {
                reset()
            }
        }
    }

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
                FilterContentHeaderBar(
                    title = stringResource(strings.selectCategory),
                    isShowClearBtn = searchCategoryId.value != 1L,
                    onClear = {
                       reset()
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
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = searchCategoryId.value != 1L,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            NavigationArrowButton {
                                if(!isLoading.value) {
                                    onBack()
                                }
                            }
                        }

                        TextAppBar(
                            buildString {
                                if (searchCategoryId.value == 1L){
                                    append(catDef)
                                } else {
                                    append(searchCategoryName.value)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.7f),
                        )

                        if (!isFilters && !isCreateOffer && searchCategoryId.value != 1L) {
                            ActionButton(
                                strings.clear,
                                fontSize = dimens.mediumText,
                            ){
                                reset()
                            }
                        }
                    }
                }
                item { noFound() }
                items(categories.value) { category ->
                    val item = NavigationItem(
                        title = category.name ?: catDef,
                        image = getCategoryIcon(category.name),
                        badgeCount = if (!(isFilters || isCreateOffer)) category.estimatedActiveOffersCount else null,
                        onClick = {
                            setUpNewParams(category)

                            if (!category.isLeaf) {
                                refresh()
                            } else {
                                if (!isFilters && !isCreateOffer) {
                                    onComplete()
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
            onComplete()
        }
    }
}
