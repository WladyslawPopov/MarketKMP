package market.engine.fragments.base.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.widgets.items.offer_Items.PromoOfferRowItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.LazyRowWithScrollBars
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoItemsFoundLayout(
    icon: DrawableResource? = null,
    image : DrawableResource = drawables.notFoundListingIcon,
    title: String = stringResource(strings.notFoundListingTitle),
    textButton: String = stringResource(strings.refreshButton),
    modifier: Modifier = Modifier,
    viewModel: CoreViewModel,
    goToOffer: ((OfferItem) -> Unit)? = null,
    onRefresh: () -> Unit
) {
    val scrollState = rememberLazyScrollState(viewModel)

    LaunchedEffect(Unit){

        val listHistory = viewModel.responseHistory.value

        if(listHistory.isEmpty()) {
            viewModel.getHistory()
        }
    }

    LazyColumnWithScrollBars(
        state = scrollState.scrollState,
        containerModifier = modifier
            .background(color = colors.primaryColor)
            .zIndex(100f),
        listModifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumSpacer)
    )
    {
        item {  }

        item {
            when {
            icon != null -> {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = colors.textA0AE
                )
            }

            else -> {
                Image(
                    painterResource(image),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                )
            }
        }
        }

        item {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                color = colors.darkBodyTextColor,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item {
            TextButton(
                onClick = {
                    onRefresh()
                },
                colors = colors.themeButtonColors,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = textButton,
                    textAlign = TextAlign.Center,
                    color = colors.black
                )
            }
        }
        if(goToOffer != null) {
            // visited list offers
            item {
                val offerVisitedHistory by viewModel.responseHistory.collectAsState()

                if (offerVisitedHistory.isNotEmpty()) {
                    Column {
                        SeparatorLabel(stringResource(strings.lastViewedOffers))

                        LazyRowWithScrollBars(
                            heightMod = Modifier.fillMaxSize().heightIn(max = 300.dp).padding(
                                bottom = dimens.largePadding,
                                top = dimens.mediumPadding
                            ),
                        ) {
                            items(offerVisitedHistory) { offer ->
                                PromoOfferRowItem(
                                    offer
                                ) {
                                    goToOffer(offer)
                                }
                            }
                        }
                    }
                }
            }

            //recommended list offers
            item {
                val ourChoiceList by viewModel.responseOurChoice.collectAsState()

                if (ourChoiceList.isNotEmpty()) {
                    Column {
                        SeparatorLabel(stringResource(strings.ourChoice))

                        LazyRowWithScrollBars(
                            heightMod = Modifier.fillMaxSize().heightIn(max = 300.dp).padding(
                                bottom = dimens.largePadding,
                                top = dimens.mediumPadding
                            ),
                        ) {
                            items(ourChoiceList) { offer ->
                                PromoOfferRowItem(
                                    offer
                                ) {
                                    goToOffer(offer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
