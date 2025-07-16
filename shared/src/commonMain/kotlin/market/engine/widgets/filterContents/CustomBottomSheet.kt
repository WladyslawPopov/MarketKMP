package market.engine.widgets.filterContents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.isBigScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    initValue : Boolean,
    contentPadding : PaddingValues,
    containerColor : Color = colors.white,
    sheetBackgroundColor : Color = colors.primaryColor,
    onClosed : () -> Unit,
    sheetContent : @Composable () -> Unit,
    content : @Composable () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (initValue)
                SheetValue.Expanded
            else
                SheetValue.PartiallyExpanded
        )
    )

    LaunchedEffect(initValue){
        if (initValue){
            scaffoldState.bottomSheetState.expand()
        }else{
            if (scaffoldState.bottomSheetState.currentValue != SheetValue.PartiallyExpanded) {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue){
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded){
            onClosed()
        }
    }

    Box(
        modifier =
            Modifier
            .padding(
                top = if(initValue) contentPadding.calculateTopPadding() else dimens.zero
            ).fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContainerColor = containerColor,
            sheetPeekHeight = 0.dp,
            sheetSwipeEnabled = initValue,
            sheetMaxWidth = if(isBigScreen.value) 1000.dp else BottomSheetDefaults.SheetMaxWidth,
            sheetContent = {
                Box(Modifier.background(sheetBackgroundColor)){
                    sheetContent()
                }
            },
            content = { contentPadding ->
                Box(
                    Modifier.background(colors.primaryColor)
                        .padding(contentPadding),
                ) {
                    content()
                }
            }
        )
    }
}
