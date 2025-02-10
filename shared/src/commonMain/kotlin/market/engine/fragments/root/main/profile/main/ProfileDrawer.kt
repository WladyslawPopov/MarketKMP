package market.engine.fragments.root.main.profile.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.NavigationItem
import org.jetbrains.compose.resources.StringResource



@Composable
fun ProfileDrawer(
    activeTitle: StringResource,
    list: List<NavigationItem>,
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.8f),
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.black,
        drawerTonalElevation = 0.dp,
    ) {
        ProfileNavContent(
            list,
            activeTitle
        )
    }
}
