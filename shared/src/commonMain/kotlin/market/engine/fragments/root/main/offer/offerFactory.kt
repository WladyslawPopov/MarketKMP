package market.engine.fragments.root.main.offer

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings

fun offerFactory(
    componentContext: ComponentContext,
    id: Long,
    selectOffer: (Long) -> Unit,
    onBack : () -> Unit,
    onListingSelected: (ListingData) -> Unit,
    onUserSelected: (Long, Boolean) -> Unit,
    isSnapshot: Boolean = false,
    navigateToCreateOffer: (
        type: CreateOfferType,
        catPath : List<Long>?,
        offerId: Long,
        externalImages : List<String>?
    ) -> Unit,
    navigateToCreateOrder: (item : Pair<Long, List<SelectedBasketItem>>) -> Unit,
    navigateToLogin: () -> Unit,
    navigateToDialog: (dialogId: Long?) -> Unit,
    navigationSubscribes: () -> Unit,
    navigateToProposalPage: (offerId: Long, type: ProposalType) -> Unit
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
            navigationCreateOffer = { type, catPath, offerId, externalImages ->
                navigateToCreateOffer(type, catPath, offerId, externalImages)
            },
            navigateToCreateOrder = { item ->
                navigateToCreateOrder(item)
            },
            navigateToLogin = {
                navigateToLogin()
            },
            navigateToDialog = { dialogId ->
                navigateToDialog(dialogId)
            },
            navigationSubscribes = {
                navigationSubscribes()
            },
            navigateToProposalPage = { offerId, type ->
                navigateToProposalPage(offerId, type)
            },
            navigateDynamicSettings = { type, owner ->
                goToDynamicSettings(type, owner, null)
            }
        )
    }

