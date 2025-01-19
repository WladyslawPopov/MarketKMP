package market.engine.fragments.root.main.profile.conversations

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsAppBar(
    modifier: Modifier = Modifier,
    drawerState: DrawerState
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            MenuHamburgerButton(
                drawerState
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextAppBar(
                stringResource(strings.messageTitle)
            )
        }
    )
}
