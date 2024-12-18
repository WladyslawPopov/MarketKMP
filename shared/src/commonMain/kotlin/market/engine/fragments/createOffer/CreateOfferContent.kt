package market.engine.fragments.createOffer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.types.CreateOfferType
import market.engine.fragments.base.BaseContent
import market.engine.widgets.filterContents.CategoryContent

@Composable
fun CreateOfferContent(
    component: CreateOfferComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOfferViewModel
    val categoryID = model.value.categoryId
    val offerId = model.value.offerId
    val type = model.value.type
    val getPage = viewModel.responseGetPage.collectAsState()
    val postPage = viewModel.responsePostPage.collectAsState()
    val params = viewModel.responseGetPageParams.collectAsState()

    val refresh = {
        val cURL = when(type){
            CreateOfferType.CREATE -> "categories/$categoryID/operations/create_offer"
            CreateOfferType.EDIT -> "offers/$offerId/operations/edit_offer"
            CreateOfferType.COPY -> "offers/$offerId/operations/copy_offer"
            CreateOfferType.COPY_WITHOUT_IMAGE -> "offers/$offerId/operations/copy_offer_without_old_photo"
            CreateOfferType.COPY_PROTOTYPE -> "offers/$offerId/operations/copy_offer_from_prototype"
        }
        viewModel.getPage(cURL)
        viewModel.getPageParams("categories/$categoryID/operations/create_offer")
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (model.value.categoryId == 1L)
                BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )
    val searchData = remember { mutableStateOf(SD()) }

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
                    searchData = searchData.value,
                    complete = {
                        model.value.categoryId = searchData.value.searchCategoryID
                        viewModel.activeFiltersType.value = ""
                    },
                    isCreateOffer = true
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(dimens.mediumSpacer),
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //title
                item {

                }

                // category btn
                item {

                }

                // parameters
                item {

                }
            }
        }
    }
}
