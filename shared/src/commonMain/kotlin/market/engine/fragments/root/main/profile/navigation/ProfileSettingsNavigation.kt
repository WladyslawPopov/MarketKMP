package market.engine.fragments.root.main.profile.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.MutableValue
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.root.main.profile.profileSettings.DefaultProfileSettingsComponent
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsAppBar
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsComponent
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsContent
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
    publicProfileNavigationItems: MutableValue<List<NavigationItem>>
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val selectedTabIndex = remember { mutableStateOf(0) }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(stringResource(strings.settingsProfileTitle), publicProfileNavigationItems.value)
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        Column {
            // app bar
            ProfileSettingsAppBar(
                currentTab = selectedTabIndex.value,
                drawerState = drawerState,
                navigationClick = {
                    val type = when(it){
                        0 -> ProfileSettingsTypes.GLOBAL_SETTINGS
                        1 -> ProfileSettingsTypes.SELLER_SETTINGS
                        2 -> ProfileSettingsTypes.ADDITIONAL_SETTINGS
                        else -> {
                            ProfileSettingsTypes.GLOBAL_SETTINGS
                        }
                    }
                    component.selectProfileSettingsPage(type)
                }
            )

            ChildPages(
                pages = component.settingsPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    val type = when(it){
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
}

fun itemProfileSettings(
    config: ProfileSettingsConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    selectProfileSettingsPage: (ProfileSettingsTypes) -> Unit,
    selectDynamicSettings: (String) -> Unit
): ProfileSettingsComponent {
    return DefaultProfileSettingsComponent(
        componentContext = componentContext,
        type = config.settingsType,
        selectedPage = selectProfileSettingsPage,
        profileNavigation = profileNavigation,
        goToDynamicSettings = selectDynamicSettings
    )
}
