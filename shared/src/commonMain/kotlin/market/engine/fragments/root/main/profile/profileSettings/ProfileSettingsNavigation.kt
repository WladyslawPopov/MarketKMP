package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import org.jetbrains.compose.resources.stringResource

@Serializable
data class ProfileSettingsConfig(
    @Serializable
    val settingsType: ProfileSettingsTypes
)

@Composable
fun ProfileSettingsNavigation(
    component: ProfileChildrenComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val drawerState =
        rememberDrawerState(initialValue = if (isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }
    val selectedTabIndex = remember { mutableStateOf(0) }
    val content: @Composable (Modifier) -> Unit = {
        Column {
            // app bar
            ProfileSettingsAppBar(
                currentTab = selectedTabIndex.value,
                drawerState = drawerState,
                showMenu = hideDrawer.value,
                openMenu = if (isBigScreen.value) {
                    {
                        hideDrawer.value = !hideDrawer.value
                    }
                }else{
                    null
                },
                navigationClick = {
                    val type = when (it) {
                        0 -> ProfileSettingsTypes.GLOBAL_SETTINGS
                        1 -> ProfileSettingsTypes.SELLER_SETTINGS
                        2 -> ProfileSettingsTypes.ADDITIONAL_SETTINGS
                        else -> {
                            ProfileSettingsTypes.GLOBAL_SETTINGS
                        }
                    }
                    component.selectProfileSettingsPage(type)
                },
            )

            ChildPages(
                pages = component.settingsPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    val type = when (it) {
                        0 -> ProfileSettingsTypes.GLOBAL_SETTINGS
                        1 -> ProfileSettingsTypes.SELLER_SETTINGS
                        2 -> ProfileSettingsTypes.ADDITIONAL_SETTINGS
                        else -> {
                            ProfileSettingsTypes.GLOBAL_SETTINGS
                        }
                    }
                    selectedTabIndex.value = it
                    component.selectProfileSettingsPage(type)
                }
            ) { _, page ->
                ProfileSettingsContent(
                    component = page,
                )
            }
        }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBigScreen.value) {
                    AnimatedVisibility(hideDrawer.value) {
                        ProfileDrawer(
                            stringResource(strings.settingsProfileTitle),
                            publicProfileNavigationItems
                        )
                    }
                } else {
                    ProfileDrawer(
                        stringResource(strings.settingsProfileTitle),
                        publicProfileNavigationItems
                    )
                }

                if (isBigScreen.value) {
                    content(Modifier.weight(1f))
                }
            }
        },
        gesturesEnabled = drawerState.isOpen && !isBigScreen.value,
    ) {
        if (!isBigScreen.value) {
            content(Modifier.fillMaxWidth())
        }
    }
}
