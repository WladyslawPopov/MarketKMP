package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItemUI
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.items.TabWithIcon
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.filterContents.CustomModalDrawer
import market.engine.widgets.tabs.PageTab
import market.engine.widgets.tabs.TabRow
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
    publicProfileNavigationItems: List<NavigationItemUI>
) {
    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }
    val selectedTabIndex = rememberSaveable { mutableStateOf(0) }

    CustomModalDrawer(
        modifier = modifier,
        title = stringResource(strings.settingsProfileTitle),
        publicProfileNavigationItems = publicProfileNavigationItems,
    ) { mod, drawerState ->
        Column {
            val globalSettings = stringResource(strings.profileGlobalSettingsLabel)
            val sellerSettings = stringResource(strings.profileSellerSettingsLabel)
            val additionalSettings = stringResource(strings.profileAdditionalSettingsLabel)

            val tabs = remember {
                listOf(
                    TabWithIcon(
                        title = globalSettings,
                    ),
                    TabWithIcon(
                        title = sellerSettings,
                    ),
                    TabWithIcon(
                        title = additionalSettings,
                    )
                )
            }
            // app bar
            DrawerAppBar(
                data = SimpleAppBarData(
                    onBackClick = if (isBigScreen.value) {
                        {
                            hideDrawer.value = !hideDrawer.value
                        }
                    }else{
                        null
                    }
                ),
                drawerState = drawerState
            ){
                TabRow(
                    tabs,
                    selectedTab = selectedTabIndex.value,
                    containerColor = colors.white,
                    modifier = Modifier.fillMaxWidth(),
                ){ index, tab ->
                    PageTab(
                        tab = tab,
                        selectedTab = selectedTabIndex.value,
                        currentIndex = index,
                        textStyle = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.clickable {
                            val type = when (index) {
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
                }
            }

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
}
