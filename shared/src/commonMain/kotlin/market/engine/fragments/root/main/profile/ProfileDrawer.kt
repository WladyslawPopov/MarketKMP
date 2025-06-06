package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem

@Composable
fun ProfileDrawer(
    activeTitle: String,
    list: List<NavigationItem>,
) {
    ModalDrawerSheet(
        modifier = if(!isBigScreen.value) Modifier.fillMaxWidth(0.8f) else Modifier.wrapContentWidth(),
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.black,
        drawerTonalElevation = 0.dp,
        drawerShape =if(!isBigScreen.value) MaterialTheme.shapes.extraSmall else MaterialTheme.shapes.small
    ) {
        ProfileNavContent(
            list,
            activeTitle
        )
    }
}
