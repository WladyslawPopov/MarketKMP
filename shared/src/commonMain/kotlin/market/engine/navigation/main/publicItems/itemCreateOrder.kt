package market.engine.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.fragments.createOrder.CreateOrderComponent
import market.engine.fragments.createOrder.DefaultCreateOrderComponent

fun itemCreateOrder(
    componentContext: ComponentContext,
    navigateOffer: (Long) -> Unit,
    navigateBack: () -> Unit
    ): CreateOrderComponent {
        return DefaultCreateOrderComponent(
            componentContext,
            navigateToOffer = { id->
                navigateOffer(id)
            },
            navigateBack = {
                navigateBack()
            }
        )
    }
