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
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.root.main.profile.main.ProfileComponent
import market.engine.fragments.root.main.profile.myOffers.DefaultMyOffersComponent
import market.engine.fragments.root.main.profile.myOffers.MyOffersAppBar
import market.engine.fragments.root.main.profile.myOffers.MyOffersComponent
import market.engine.fragments.root.main.profile.main.ProfileDrawer
import market.engine.fragments.root.main.profile.myOffers.MyOffersContent


@Serializable
data class MyOfferConfig(
    @Serializable
    val lotsType: LotsType
)

@Composable
fun ProfileMyOffersNavigation(
    component: ProfileComponent,
    modifier: Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(strings.myOffersTitle, component.model.value.navigationItems)
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        val select = remember {
            mutableStateOf(LotsType.MYLOT_UNACTIVE)
        }
        Column {

            MyOffersAppBar(
                select.value,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectOfferPage(newType)
                }
            )

            ChildPages(
                pages = component.myOffersPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    select.value = when(it){
                        0 -> LotsType.MYLOT_ACTIVE
                        1 -> LotsType.MYLOT_UNACTIVE
                        2 -> LotsType.MYLOT_FUTURE
                        else -> {
                            LotsType.MYLOT_ACTIVE
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
        }
    )
}
