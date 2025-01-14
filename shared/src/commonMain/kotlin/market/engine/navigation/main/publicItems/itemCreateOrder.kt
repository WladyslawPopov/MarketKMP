package market.engine.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.SelectedBasketItem
import market.engine.fragments.createOrder.CreateOrderComponent
import market.engine.fragments.createOrder.DefaultCreateOrderComponent

fun itemCreateOrder(
    componentContext: ComponentContext,
    basketItem : Pair<Long, List<SelectedBasketItem>>,
    navigateOffer: (Long) -> Unit,
    navigateUser: (Long) -> Unit,
    navigateBack: () -> Unit
    ): CreateOrderComponent {
        return DefaultCreateOrderComponent(
            componentContext,
            basketItem,
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
