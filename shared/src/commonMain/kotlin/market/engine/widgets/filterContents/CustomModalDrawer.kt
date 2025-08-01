package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItemUI
import market.engine.fragments.root.main.profile.ProfileDrawer

@Composable
fun CustomModalDrawer(
    modifier : Modifier = Modifier,
    title : String,
    publicProfileNavigationItems: List<NavigationItemUI>,
    content : @Composable (Modifier, DrawerState) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = if(isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }

    Scaffold {
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = {
                Row(
                    modifier = Modifier.background(
                        if (isBigScreen.value)
                            colors.primaryColor
                        else
                            colors.transparent
                    ).fillMaxSize()
                ) {
                    if (isBigScreen.value) {
                        AnimatedVisibility(hideDrawer.value) {
                            ProfileDrawer(
                                title,
                                publicProfileNavigationItems
                            )
                        }
                    } else {
                        ProfileDrawer(
                            title,
                            publicProfileNavigationItems
                        )
                    }

                    if (isBigScreen.value) {
                        content(Modifier.weight(1f), drawerState)
                    }
                }
            },
            scrimColor = if (drawerState.isOpen && !isBigScreen.value)
                DrawerDefaults.scrimColor
            else colors.transparent,
            gesturesEnabled = drawerState.isOpen && !isBigScreen.value,
        ) {
            content(Modifier.fillMaxWidth(), drawerState)
        }
    }
}
