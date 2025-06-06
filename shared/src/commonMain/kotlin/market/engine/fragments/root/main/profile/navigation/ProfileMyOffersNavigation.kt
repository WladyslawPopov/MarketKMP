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
import com.arkivanov.decompose.router.stack.replaceCurrent
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.myOffers.DefaultMyOffersComponent
import market.engine.fragments.root.main.profile.myOffers.MyOffersAppBar
import market.engine.fragments.root.main.profile.myOffers.MyOffersComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.root.main.profile.myOffers.MyOffersContent
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
            MyOffersAppBar(
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
                navigationClick = { newType ->
                    component.selectOfferPage(newType)
                },
                onRefresh = {
                    component.onRefreshOffers()
                }
            )

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

fun itemMyOffers(
    config: MyOfferConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    selectMyOfferPage: (LotsType) -> Unit
): MyOffersComponent {
    return DefaultMyOffersComponent(
        componentContext = componentContext,
        type = config.lotsType,
        offerSelected = { id ->
            profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate()))
        },
        selectedMyOfferPage = { type ->
            selectMyOfferPage(type)
        },
        navigateToCreateOffer = { type, offerId, catPath ->
            profileNavigation.pushNew(
                ProfileConfig.CreateOfferScreen(
                    catPath = catPath,
                    createOfferType = type,
                    offerId = offerId,
                )
            )
        },
        navigateToBack = {
            profileNavigation.replaceCurrent(ProfileConfig.ProfileScreen())
        },
        navigateToProposal = { id, type ->
            profileNavigation.pushNew(ProfileConfig.ProposalScreen(id, type, getCurrentDate()))
        },
        navigateToDynamicSettings = { type, id ->
            goToDynamicSettings(type, id, null)
        }
    )
}
