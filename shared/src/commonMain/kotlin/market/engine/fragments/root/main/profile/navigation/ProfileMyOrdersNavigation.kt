package market.engine.fragments.root.main.profile.navigation

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
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.root.main.profile.myOrders.MyOrderAppBar
import market.engine.fragments.root.main.profile.myOrders.MyOrdersContent
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
    val drawerState = rememberDrawerState(initialValue = if(isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }

    val content : @Composable (Modifier) -> Unit = {
        val select = remember {
            mutableStateOf(if(typeGroup == DealTypeGroup.SELL) DealType.SELL_ALL else DealType.BUY_IN_WORK)
        }
        Column {
            MyOrderAppBar(
                select.value,
                typeGroup,
                drawerState = drawerState,
                showMenu = hideDrawer.value,
                openMenu = if (isBigScreen.value) {
                    {
                        hideDrawer.value = !hideDrawer.value
                    }
                }else{
                    null
                },
                navigationClick = { newType->
                    component.selectMyOrderPage(newType)
                },
                onRefresh = {
                    component.onRefreshOrders()
                }
            )

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
                            stringResource(if(typeGroup == DealTypeGroup.SELL)
                                strings.mySalesTitle
                            else
                                strings.myPurchasesTitle),
                            publicProfileNavigationItems
                        )
                    }
                }else{
                    ProfileDrawer(
                        stringResource(if(typeGroup == DealTypeGroup.SELL)
                            strings.mySalesTitle
                        else
                            strings.myPurchasesTitle),
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
