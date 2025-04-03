package market.engine.fragments.root.main.createSubscription

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.fragments.base.onError
import market.engine.widgets.filterContents.CategoryContent
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

    val responseGetPage = viewModel.responseGetPage.collectAsState()

    val refresh = {
        viewModel.onError(ServerErrorException())
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        {
            onError(err)
            {
                refresh()
            }
        }
    } else {
        null
    }

    val focusManager: FocusManager = LocalFocusManager.current

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    val defCat = stringResource(strings.selectCategory)

    val selectedCategory = remember { viewModel.selectedCategory }
    val selectedCategoryID = remember { viewModel.selectedCategoryID }

    val searchData = remember { mutableStateOf(SD()) }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(responseGetPage.value){
        if (responseGetPage.value != null){
            viewModel.selectedCategory.value = responseGetPage.value?.fields?.find {
                it.key == "category_id" }?.shortDescription ?: defCat
            viewModel.selectedCategoryID.value = responseGetPage.value?.fields?.find {
                it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull ?: 1L
        }
    }

    val onBack = {
        if (openBottomSheet.value) {
            viewModel.catBack.value = true
        } else {
            component.onBackClicked()
        }
    }

    BackHandler(
        backHandler = model.value.backHandler,
        onBack = {
            onBack()
        }
    )

    val title = stringResource(
        if (model.value.editId == null)
            strings.createNewSubscriptionTitle
        else strings.editLabel
    )

    val clear = remember {
        {
            searchData.value.clear(defCat)

            selectedCategory.value = defCat
            selectedCategoryID.value = 1L

            responseGetPage.value?.fields?.find { it.key == "category_id" }?.shortDescription =
                defCat
            responseGetPage.value?.fields?.find { it.key == "category_id" }?.data =
                null
        }
    }

    BaseContent(
        topBar = {
            CreateSubscriptionAppBar(
                title,
                onBackClick = {
                    onBack()
                }
            )
        },
        onRefresh = {
            refresh()
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
                    isOpen = viewModel.openFiltersCat,
                    searchData = searchData.value,
                    baseViewModel = viewModel,
                    onBackClicked = viewModel.catBack,
                    isFilters = true,
                ){
                    responseGetPage.value?.fields?.find { it.key == "category_id" }?.shortDescription = searchData.value.searchCategoryName
                    responseGetPage.value?.fields?.find { it.key == "category_id" }?.data = JsonPrimitive(searchData.value.searchCategoryID)
                    selectedCategory.value = searchData.value.searchCategoryName
                    selectedCategoryID.value = searchData.value.searchCategoryID

                    openBottomSheet.value = false
                }
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isBigScreen) 2 else 1),
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
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    content = {
                        val items = responseGetPage.value?.fields
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
                                                        openBottomSheet.value = !openBottomSheet.value

                                                        searchData.value.run {
                                                            searchCategoryName = selectedCategory.value
                                                            searchCategoryID = selectedCategoryID.value
                                                            searchParentID = searchCategoryID
                                                        }

                                                        viewModel.openFiltersCat.value = true
                                                    },
                                                    onCancelClick =
                                                        if (selectedCategoryID.value != 1L) {
                                                            clear
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
                                if (model.value.editId == null)
                                    strings.createNewSubscriptionTitle
                                else
                                    strings.editLabel,
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
