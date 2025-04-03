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
import com.arkivanov.decompose.value.MutableValue
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.ProfileChildrenComponent
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.root.main.profile.myProposals.DefaultMyProposalsComponent
import market.engine.fragments.root.main.profile.myProposals.MyProposalsAppBar
import market.engine.fragments.root.main.profile.myProposals.MyProposalsComponent
import market.engine.fragments.root.main.profile.myProposals.MyProposalsContent
import org.jetbrains.compose.resources.stringResource

@Serializable
data class MyProposalsConfig(
    @Serializable
    val lotsType: LotsType
)

@Composable
fun ProfileMyProposalsNavigation(
    component: ProfileChildrenComponent,
    modifier: Modifier,
    publicProfileNavigationItems: MutableValue<List<NavigationItem>>
) {
    val drawerState = rememberDrawerState(initialValue = if(isBigScreen) DrawerValue.Open else DrawerValue.Closed)

    val hideDrawer = remember { mutableStateOf(isBigScreen) }

    val content : @Composable (Modifier) -> Unit = {
        val select = remember {
            mutableStateOf(LotsType.ALL_PROPOSAL)
        }
        Column {
            MyProposalsAppBar(
                select.value,
                drawerState = drawerState,
                showMenu = hideDrawer.value,
                openMenu = if (isBigScreen) {
                    {
                        hideDrawer.value = !hideDrawer.value
                    }
                }else{
                    null
                },
                navigationClick = { newType->
                    component.selectOfferPage(newType)
                }
            )

            ChildPages(
                pages = component.myProposalsPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    select.value = when(it){
                        0 -> LotsType.ALL_PROPOSAL
                        1 -> LotsType.NEED_RESPOSE
                        else -> {
                            LotsType.ALL_PROPOSAL
                        }
                    }
                    component.selectOfferPage(select.value)
                }
            ) { _, page ->
                MyProposalsContent(
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
                if (isBigScreen) {
                    AnimatedVisibility(hideDrawer.value) {
                        ProfileDrawer(stringResource(strings.proposalTitle), publicProfileNavigationItems.value)
                    }
                }else{
                    ProfileDrawer(stringResource(strings.proposalTitle), publicProfileNavigationItems.value)
                }

                if (isBigScreen) {
                    content(Modifier.weight(1f))
                }
            }
        },
        gesturesEnabled = drawerState.isOpen && !isBigScreen,
    ) {
        if(!isBigScreen) {
            content(Modifier.fillMaxWidth())
        }
    }
}

fun itemMyProposals(
    config: MyProposalsConfig,
    componentContext: ComponentContext,
    profileNavigation: StackNavigation<ProfileConfig>,
    selectMyProposalPage: (LotsType) -> Unit
): MyProposalsComponent {
    return DefaultMyProposalsComponent(
        componentContext = componentContext,
        type = config.lotsType,
        offerSelected = { id ->
            profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate()))
        },
        selectedMyProposalsPage = {
            selectMyProposalPage(it)
        },
        navigateToUser = { userId ->
            profileNavigation.pushNew(ProfileConfig.UserScreen(userId, getCurrentDate(), false))
        },
        navigateToDialog = { dialogId ->
            if (dialogId != null)
                profileNavigation.pushNew(ProfileConfig.DialogsScreen(dialogId, null, getCurrentDate()))
            else
                profileNavigation.replaceAll(ProfileConfig.ConversationsScreen())
        },
        navigateBack = {
            profileNavigation.replaceCurrent(ProfileConfig.ProfileScreen())
        },
        navigateToProposal = { offerId, proposalType ->
            profileNavigation.pushNew(ProfileConfig.ProposalScreen(offerId, proposalType, getCurrentDate()))
        }
    )
}
