package market.engine.core.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.items.ListingData
import market.engine.presentation.listing.DefaultListingComponent
import market.engine.presentation.listing.ListingComponent

fun itemListing(
    componentContext: ComponentContext,
    listingData: ListingData,
    selectOffer: (Long) -> Unit,
    onBack : () -> Unit,
    isOpenSearch : Boolean = false,
    isOpenCategory : Boolean
    ): ListingComponent {
        return DefaultListingComponent(
            isOpenSearch = isOpenSearch,
            isOpenCategory = isOpenCategory,
            componentContext = componentContext,
            listingData = listingData,
            selectOffer = { id ->
                selectOffer(id)
            },
            selectedBack = {
                onBack()
            },
        )
    }
