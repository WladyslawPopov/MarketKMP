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
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.items.NavigationItem
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
        NavigationItem(
            title = "Top 100",
            icon = drawables.home,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = "Help",
            icon = drawables.search,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = "Contact Us",
            icon = drawables.search,
            tint = colors.black,
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
                        painter = painterResource(item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(dimens.smallIconSize)
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


