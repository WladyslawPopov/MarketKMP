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
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.main.ProfileComponent
import market.engine.fragments.root.main.profile.myBids.DefaultMyBidsComponent
import market.engine.fragments.root.main.profile.myBids.MyBidsAppBar
import market.engine.fragments.root.main.profile.myBids.MyBidsComponent
import market.engine.fragments.root.main.profile.myBids.MyBidsContent
import market.engine.fragments.root.main.profile.main.ProfileDrawer


@Serializable
data class MyBidsConfig(
    @Serializable
    val lotsType: LotsType
)

@Composable
fun ProfileMyBidsNavigation(
    component: ProfileComponent,
    modifier: Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(strings.myBidsTitle, component.model.value.navigationItems)
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        val select = remember {
            mutableStateOf(LotsType.MYBIDLOTS_ACTIVE)
        }
        Column {

            MyBidsAppBar(
                select.value,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectMyBidsPage(newType)
                }
            )

            ChildPages(
                pages = component.myBidsPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    select.value = when(it){
                        0 -> LotsType.MYBIDLOTS_ACTIVE
                        1 -> LotsType.MYBIDLOTS_UNACTIVE
                        else -> {
                            LotsType.MYBIDLOTS_ACTIVE
                        }
                    }
                    component.selectMyBidsPage(select.value)
                }
            ) { _, page ->
                MyBidsContent(
                    component = page,
                    modifier = modifier
                )
            }
        }
    }
}

fun itemMyBids(
    config: MyBidsConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    selectMyBidsPage: (LotsType) -> Unit
): MyBidsComponent {
    return DefaultMyBidsComponent(
        componentContext = componentContext,
        type = config.lotsType,
        offerSelected = { id ->
            profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate()))
        },
        selectedMyBidsPage = { type ->
            selectMyBidsPage(type)
        },
        navigateToUser = { userId ->
            profileNavigation.pushNew(ProfileConfig.UserScreen(userId, getCurrentDate(), false))
        },
        navigateToPurchases = {
            profileNavigation.replaceCurrent(ProfileConfig.MyOrdersScreen(DealTypeGroup.BUY))
        },
        navigateToDialog = { dialogId ->
            if (dialogId != null)
                profileNavigation.pushNew(ProfileConfig.DialogsScreen(dialogId))
            else
                profileNavigation.pushNew(ProfileConfig.ConversationsScreen)
        },
        navigateBack = {
            profileNavigation.replaceCurrent(ProfileConfig.ProfileScreen())
        }
    )
}
