package market.engine.widgets.grids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
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
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.WindowType
import market.engine.core.util.getWindowType
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.items.PromoOfferGridItem
import market.engine.widgets.texts.SeparatorLabel

@Composable
fun GridPromoOffers(
    promoOffers : List<Offer>,
    onOfferClick: (Offer) -> Unit,
    onAllClickButton: () -> Unit
) {

    val windowClass = getWindowType()
    val showNavigationRail = windowClass == WindowType.Big

    Spacer(modifier = Modifier.heightIn(dimens.mediumPadding))

    SeparatorLabel(strings.topOffersTitle)

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
                PromoOfferGridItem(offer, onOfferClick = onOfferClick)
            }
        }
    )

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))

    ActionButton(
        strings.allPromoOffersBtn,
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        modifier = Modifier.padding(horizontal = dimens.smallPadding).fillMaxWidth(),
        alignment = Alignment.BottomEnd
    ) {
        onAllClickButton()
    }

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))
}
