package market.engine.widgets.bottombars

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.items.NavigationItem
import market.engine.root.RootComponent
import market.engine.root.navigateFromBottomBar
import market.engine.widgets.getBadgedBox

@Composable
fun getBottomNavBar(
    component: RootComponent,
    modifier: Modifier = Modifier,
    listItems: List<NavigationItem>,
    currentScreen: Int
){
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = dimens.smallPadding, topEnd = dimens.smallPadding)),

        containerColor = colors.white,
        contentColor = colors.lightGray
    ) {
        Spacer(modifier = Modifier.width(dimens.smallSpacer))
        listItems.forEachIndexed { index, item ->
            val isSelected = getActiveScreen(index, currentScreen)
            NavigationBarItem(
                colors = colors.navItemColors,
                selected = isSelected,
                onClick = {
                    navigateFromBottomBar(index, component)
                },
                icon = {
                    getBadgedBox(modifier, item, isSelected)
                },
                label = {
                    if(isSelected) {
                        Text(
                            text = item.title,
                            fontSize = 9.sp,
                            maxLines = 1,
                            lineHeight = 8.sp
                        )
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(dimens.smallSpacer))
    }
}

fun getActiveScreen(index: Int, currentScreen: Int) : Boolean{
    return if (currentScreen == index){
        true
    }else{
        when(currentScreen){
            5 -> index == 1
            else -> false
        }
    }
}


