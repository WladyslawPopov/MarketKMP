package market.engine.core.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.items.ListingData
import market.engine.core.types.CreateOfferTypes
import market.engine.presentation.offer.DefaultOfferComponent
import market.engine.presentation.offer.OfferComponent

fun itemOffer(
    componentContext: ComponentContext,
    id: Long,
    selectOffer: (Long) -> Unit,
    onBack : () -> Unit,
    onListingSelected: (ListingData) -> Unit,
    onUserSelected: (Long, Boolean) -> Unit,
    isSnapshot: Boolean = false,
    navigateToCreateOffer: (type: CreateOfferTypes, offerId: Long?, externalImages : List<String>?) -> Unit
): OfferComponent {
        return DefaultOfferComponent(
            id,
            isSnapshot,
            componentContext,
            selectOffer = { newId->
                selectOffer(newId)
            },
            navigationBack = {
                onBack()
            },
            navigationListing = {
                onListingSelected(it)
            },
            navigateToUser = { ui, about ->
                onUserSelected(ui, about)
            },
            navigationCreateOffer = { type, offerId, externalImages ->
                navigateToCreateOffer(type, offerId, externalImages)
            }
        )
    }

