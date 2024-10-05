package market.engine.widgets.grids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import market.engine.core.networkObjects.Offer
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.widgets.buttons.ActionTextButton
import market.engine.widgets.items.PromoLotItem
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.stringResource

@Composable
fun GridPromoOffers(promoOffers : List<Offer>, onOfferClick: (Offer) -> Unit) {

    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    Spacer(modifier = Modifier.heightIn(dimens.mediumPadding))

    TitleText(stringResource(strings.topOffersTitle))

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

    ActionTextButton(
        strings.allPromoOffersBtn,
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        modifier = Modifier.padding(horizontal = dimens.smallPadding).fillMaxWidth(),
        alignment = Alignment.BottomEnd
    ) {

    }

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))
}
