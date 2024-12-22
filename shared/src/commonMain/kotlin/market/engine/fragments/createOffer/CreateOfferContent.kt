package market.engine.fragments.createOffer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.common.getPermissionHandler
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.CreateOfferType
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.exceptions.DynamicPayloadContent
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.grids.PhotoDraggableGrid
import market.engine.widgets.textFields.DynamicInputField

@Composable
fun CreateOfferContent(
    component: CreateOfferComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOfferViewModel
    val offerId = model.value.offerId
    val type = model.value.type
    val getPage = viewModel.responseGetPage.collectAsState()
    val postPage = viewModel.responsePostPage.collectAsState()

    val images = viewModel.responseImages.collectAsState()

    val focusManager = LocalFocusManager.current


    val categoryName = remember { mutableStateOf("") }
    val categoryID = remember { mutableStateOf(model.value.categoryId) }
    val parentID : MutableState<Long?> = remember { mutableStateOf(null) }
    val isLeaf = remember { mutableStateOf(false) }
    val isRefreshingFromFilters = remember { mutableStateOf(false) }


    val refresh = {
        when(type){
            CreateOfferType.CREATE -> viewModel.getPage("categories/${categoryID.value}/operations/create_offer")
            CreateOfferType.EDIT ->  viewModel.getPage("offers/$offerId/operations/edit_offer", categoryID.value)
            CreateOfferType.COPY ->  viewModel.getPage("offers/$offerId/operations/copy_offer", categoryID.value)
            CreateOfferType.COPY_WITHOUT_IMAGE -> viewModel.getPage("offers/$offerId/operations/copy_offer_without_old_photo", categoryID.value)
            CreateOfferType.COPY_PROTOTYPE -> viewModel.getPage("offers/$offerId/operations/copy_offer_from_prototype", categoryID.value)
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (model.value.categoryId == 1L)
                BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )
    val searchData = remember { mutableStateOf(SD(
        searchCategoryID = model.value.categoryId,
        searchCategoryName = categoryName.value
    )) }

    val listingData = remember { mutableStateOf(LD()) }


    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed){
        if (scaffoldState.bottomSheetState.isCollapsed){
           refresh()
        }
    }

    LaunchedEffect(viewModel.activeFiltersType.value){
        if (viewModel.activeFiltersType.value == ""){
            scaffoldState.bottomSheetState.collapse()
            refresh()
        }else{
            scaffoldState.bottomSheetState.expand()
        }
    }

    val isLoading = viewModel.isShowProgress.collectAsState()
    val error : (@Composable () -> Unit)? = null

    BaseContent(
        topBar = {
            CreateOfferAppBar(
                type,
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
                    complete = {
                        model.value.categoryId = searchData.value.searchCategoryID
                        viewModel.activeFiltersType.value = ""
                    },
                    isCreateOffer = true,
                    searchData = searchData.value,
                    listingData = listingData.value,
                    searchCategoryId = categoryID,
                    searchCategoryName = categoryName,
                    searchParentID = parentID,
                    searchIsLeaf = isLeaf,
                    isRefreshingFromFilters = isRefreshingFromFilters,
                )
            },
        ) {
            AnimatedVisibility(scaffoldState.bottomSheetState.isCollapsed) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding)
                        ) {
                            Text(
                                getPage.value?.description ?: "",
                                color = colors.black,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            ActionButton(
                                strings.changeCategory,
                                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                alignment = Alignment.TopEnd,
                            ) {
                                viewModel.activeFiltersType.value = "categories"
                            }
                        }
                    }

                    item {
                        val titleField = getPage.value?.fields?.find { it.key == "title" }
                        if (titleField != null) {
                            DynamicInputField(
                                titleField,
                                Modifier.fillMaxWidth().padding(dimens.smallPadding)
                            )
                        }
                    }

                    item {
                        val paramList = getPage.value?.fields?.filter { it.key?.contains("par_") == true } ?: emptyList()
                        DynamicPayloadContent(
                            paramList,
                            Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding)
                        )
                    }

                    // Images
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding)
                        ) {
                            Text(
                                "Прикрепить фотографии к лоту",
                                color = colors.black,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )

                            ActionButton(
                                strings.chooseAction,
                                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                alignment = Alignment.TopEnd,
                            ) {
                                if (!getPermissionHandler().checkImagePermissions()){
                                    getPermissionHandler().requestImagePermissions {
                                        if (it) {
                                            viewModel.getImages()
                                        }
                                    }
                                }else{
                                    viewModel.getImages()
                                }
                            }
                        }

                        AnimatedVisibility(images.value.isNotEmpty()){
                            PhotoDraggableGrid(images.value, viewModel)
                        }
                    }

                    getPage.value?.fields?.forEach { field ->
                        when (field.key) {
                            "category_id" -> {
                                field.data?.jsonPrimitive?.longOrNull?.let {
                                    model.value.categoryId = it
                                }
                            }
                        }
                    }

                    item {
                        val label = when (type) {
                            CreateOfferType.EDIT -> strings.actionSaveLabel
                            else -> strings.sellOfferLabel
                        }
                        AcceptedPageButton(
                            text = label,
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.mediumPadding),
                        ) {

                        }
                    }
                }
            }
        }
    }
}
