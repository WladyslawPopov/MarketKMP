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
import market.engine.core.data.types.LotsType
import market.engine.core.util.getCurrentDate
import market.engine.fragments.profile.ProfileComponent
import market.engine.fragments.profileMyOffers.DefaultMyOffersComponent
import market.engine.fragments.profileMyOffers.MyOffersComponent
import market.engine.widgets.exceptions.ProfileDrawer
import market.engine.fragments.profileMyOffers.MyOffersContent
import market.engine.fragments.profileMyOffers.ProfileMyOffersAppBar


@Serializable
data class MyOfferConfig(
    @Serializable
    val type: LotsType
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

            ProfileMyOffersAppBar(
                select.value,
                drawerState = drawerState,
                navigationClick = { newType->
                    component.selectMyOfferPage(newType)
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
                    component.selectMyOfferPage(select.value)
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
        type = config.type,
        offerSelected = { id ->
            profileNavigation.pushNew(ProfileConfig.OfferScreen(id, getCurrentDate()))
        },
        selectedMyOfferPage = { type ->
            selectMyOfferPage(type)
        },
        navigateToCreateOffer = { type, offerId ->
            profileNavigation.pushNew(
                ProfileConfig.CreateOfferScreen(
                    categoryId = 1L,
                    type = type,
                    offerId = offerId,
                )
            )
        }
    )
}
