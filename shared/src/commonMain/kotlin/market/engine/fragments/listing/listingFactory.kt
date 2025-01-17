package market.engine.fragments.listing

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.ListingData

fun listingFactory(
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
