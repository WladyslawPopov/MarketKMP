package market.engine.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import application.market.auction_mobile.business.networkObjects.Offer
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
import org.jetbrains.compose.resources.stringResource

@Composable
fun GridPromoOffers(promoOffers : List<Offer>, onOfferClick: (Offer) -> Unit) {

    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    Spacer(modifier = Modifier.heightIn(dimens.mediumPadding))

    TitleText(strings.topOffersTitle)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(if (showNavigationRail) 4 else 2),
        modifier = Modifier
            .heightIn(200.dp, (300*promoOffers.size).dp)
            .padding(dimens.smallPadding)
            .wrapContentHeight(),
        userScrollEnabled = false,
        verticalItemSpacing = dimens.smallPadding,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        content = {
            items(promoOffers) { offer ->
                PromoLotItem(offer, onOfferClick = onOfferClick)
            }
        }
    )

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))

    ActionTextButton(strings.allPromoOffersBtn) {

    }

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))
}
