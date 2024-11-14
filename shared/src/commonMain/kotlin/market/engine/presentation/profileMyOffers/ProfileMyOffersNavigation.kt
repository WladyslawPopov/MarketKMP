package market.engine.presentation.profileMyOffers

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import kotlinx.coroutines.launch
import market.engine.core.types.LotsType
import market.engine.presentation.main.MainComponent
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.presentation.profile.ProfileDrawer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileMyOffersNavigation(
    component: MainComponent,
    modifier: Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainViewModel : MainViewModel = koinViewModel()

    fun openMenu(state : DrawerValue) {
        scope.launch {
            if (state == DrawerValue.Closed) {
                drawerState.open()
            }else{
                drawerState.close()
            }
        }
    }

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

                mainViewModel.sendEvent(
                    UIMainEvent.UpdateTopBar {
                        ProfileMyOffersAppBar(
                            select,
                            navigationClick = { newType->
                                component.selectMyOfferPage(newType)
                            }
                        ) {
                            openMenu(drawerState.targetValue)
                        }

                        component.selectMyOfferPage(select)
                    }
                )
            }
        ) { _, page ->
            MyOffersContent(
                component = page,
                modifier = modifier
            )
        }
    }
}
