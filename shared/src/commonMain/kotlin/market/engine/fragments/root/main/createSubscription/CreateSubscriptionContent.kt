package market.engine.fragments.root.main.createSubscription

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.filterContents.CustomBottomSheet
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.textFields.DynamicInputField
import org.jetbrains.compose.resources.stringResource


@Composable
fun CreateSubscriptionContent(
    component : CreateSubscriptionComponent,
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createSubscriptionViewModel

    val isLoading by viewModel.isShowProgress.collectAsState()
    val err by viewModel.errorMessage.collectAsState()

    val uiState by viewModel.createSubContentState.collectAsState()

    val toastItem by viewModel.toastItem.collectAsState()

    val appBar = uiState.appBar
    val fields = uiState.fields
    val categoryState = uiState.categoryState
    val title = uiState.title

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage.isNotBlank()) {
            {
                OnError(err)
                {
                    viewModel.refreshPage()
                }
            }
        } else {
            null
        }
    }

    val focusManager: FocusManager = LocalFocusManager.current
    val searchData = categoryState.categoryViewModel.searchData.collectAsState()

    BackHandler(
        backHandler = model.value.backHandler,
        onBack = {
            viewModel.onBack()
        }
    )

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = appBar
            ) {
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        onRefresh = {
            viewModel.refreshPage()
        },
        error = error,
        noFound = null,
        isLoading = isLoading,
        toastItem = toastItem,
        modifier = Modifier.pointerInput(Unit){
            detectTapGestures {
                focusManager.clearFocus()
            }
        }.fillMaxSize()
    ) { contentPadding ->
        CustomBottomSheet(
            initValue = categoryState.openCategory,
            contentPadding = contentPadding,
            onClosed = {
                viewModel.closeCategory()
            },
            sheetContent = {
                CategoryContent(
                    viewModel = categoryState.categoryViewModel,
                    onCompleted = {
                        viewModel.applyCategory(
                            categoryName = searchData.value.searchCategoryName,
                            categoryId = searchData.value.searchCategoryID
                        )
                        viewModel.closeCategory()
                    },
                    onClose = {
                        viewModel.closeCategory()
                    }
                )
            },
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 2 else 1),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(
                    dimens.smallPadding,
                    Alignment.CenterHorizontally
                ),
                verticalItemSpacing = dimens.smallPadding,
                contentPadding = contentPadding,
                content = {
                    items(
                        fields.size,
                        key = { fields[it].key ?: it }
                    ) { index ->
                        val field = fields[index]
                        when (field.widgetType) {
                            "input" -> {
                                if (field.key == "category_id") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                            .padding(dimens.mediumPadding)
                                    ) {
                                        FilterButton(
                                            searchData.value.searchCategoryName,
                                            color = if (searchData.value.searchCategoryID == 1L)
                                                colors.simpleButtonColors else colors.themeButtonColors,
                                            onClick = {
                                                viewModel.openCategory()
                                            },
                                            onCancelClick = if (searchData.value.searchCategoryID != 1L) {
                                                {
                                                    viewModel.clearCategory()
                                                }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                } else {
                                    DynamicInputField(field){
                                        viewModel.setNewField(it)
                                    }
                                }
                            }

                            "checkbox" -> {
                                DynamicCheckbox(field){
                                    viewModel.setNewField(it)
                                }
                            }

                            "select" -> {
                                DynamicSelect(field){
                                    viewModel.setNewField(it)
                                }
                            }

                            "checkbox_group" -> {
                                DynamicCheckboxGroup(field){
                                    viewModel.setNewField(it)
                                }
                            }

                            else -> {

                            }
                        }
                    }

                    item {
                        AcceptedPageButton(
                            stringResource(
                                if (model.value.editId == null)
                                    strings.createNewSubscriptionTitle
                                else
                                    strings.editLabel
                            ),
                            Modifier.align(Alignment.BottomCenter)
                                .wrapContentWidth()
                                .padding(dimens.mediumPadding),
                            enabled = !isLoading
                        ) {
                            viewModel.postPage(model.value.editId) {
                                component.onBackClicked()
                            }
                        }
                    }
                }
            )
        }
    }
}
