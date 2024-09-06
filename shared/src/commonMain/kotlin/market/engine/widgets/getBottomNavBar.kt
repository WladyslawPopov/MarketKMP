package market.engine.widgets

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.items.NavigationItem
import market.engine.root.RootComponent
import market.engine.root.navigateFromBottomBar

@Composable
fun getBottomNavBar(
    component: RootComponent,
    modifier: Modifier = Modifier,
    listItems : List<NavigationItem>
){
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }
    
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = dimens.smallPadding, topEnd = dimens.smallPadding)),

        containerColor = colors.white
    ) {
        listItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    navigateFromBottomBar(index, component)
                },
                icon = {
                    getBadgedBox(modifier = modifier, item, selectedItemIndex == index)
                },
                label = {
                    if(selectedItemIndex == index) {
                        Text(text = item.title, fontSize = 10.sp)
                    }
                }
            )
        }
    }
}
