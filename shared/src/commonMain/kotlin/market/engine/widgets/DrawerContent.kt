package market.engine.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import market.engine.business.constants.ThemeResources.drawables
import market.engine.root.DrawerItem
import org.jetbrains.compose.resources.painterResource

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope
) {

    var selectedIndex by rememberSaveable{
        mutableStateOf(0)
    }

    val list = listOf(
        DrawerItem(
            title = "Top 100",
            selectedIcon = drawables.home,
            unselectedIcon = drawables.home,
            hasNews = false,
            badgeCount = null
        ),
        DrawerItem(
            title = "Help",
            selectedIcon = drawables.search,
            unselectedIcon = drawables.search,
            hasNews = false,
            badgeCount = null
        ),
        DrawerItem(
            title = "Contact Us",
            selectedIcon = drawables.search,
            unselectedIcon = drawables.search,
            hasNews = false,
            badgeCount = null
        )
    )

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 300.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        list.forEachIndexed { index, item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = index == selectedIndex,
                onClick = {
                    selectedIndex = index
                    scope.launch {
                        drawerState.close()
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(if (index == selectedIndex) item.selectedIcon else item.unselectedIcon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                badge = {
                    if (item.badgeCount != null) {
                        Text(text = item.badgeCount.toString())
                    }

                    if (item.hasNews) {
                        Badge {  }
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}


