package market.engine.core.navigation.main.children

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.types.LotsType
import market.engine.core.navigation.main.MainComponent
import market.engine.widgets.exceptions.ProfileDrawer
import market.engine.presentation.profileMyOffers.MyOffersContent
import market.engine.presentation.profileMyOffers.ProfileMyOffersAppBar


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
            ProfileDrawer(strings.myOffersTitle, component.profileNavigationList.value)
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
