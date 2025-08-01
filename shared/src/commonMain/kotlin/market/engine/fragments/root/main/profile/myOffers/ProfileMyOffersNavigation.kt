package market.engine.fragments.root.main.profile.myOffers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import kotlinx.serialization.Serializable
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.NavigationItemUI
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.PlatformWindowType
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.filterContents.CustomModalDrawer
import org.jetbrains.compose.resources.stringResource


@Serializable
data class MyOfferConfig(
    @Serializable
    val lotsType: LotsType
)

@Composable
fun ProfileMyOffersNavigation(
    component: ProfileChildrenComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItemUI>
) {
    CustomModalDrawer(
        modifier = modifier,
        title = stringResource(strings.myOffersTitle),
        publicProfileNavigationItems = publicProfileNavigationItems,
    ){ mod, drawerState ->
        val select = remember {
            mutableStateOf(LotsType.MY_LOT_INACTIVE)
        }

        Column(
            modifier = mod
        ) {
            DrawerAppBar(
                drawerState = drawerState,
                data = SimpleAppBarData(
                    listItems = listOf(
                        NavigationItemUI(
                            NavigationItem(
                                title = "",
                                hasNews = false,
                                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                                badgeCount = null,
                            ),
                            icon = drawables.recycleIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            onClick = {
                                component.onRefreshOffers()
                            }
                        )
                    )
                ),
                color = colors.transparent
            ){
                val tabs = remember {
                    listOf(
                        LotsType.MY_LOT_ACTIVE to strings.activeTab,
                        LotsType.MY_LOT_INACTIVE to strings.inactiveTab,
                        LotsType.MY_LOT_IN_FUTURE to strings.futureTab
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    items(tabs){ tab ->
                        SimpleTextButton(
                            stringResource(tab.second),
                            backgroundColor = if (select.value == tab.first) colors.rippleColor else colors.white,
                            textStyle = MaterialTheme.typography.bodyMedium,
                        ) {
                            component.selectOfferPage(tab.first)
                        }
                    }
                }
            }

            ChildPages(
                pages = component.myOffersPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    select.value = when (it) {
                        0 -> LotsType.MY_LOT_ACTIVE
                        1 -> LotsType.MY_LOT_INACTIVE
                        2 -> LotsType.MY_LOT_IN_FUTURE
                        else -> {
                            LotsType.MY_LOT_ACTIVE
                        }
                    }
                    component.selectOfferPage(select.value)
                }
            ) { _, page ->
                MyOffersContent(
                    component = page,
                    modifier = modifier
                )
            }
        }
    }
}

