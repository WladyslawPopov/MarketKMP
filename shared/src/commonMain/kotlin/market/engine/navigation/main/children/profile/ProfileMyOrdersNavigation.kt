package market.engine.navigation.main.children.profile

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
import kotlinx.serialization.Serializable
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.LotsType
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.profile.ProfileComponent
import market.engine.fragments.profileMyOffers.DefaultMyOffersComponent
import market.engine.fragments.profileMyOffers.MyOffersComponent
import market.engine.widgets.exceptions.ProfileDrawer
import market.engine.fragments.profileMyOffers.MyOffersContent
import market.engine.fragments.profileMyOffers.ProfileMyOffersAppBar
import market.engine.fragments.profileMyOrders.DefaultMyOrdersComponent
import market.engine.fragments.profileMyOrders.MyOrdersComponent
import market.engine.fragments.profileMyOrders.MyOrdersContent
import market.engine.fragments.profileMyOrders.ProfileMyOrdersAppBar


@Serializable
data class MyOrderConfig(
    @Serializable
    val dealType: DealType,
    @Serializable
    val groupType : DealTypeGroup
)

@Composable
fun ProfileMyOrdersNavigation(
    component: ProfileComponent,
    modifier: Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(strings.myPurchasesSubTitle, component.model.value.navigationItems)
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        val select = remember {
            mutableStateOf(DealType.SELL_ALL)
        }
        Column {

            ProfileMyOrdersAppBar(
                select.value,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectMyOfferPage(newType)
                }
            )

            ChildPages(
                pages = component.myOrdersPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
//                    select.value = when(it){
//                        0 -> LotsType.MYLOT_ACTIVE
//                        1 -> LotsType.MYLOT_UNACTIVE
//                        2 -> LotsType.MYLOT_FUTURE
//                        else -> {
//                            LotsType.MYLOT_ACTIVE
//                        }
//                    }
//                    component.selectMyOfferPage(select.value)
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
): MyOrdersComponent {
    return DefaultMyOrdersComponent(
        componentContext = componentContext,
        type = config.dealType,
        offerSelected = { id ->
            profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate()))
        },
        navigateToCreateOffer = { type, offerId, catPath ->
            profileNavigation.pushNew(
                ProfileConfig.CreateOfferScreen(
                    catPath = catPath,
                    createOfferType = type,
                    offerId = offerId,
                )
            )
        }
    )
}
