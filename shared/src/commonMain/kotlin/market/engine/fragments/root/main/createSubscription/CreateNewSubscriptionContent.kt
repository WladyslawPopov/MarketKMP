package market.engine.fragments.root.main.createSubscription

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.exceptions.onError
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.textFields.DynamicInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateNewSubscriptionContent(
    component : CreateNewSubscriptionComponent,
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createNewSubscriptionViewModel

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val responseGetPage = viewModel.responseGetPage.collectAsState()

    val refresh = {
        viewModel.onError(ServerErrorException())
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        { onError(err.value) { refresh() } }
    } else {
        null
    }

    val focusManager: FocusManager = LocalFocusManager.current

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    val defCat = stringResource(strings.categoryEnter)

    val selectedCategory = remember { mutableStateOf(defCat) }
    val selectedCategoryID = remember { mutableStateOf(1L) }
    val selectedCategoryParentID = remember { mutableStateOf<Long?>(null) }
    val selectedCategoryIsLeaf = remember { mutableStateOf(false) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            val sd = SD(
                searchCategoryID = selectedCategoryID.value,
                searchCategoryName = selectedCategory.value,
                searchParentID = selectedCategoryParentID.value,
                searchIsLeaf = selectedCategoryIsLeaf.value
            )
            viewModel.setLoading(true)
            viewModel.getCategories(sd, LD(),true)

            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(responseGetPage.value){
        if (responseGetPage.value != null){
            selectedCategory.value = responseGetPage.value?.fields?.find { it.key == "category_id" }?.shortDescription ?: defCat
            selectedCategoryID.value = responseGetPage.value?.fields?.find { it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull ?: 1L
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed){
        if (scaffoldState.bottomSheetState.isCollapsed){
            responseGetPage.value?.fields?.find { it.key == "category_id" }?.shortDescription = selectedCategory.value
            responseGetPage.value?.fields?.find { it.key == "category_id" }?.data = JsonPrimitive(selectedCategoryID.value)
        }
    }

    BaseContent(
        topBar = {
            CreateNewSubscriptionAppBar(
                onBackClick = {
                    component.onBackClicked()
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
                    baseViewModel = viewModel,
                    searchData = SD(
                        searchCategoryID = selectedCategoryID.value,
                        searchCategoryName = selectedCategory.value,
                        searchParentID = selectedCategoryParentID.value,
                        searchIsLeaf = selectedCategoryIsLeaf.value
                    ),
                    listingData = LD(),
                    searchCategoryId = selectedCategoryID,
                    searchCategoryName = selectedCategory,
                    searchParentID = selectedCategoryParentID,
                    searchIsLeaf = selectedCategoryIsLeaf,
                    isRefreshingFromFilters = isRefreshingFromFilters,
                    isFilters = true,
                    complete = {
                        openBottomSheet.value = false
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(dimens.mediumPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
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
                                            modifier = Modifier.padding(dimens.mediumPadding)
                                        ){
                                            FilterButton(
                                                modifier = Modifier.padding(dimens.smallPadding),
                                                selectedCategory.value,
                                                color = if(selectedCategoryID.value == 1L)
                                                    colors.simpleButtonColors else colors.themeButtonColors,
                                                onClick = {
                                                    openBottomSheet.value = !openBottomSheet.value
                                                },
                                                onCancelClick = {
                                                    if (selectedCategoryID.value != 1L) {
                                                        SmallIconButton(
                                                            icon = drawables.cancelIcon,
                                                            contentDescription = stringResource(strings.actionClose),
                                                            color = colors.steelBlue,
                                                            modifier = Modifier.size(dimens.extraSmallIconSize),
                                                            modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                                                        ) {
                                                            selectedCategory.value = defCat
                                                            selectedCategoryID.value = 1L
                                                            selectedCategoryParentID.value = null
                                                            responseGetPage.value?.fields?.find { it.key == "category_id" }?.shortDescription = defCat
                                                            responseGetPage.value?.fields?.find { it.key == "category_id" }?.data = null
                                                        }
                                                    }
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
                            strings.createNewSubscriptionTitle,
                            Modifier.align(Alignment.BottomCenter)
                                .wrapContentWidth()
                                .padding(dimens.mediumPadding)
                        ) {
                            viewModel.viewModelScope.launch {
                                val res = viewModel.postPage(model.value.editId)

                                if (res) {
                                    delay(2000)
                                    withContext(Dispatchers.Main){
                                        component.onBackClicked()
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimens.mediumSpacer))
            }
        }
    }
}
