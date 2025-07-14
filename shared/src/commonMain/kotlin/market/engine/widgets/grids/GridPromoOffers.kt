package market.engine.widgets.grids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.OfferItem
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.items.offer_Items.PromoOfferGridItem
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun GridPromoOffers(
    promoOffers : List<OfferItem>,
    onOfferClick: (Long) -> Unit,
    onAllClickButton: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
    ) {
        SeparatorLabel(stringResource(strings.topOffersTitle))
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 4 else 2),
            modifier = Modifier
                .heightIn(200.dp, (300*promoOffers.size).dp),
            userScrollEnabled = false,
            verticalItemSpacing = dimens.smallPadding,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            content = {
                items(promoOffers, key = { it.id }) { offer ->
                    PromoOfferGridItem(offer, onOfferClick = onOfferClick)
                }
            }
        )
        ActionButton(
            stringResource(strings.allPromoOffersBtn),
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.padding(horizontal = dimens.smallPadding).fillMaxWidth(),
            alignment = Alignment.BottomEnd
        ) {
            onAllClickButton()
        }
    }
}
