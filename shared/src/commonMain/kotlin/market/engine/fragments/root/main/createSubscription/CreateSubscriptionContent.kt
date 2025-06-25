package market.engine.fragments.root.main.createSubscription

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.fragments.base.onError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.textFields.DynamicInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateSubscriptionContent(
    component : CreateSubscriptionComponent,
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createSubscriptionViewModel

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val uiState = viewModel.createSubContentState.collectAsState()

    val appBar = uiState.value.appBar
    val page = uiState.value.page
    val categoryState = uiState.value.categoryState
    val title = uiState.value.title

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage.isNotBlank()) {
            {
                onError(err.value)
                {
                    viewModel.refreshPage()
                }
            }
        } else {
            null
        }
    }

    val focusManager: FocusManager = LocalFocusManager.current

    val scaffoldState = rememberBottomSheetScaffoldState()

    val selectedCategory = categoryState.categoryViewModel.categoryName.collectAsState()
    val selectedCategoryID = categoryState.categoryViewModel.categoryId.collectAsState()

    LaunchedEffect(categoryState.openCategory){
        snapshotFlow {
            categoryState.openCategory
        }.collect {
            if (it) {
                scaffoldState.bottomSheetState.expand()
            }else{
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    BackHandler(
        backHandler = model.value.backHandler,
        onBack = {
            viewModel.onBack()
        }
    )

    BaseContent(
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
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()

    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            sheetContentColor = colors.primaryColor,
            sheetBackgroundColor = colors.primaryColor,
            contentColor = colors.primaryColor,
            backgroundColor = colors.primaryColor,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = false,
            sheetContent = {
                CategoryContent(
                    viewModel= categoryState.categoryViewModel,
                    onCompleted = {
                        viewModel.applyCategory(
                            categoryName = selectedCategory.value,
                            categoryId = selectedCategoryID.value
                        )
                        viewModel.closeCategory()
                    },
                    onClose = {
                        viewModel.closeCategory()
                    }
                )
            },
        ) {
            Box(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
                contentAlignment = Alignment.TopCenter
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 2 else 1),
                    modifier = Modifier.pointerInput(Unit){
                        detectTapGestures {
                            focusManager.clearFocus()
                        }
                    }.fillMaxSize().padding(dimens.mediumPadding),
                    userScrollEnabled = true,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        Alignment.CenterHorizontally
                    ),
                    verticalItemSpacing = dimens.smallPadding,
                    content = {
                        val items = page?.fields
                        if (items != null) {
                            items(
                                items.size,
                                key = { items[it].key ?: it }
                            ) { index ->
                                val field = items[index]
                                when (field.widgetType) {
                                    "input" -> {
                                        if (field.key == "category_id"){
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start,
                                                modifier = Modifier.fillMaxWidth(0.8f).padding(dimens.mediumPadding)
                                            ){
                                                FilterButton(
                                                    selectedCategory.value,
                                                    color = if(selectedCategoryID.value == 1L)
                                                        colors.simpleButtonColors else colors.themeButtonColors,
                                                    onClick = {
                                                        viewModel.openCategory()
                                                    },
                                                    onCancelClick =if(selectedCategoryID.value != 1L) {
                                                        {
                                                            viewModel.clearCategory()
                                                        }
                                                    }else{
                                                        null
                                                    }
                                                )
                                            }
                                        }else{
                                            DynamicInputField(field)
                                        }
                                    }
                                    "checkbox" -> {
                                        DynamicCheckbox(field)
                                    }
                                    "select" -> {
                                        DynamicSelect(field)
                                    }
                                    "checkbox_group" -> {
                                        DynamicCheckboxGroup(field)
                                    }
                                    else -> {

                                    }
                                }
                            }
                        }

                        item {
                            AcceptedPageButton(
                                stringResource(if (model.value.editId == null)
                                    strings.createNewSubscriptionTitle
                                else
                                    strings.editLabel),
                                Modifier.align(Alignment.BottomCenter)
                                    .wrapContentWidth()
                                    .padding(dimens.mediumPadding),
                                enabled = !isLoading.value
                            ) {
                                viewModel.postPage(model.value.editId){
                                    component.onBackClicked()
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(dimens.mediumSpacer))
            }
        }
    }
}
