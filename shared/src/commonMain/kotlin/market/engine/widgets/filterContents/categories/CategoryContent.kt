package market.engine.widgets.filterContents.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryContent(
    viewModel: CategoryViewModel,
    onClose: () -> Unit = {},
) {
    val searchCategoryName = viewModel.categoryName.collectAsState()
    val searchCategoryId = viewModel.categoryId.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val categories = viewModel.categories.collectAsState()
    val selectedId = viewModel.selectedId.collectAsState()

    val onBack = remember {{
        if (searchCategoryId.value != 1L) {
            viewModel.navigateBack()
        }else{
            onClose()
        }
    }}

    val noFound : @Composable () -> Unit = remember(categories.value){{
        if (categories.value.isEmpty()) {
            showNoItemLayout(
                textButton = if(searchCategoryId.value != 1L) stringResource(strings.resetLabel)
                else stringResource(strings.refreshButton),
            ) {
                viewModel.resetToRoot()
            }
        }
    }}

    val refresh = remember {
        {
            viewModel.onRefresh()
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = refresh,
        error = null,
        noFound = null,//important, because we have our own noFound
        isLoading = isLoading.value,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumnWithScrollBars(
            modifierList = Modifier
                .fillMaxWidth(if(isBigScreen.value) 0.8f else 1f)
                .padding(bottom = 60.dp)
                .align(Alignment.TopCenter),
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
                                append(viewModel.catDef.value)
                            } else {
                                append(searchCategoryName.value)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )

                    if (searchCategoryId.value != 1L) {
                        ActionButton(
                            stringResource(strings.clear),
                            fontSize = dimens.mediumText,
                        ){
                            viewModel.resetToRoot()
                        }
                    }
                }
            }
            item { noFound() }
            items(categories.value) { category ->
                val icon = getCategoryIcon(category.name)

                val isSelected = remember(selectedId.value) {
                    if (viewModel.categoryWithoutCounter)
                        selectedId.value == category.id else category.isLeaf
                }

                val item = remember(category) {
                    NavigationItem(
                        title = category.name ?: viewModel.catDef.value,
                        image = icon,
                        badgeCount = if (!viewModel.categoryWithoutCounter)
                            category.estimatedActiveOffersCount else null,
                        onClick = {
                            viewModel.selectCategory(category)
                            if (category.isLeaf && !viewModel.categoryWithoutCounter) {
                                onClose()
                            }
                        }
                    )
                }

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
                    isSelected = isSelected,
                    badgeColor = colors.steelBlue
                ){
                    item.onClick()
                }
            }
        }

        AcceptedPageButton(
            viewModel.catBtn.value,
            Modifier.fillMaxWidth(if(isBigScreen.value) 0.8f else 1f).padding(dimens.smallPadding).align(Alignment.BottomCenter),
            enabled = viewModel.enabledBtn.value
        ) {
            onClose()
        }
    }
}
