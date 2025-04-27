package market.engine.fragments.root.main.listing

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.baseFilters.ListingData

fun listingFactory(
    componentContext: ComponentContext,
    listingData: ListingData,
    selectOffer: (Long) -> Unit,
    navigateToSubscribe: () -> Unit,
    navigateToListing: (ListingData) -> Unit,
    navigateToNewSubscription: (Long?) -> Unit,
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
        navigateToSubscribe = {
            navigateToSubscribe()
        },
        navigateToListing = { ld ->
            navigateToListing(ld)
        },
        navigateToNewSubscription = {
            navigateToNewSubscription(it)
        }
    )
}
