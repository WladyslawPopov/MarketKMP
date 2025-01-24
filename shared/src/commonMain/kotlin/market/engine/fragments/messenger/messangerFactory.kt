package market.engine.fragments.messenger

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.types.DealTypeGroup

fun messengerFactory(
    componentContext: ComponentContext,
    dialogId : Long,
    navigateBack : () -> Unit,
    navigateToOrder : (Long, DealTypeGroup) -> Unit,
    navigateToOffer : (Long) -> Unit,
    navigateToUser : (Long) -> Unit
) : DialogsComponent {
    return DefaultDialogsComponent(
        componentContext = componentContext,
        dialogId = dialogId,
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
    )
}
