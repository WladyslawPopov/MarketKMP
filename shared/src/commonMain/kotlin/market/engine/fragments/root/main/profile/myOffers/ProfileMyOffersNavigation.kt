package market.engine.fragments.root.main.profile.myOffers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
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
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.PlatformWindowType
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.buttons.SimpleTextButton
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
    publicProfileNavigationItems: List<NavigationItem>
) {
    val drawerState = rememberDrawerState(initialValue = if(isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }

    val content : @Composable (Modifier) -> Unit = { mod ->
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
                        NavigationItem(
                            title = "",
                            icon = drawables.recycleIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                            badgeCount = null,
                            onClick = {
                                component.onRefreshOffers()
                            }
                        ),
                    ),
                    color = colors.primaryColor,
                )
            ){
                val active = stringResource(strings.activeTab)
                val inactive = stringResource(strings.inactiveTab)
                val future = stringResource(strings.futureTab)

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    item {
                        SimpleTextButton(
                            active,
                            backgroundColor = if (select.value == LotsType.MY_LOT_ACTIVE) colors.rippleColor else colors.white,
                            textStyle = MaterialTheme.typography.bodySmall
                        ) {
                            component.selectOfferPage(LotsType.MY_LOT_ACTIVE)
                        }
                    }
                    item {
                        SimpleTextButton(
                            inactive,
                            if (select.value == LotsType.MY_LOT_INACTIVE) colors.rippleColor else colors.white,
                            textStyle = MaterialTheme.typography.bodySmall
                        ) {
                            component.selectOfferPage(LotsType.MY_LOT_INACTIVE)
                        }
                    }
                    item {
                        SimpleTextButton(
                            future,
                            if (select.value == LotsType.MY_LOT_IN_FUTURE) colors.rippleColor else colors.white,
                            textStyle = MaterialTheme.typography.bodySmall
                        ) {
                            component.selectOfferPage(LotsType.MY_LOT_IN_FUTURE)
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
                            stringResource(strings.myOffersTitle),
                            publicProfileNavigationItems
                        )
                    }
                }else{
                    ProfileDrawer(
                        stringResource(strings.myOffersTitle),
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
        if(!isBigScreen.value) {
            content(Modifier.fillMaxWidth())
        }
    }
}

