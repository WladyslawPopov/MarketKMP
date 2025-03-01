package market.engine.fragments.root.main.messenger

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.DealTypeGroup

fun messengerFactory(
    componentContext: ComponentContext,
    dialogId : Long,
    message : String? = null,
    navigateBack : () -> Unit,
    navigateToOrder : (Long, DealTypeGroup) -> Unit,
    navigateToOffer : (Long) -> Unit,
    navigateToUser : (Long) -> Unit,
    navigateToListingSelected : (ListingData) -> Unit
) : DialogsComponent {
    return DefaultDialogsComponent(
        componentContext = componentContext,
        dialogId = dialogId,
        message = message,
        navigateBack = {
            navigateBack()
        },
        navigateToOrder = { id, type ->
            navigateToOrder(id, type)
        },
        navigateToUser = {
            navigateToUser(it)
        },
        navigateToOffer = {
            navigateToOffer(it)
        },
        navigateToListingSelected = {
            navigateToListingSelected(it)
        }
    )
}
