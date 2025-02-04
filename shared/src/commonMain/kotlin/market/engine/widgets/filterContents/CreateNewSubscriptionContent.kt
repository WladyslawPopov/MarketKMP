package market.engine.widgets.filterContents

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
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Icon
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.filtersObjects.EmptyFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.AcceptedPageButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateNewSubscriptionContent(
    isRefreshing: MutableState<Boolean>,
    listingData: LD,
    baseViewModel: BaseViewModel,
    onClose: () -> Unit
) {
    val focusManager: FocusManager = LocalFocusManager.current

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBottomSheet = remember { mutableStateOf(false) }

    val defCat = stringResource(strings.selectCategory)

    val selectedCategory = remember { mutableStateOf(listingData.filters.find { it.key == "category" }?.interpritation ?: defCat) }
    val selectedCategoryID = remember { mutableStateOf(listingData.filters.find { it.key == "category" }?.value?.toLongOrNull() ?: 1L) }
    val selectedCategoryParentID = remember { mutableStateOf(listingData.filters.find { it.key == "category" }?.value?.toLongOrNull()) }
    val selectedCategoryIsLeaf = remember { mutableStateOf(listingData.filters.find { it.key == "category" }?.operation?.toBoolean() ?: false) }
    val selectedType = remember { mutableStateOf(listingData.filters.find { it.key == "sale_type" }?.interpritation ?: "") }

    val isRefreshingFromFilters = remember { mutableStateOf(false) }

    LaunchedEffect(openBottomSheet.value){
        if (openBottomSheet.value) {
            val sd = SD(
                searchCategoryID = selectedCategoryID.value,
                searchCategoryName = selectedCategory.value,
                searchParentID = selectedCategoryParentID.value,
                searchIsLeaf = selectedCategoryIsLeaf.value
            )
            baseViewModel.setLoading(true)
            baseViewModel.getCategories(sd, LD(),true)

            scaffoldState.bottomSheetState.expand()
        }else{
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed) {
        if (scaffoldState.bottomSheetState.isCollapsed) {
            if (selectedCategoryID.value != 1L) {
                listingData.filters.find { it.key == "category" }?.value =
                    selectedCategoryID.value.toString()
                listingData.filters.find { it.key == "category" }?.interpritation =
                    selectedCategory.value
                listingData.filters.find { it.key == "category" }?.operation =
                    selectedCategoryIsLeaf.value.toString()
            }else{
                listingData.filters.find { it.key == "category" }?.value = ""
                listingData.filters.find { it.key == "category" }?.interpritation = null
                listingData.filters.find { it.key == "category" }?.operation = null
            }
            selectedCategory.value = selectedCategory.value
        }
    }

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
                baseViewModel = baseViewModel,
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
            //Header Filters
            Row(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            onClose()
                        },
                        content = {
                            Icon(
                                painterResource(drawables.closeBtn),
                                tint = colors.black,
                                contentDescription = stringResource(strings.actionClose)
                            )
                        },
                    )

                    Text(
                        stringResource(strings.filter),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(dimens.smallPadding)
                    )
                }

                if (isRefreshing.value) {
                    Button(
                        onClick = {
                            listingData.filters.clear()
                            listingData.filters.addAll(EmptyFilters.getEmpty())
                            isRefreshing.value = true
                            onClose()
                        },
                        content = {
                            Text(
                                stringResource(strings.clear),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.black
                            )
                        },
                        colors = colors.simpleButtonColors
                    )
                }
            }

            AcceptedPageButton(
                strings.actionAcceptFilters,
                Modifier.align(Alignment.BottomCenter)
                    .wrapContentWidth()
                    .padding(dimens.mediumPadding)
            ){
                isRefreshing.value = true
                onClose()
            }
            Spacer(modifier = Modifier.height(dimens.mediumSpacer))
        }
    }
}
