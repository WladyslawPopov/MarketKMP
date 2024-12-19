package market.engine.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.CreateOfferType
import market.engine.fragments.offer.DefaultOfferComponent
import market.engine.fragments.offer.OfferComponent

fun itemOffer(
    componentContext: ComponentContext,
    id: Long,
    selectOffer: (Long) -> Unit,
    onBack : () -> Unit,
    onListingSelected: (ListingData) -> Unit,
    onUserSelected: (Long, Boolean) -> Unit,
    isSnapshot: Boolean = false,
    navigateToCreateOffer: (type: CreateOfferType, offerId: Long?, externalImages : List<String>?) -> Unit
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

