package market.engine.presentation.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import market.engine.core.types.LotsType
import market.engine.presentation.main.MainComponent
import market.engine.presentation.profileMyOffers.MyOffersContent

@Composable
fun ProfileMyOffersNavigation(
    component: MainComponent,
    modifier: Modifier
) {

    ChildPages(
        pages = component.myOffersPages,
        scrollAnimation = PagesScrollAnimation.Default,
        onPageSelected = {
            when(it){
                0 -> component.selectMyOfferPage(LotsType.MYLOT_ACTIVE)
                1 -> component.selectMyOfferPage(LotsType.MYLOT_UNACTIVE)
                2 -> component.selectMyOfferPage(LotsType.MYLOT_FUTURE)
            }
        }
    ) { index, page ->
        when (index) {
            0 -> {
                if (page.model.value.type == LotsType.MYLOT_ACTIVE) {
                    MyOffersContent(
                        component = page,
                        modifier = modifier
                    )
                }
            }

            1 -> {
                if (page.model.value.type == LotsType.MYLOT_UNACTIVE){
                    MyOffersContent(
                        component = page,
                        modifier = modifier
                    )
                }
            }

            2 -> {
                if (page.model.value.type == LotsType.MYLOT_FUTURE) {
                    MyOffersContent(
                        component = page,
                        modifier = modifier
                    )
                }
            }
        }
    }
}
