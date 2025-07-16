package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
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
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.PlatformWindowType
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.filterContents.CustomModalDrawer
import org.jetbrains.compose.resources.stringResource

@Serializable
data class MyOrderConfig(
    @Serializable
    val dealType: DealType,

    @Serializable
    val id: Long? = null
)

@Composable
fun ProfileMyOrdersNavigation(
    typeGroup: DealTypeGroup,
    component: ProfileChildrenComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    CustomModalDrawer(
        modifier = modifier,
        title = stringResource(if(typeGroup == DealTypeGroup.SELL)
            strings.mySalesTitle
        else
            strings.myPurchasesTitle),
        publicProfileNavigationItems = publicProfileNavigationItems,
    ) { mod, drawerState ->
        val select = remember {
            mutableStateOf(if(typeGroup == DealTypeGroup.SELL) DealType.SELL_ALL else DealType.BUY_IN_WORK)
        }
        Column {
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
                    )
                ),
                color = colors.transparent
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    val tabs = when (typeGroup){
                        DealTypeGroup.BUY -> {
                            listOf(
                                DealType.BUY_IN_WORK to strings.tabInWorkLabel,
                                DealType.BUY_ARCHIVE to strings.tabArchiveLabel
                            )
                        }
                        DealTypeGroup.SELL -> {
                            listOf(
                                DealType.SELL_ALL to strings.tabAllLabel,
                                DealType.SELL_IN_WORK to strings.tabInWorkLabel,
                                DealType.SELL_ARCHIVE to strings.tabArchiveLabel,
                            )
                        }
                    }

                    tabs.forEach { tab ->
                        item {
                            SimpleTextButton(
                                stringResource(tab.second),
                                backgroundColor = if (select.value == tab.first) colors.rippleColor else colors.white,
                                textStyle = MaterialTheme.typography.bodyMedium,
                            ) {
                                component.selectMyOrderPage(tab.first)
                            }
                        }
                    }
                }
            }

            ChildPages(
                pages = component.myOrdersPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    when(typeGroup){
                        DealTypeGroup.BUY -> {
                            select.value = when(it){
                                0 -> DealType.BUY_IN_WORK
                                1 -> DealType.BUY_ARCHIVE
                                else -> DealType.BUY_IN_WORK
                            }
                        }
                        DealTypeGroup.SELL -> {
                            select.value = when(it){
                                0 -> DealType.SELL_ALL
                                1 -> DealType.SELL_IN_WORK
                                2 -> DealType.SELL_ARCHIVE
                                else -> DealType.SELL_ALL
                            }
                        }
                    }
                    component.selectMyOrderPage(select.value)
                }
            ) { _, page ->
                MyOrdersContent(
                    component = page,
                    modifier = modifier
                )
            }
        }
    }
}
