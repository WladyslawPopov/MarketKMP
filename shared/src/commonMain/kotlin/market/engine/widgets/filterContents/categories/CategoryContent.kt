package market.engine.widgets.filterContents.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.ilustrations.getCategoryIcon
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryContent(
    viewModel: CategoryViewModel,
    modifier: Modifier = Modifier,
    onCompleted: () -> Unit,
    onClose: () -> Unit,
) {
    val searchData by viewModel.searchData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()

    val pageState by viewModel.pageState.collectAsState()

    val onBack = remember {{
        if (searchData.searchCategoryID != 1L) {
            viewModel.navigateBack()
        }else{
            onClose()
        }
    }}

    val noFound : (@Composable () -> Unit)? = remember(categories){
        if (categories.isEmpty()) {
            {
                NoItemsFoundLayout(
                    textButton = if (searchData.searchCategoryID != 1L) stringResource(strings.resetLabel)
                    else stringResource(strings.refreshButton),
                ) {
                    viewModel.resetToRoot()
                }
            }
        } else {
            null
        }
    }

    EdgeToEdgeScaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.smallPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                AnimatedVisibility(
                    visible = searchData.searchCategoryID != 1L,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NavigationArrowButton {
                        if (!isLoading) {
                            onBack()
                        }
                    }
                }

                TextAppBar(
                    buildString {
                        if (searchData.searchCategoryID == 1L) {
                            append(pageState.catDef)
                        } else {
                            append(searchData.searchCategoryName)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                if (searchData.searchCategoryID != 1L) {
                    ActionButton(
                        stringResource(strings.clear),
                        fontSize = dimens.mediumText,
                    ) {
                        viewModel.resetToRoot()
                    }
                }
            }
        },
        onRefresh = {
            viewModel.onRefresh()
        },
        error = null,
        noFound = noFound,
        isLoading = isLoading,
        modifier = modifier,
    ) { contentPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
        )
        {
            LazyColumnWithScrollBars(
                modifierList = Modifier
                    .fillMaxWidth(if (isBigScreen.value) 0.8f else 1f)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())
            ) {
                items(categories) { category ->
                    val icon = getCategoryIcon(category.name)

                    val isSelected = remember(selectedId) {
                        if (pageState.categoryWithoutCounter)
                            selectedId == category.id else category.isLeaf
                    }

                    val item = remember(category) {
                        NavigationItem(
                            title = category.name ?: pageState.catDef,
                            image = icon,
                            badgeCount = if (!pageState.categoryWithoutCounter)
                                category.estimatedActiveOffersCount else null,
                            onClick = {
                                viewModel.selectCategory(category)
                                if (category.isLeaf && !pageState.categoryWithoutCounter) {
                                    onCompleted()
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
                    ) {
                        item.onClick()
                    }
                }
            }

            AcceptedPageButton(
                pageState.catBtn,
                Modifier.fillMaxWidth(if (isBigScreen.value) 0.8f else 1f)
                    .padding(dimens.smallPadding).align(Alignment.BottomCenter),
                enabled = pageState.enabledBtn || searchData.searchIsLeaf
            ) {
                onCompleted()
            }
        }
    }
}
