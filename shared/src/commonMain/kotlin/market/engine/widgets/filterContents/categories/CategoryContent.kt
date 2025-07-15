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
    val searchData = viewModel.searchData.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val categories = viewModel.categories.collectAsState()
    val selectedId = viewModel.selectedId.collectAsState()

    val pageState = viewModel.pageState.collectAsState()

    val onBack = remember {{
        if (searchData.value.searchCategoryID != 1L) {
            viewModel.navigateBack()
        }else{
            onClose()
        }
    }}

    val noFound : (@Composable () -> Unit)? = remember(categories.value){
        if (categories.value.isEmpty()) {
            {
                NoItemsFoundLayout(
                    textButton = if (searchData.value.searchCategoryID != 1L) stringResource(strings.resetLabel)
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
                    visible = searchData.value.searchCategoryID != 1L,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    NavigationArrowButton {
                        if (!isLoading.value) {
                            onBack()
                        }
                    }
                }

                TextAppBar(
                    buildString {
                        if (searchData.value.searchCategoryID == 1L) {
                            append(pageState.value.catDef)
                        } else {
                            append(searchData.value.searchCategoryName)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                if (searchData.value.searchCategoryID != 1L) {
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
        isLoading = isLoading.value,
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
                items(categories.value) { category ->
                    val icon = getCategoryIcon(category.name)

                    val isSelected = remember(selectedId.value) {
                        if (pageState.value.categoryWithoutCounter)
                            selectedId.value == category.id else category.isLeaf
                    }

                    val item = remember(category) {
                        NavigationItem(
                            title = category.name ?: pageState.value.catDef,
                            image = icon,
                            badgeCount = if (!pageState.value.categoryWithoutCounter)
                                category.estimatedActiveOffersCount else null,
                            onClick = {
                                viewModel.selectCategory(category)
                                if (category.isLeaf && !pageState.value.categoryWithoutCounter) {
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
                pageState.value.catBtn,
                Modifier.fillMaxWidth(if (isBigScreen.value) 0.8f else 1f)
                    .padding(dimens.smallPadding).align(Alignment.BottomCenter),
                enabled = pageState.value.enabledBtn || searchData.value.searchIsLeaf
            ) {
                onCompleted()
            }
        }
    }
}
