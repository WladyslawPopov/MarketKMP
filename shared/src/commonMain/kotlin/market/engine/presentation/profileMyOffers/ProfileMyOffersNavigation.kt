package market.engine.presentation.profileMyOffers

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import market.engine.core.types.LotsType
import market.engine.presentation.main.MainComponent
import market.engine.presentation.profile.ProfileDrawer


@Composable
fun ProfileMyOffersNavigation(
    component: MainComponent,
    modifier: Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer{

            }
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        ChildPages(
            pages = component.myOffersPages,
            scrollAnimation = PagesScrollAnimation.Default,
            onPageSelected = {
                val select = when(it){
                    0 -> LotsType.MYLOT_ACTIVE
                    1 -> LotsType.MYLOT_UNACTIVE
                    2 -> LotsType.MYLOT_FUTURE
                    else -> {
                        LotsType.MYLOT_ACTIVE
                    }
                }
                component.selectMyOfferPage(select)
            }
        ) { _, page ->
            MyOffersContent(
                component = page,
                drawerState,
                modifier = modifier
            )
        }
    }
}
