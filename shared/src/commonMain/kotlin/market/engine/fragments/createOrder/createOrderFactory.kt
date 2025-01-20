package market.engine.fragments.createOrder

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.SelectedBasketItem

fun createOrderFactory(
    componentContext: ComponentContext,
    selectedItems : Pair<Long, List<SelectedBasketItem>>,
    navigateOffer: (Long) -> Unit,
    navigateUser: (Long) -> Unit,
    navigateBack: () -> Unit,
    navigateToMyOrders: () -> Unit
    ): CreateOrderComponent {
        return DefaultCreateOrderComponent(
            componentContext,
            selectedItems,
            navigateToOffer = { id->
                navigateOffer(id)
            },
            navigateBack = {
                navigateBack()
            },
            navigateToUser = { id->
                navigateUser(id)
            },
            navigateToMyOrders = {
                navigateBack()
                navigateToMyOrders()
            }
        )
    }
