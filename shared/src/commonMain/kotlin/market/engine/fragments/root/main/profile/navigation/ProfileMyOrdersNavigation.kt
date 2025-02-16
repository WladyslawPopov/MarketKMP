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
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.root.main.profile.myOrders.DefaultMyOrdersComponent
import market.engine.fragments.root.main.profile.myOrders.MyOrderAppBar
import market.engine.fragments.root.main.profile.myOrders.MyOrdersComponent
import market.engine.fragments.root.main.profile.myOrders.MyOrdersContent


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
    modifier: Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(
                if(typeGroup == DealTypeGroup.SELL)
                    strings.mySalesTitle
                else
                    strings.myPurchasesTitle,
                component.model.value.navigationItems
            )
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        val select = remember {
            mutableStateOf(if(typeGroup == DealTypeGroup.SELL) DealType.SELL_ALL else DealType.BUY_IN_WORK)
        }
        Column {
            MyOrderAppBar(
                select.value,
                typeGroup,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectMyOrderPage(newType)
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
}

fun itemMyOrders(
    config: MyOrderConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    selectMyOrderPage: (DealType) -> Unit,
): MyOrdersComponent {
    return DefaultMyOrdersComponent(
        componentContext = componentContext,
        type = config.dealType,
        orderSelected = config.id,
        offerSelected = { id ->
            profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate(), true))
        },
        navigateToMyOrder = {
            selectMyOrderPage(it)
        },
        navigateToUser = {
            profileNavigation.pushNew(ProfileConfig.UserScreen(it, getCurrentDate(), false))
        },
        navigateToMessenger = { dialogId ->
            if(dialogId != null)
                profileNavigation.pushNew(ProfileConfig.DialogsScreen(dialogId))
            else
                profileNavigation.replaceCurrent(ProfileConfig.ConversationsScreen)
        },
        navigateToBack = {
            profileNavigation.replaceCurrent(ProfileConfig.ProfileScreen())
        }
    )
}
