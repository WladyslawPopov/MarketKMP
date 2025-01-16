package market.engine.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.createOrder.CreateOrderComponent
import market.engine.fragments.createOrder.DefaultCreateOrderComponent

fun itemCreateOrder(
    componentContext: ComponentContext,
    selectedItems : Pair<Long, List<SelectedBasketItem>>,
    navigateOffer: (Long) -> Unit,
    navigateUser: (Long) -> Unit,
    navigateBack: () -> Unit
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
            }
        )
    }
