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
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.myBids.MyBidsAppBar
import market.engine.fragments.root.main.profile.myBids.MyBidsContent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.root.main.profile.myBids.DefaultMyBidsComponent
import market.engine.fragments.root.main.profile.myBids.MyBidsComponent
import org.jetbrains.compose.resources.stringResource


@Serializable
data class MyBidsConfig(
    @Serializable
    val lotsType: LotsType
)

@Composable
fun ProfileMyBidsNavigation(
    component: ProfileChildrenComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val drawerState = rememberDrawerState(initialValue = if(isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }

    val content : @Composable (Modifier) -> Unit = {
        val select = remember {
            mutableStateOf(LotsType.MY_BIDS_ACTIVE)
        }
        Column {
            MyBidsAppBar(
                select.value,
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
                    component.selectOfferPage(newType)
                },
                onRefresh = {
                    component.onRefreshBids()
                }
            )

            ChildPages(
                pages = component.myBidsPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    select.value = when(it){
                        0 -> LotsType.MY_BIDS_ACTIVE
                        1 -> LotsType.MY_BIDS_INACTIVE
                        else -> {
                            LotsType.MY_BIDS_ACTIVE
                        }
                    }
                    component.selectOfferPage(select.value)
                }
            ) { _, page ->
                MyBidsContent(
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
                            stringResource(strings.myBidsTitle),
                            publicProfileNavigationItems
                        )
                    }
                }else{
                    ProfileDrawer(
                        stringResource(strings.myBidsTitle),
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
                profileNavigation.pushNew(ProfileConfig.DialogsScreen(dialogId, null, getCurrentDate()))
            else
                profileNavigation.replaceAll(ProfileConfig.ConversationsScreen())
        },
        navigateBack = {
            profileNavigation.replaceCurrent(ProfileConfig.ProfileScreen())
        }
    )
}
