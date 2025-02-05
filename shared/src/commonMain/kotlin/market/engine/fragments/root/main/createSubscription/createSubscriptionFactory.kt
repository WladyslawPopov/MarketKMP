package market.engine.fragments.root.main.createSubscription

import com.arkivanov.decompose.ComponentContext

fun createSubscriptionFactory(
    componentContext: ComponentContext,
    editId : Long?,
    navigateBack: () -> Unit,
    ): CreateSubscriptionComponent {
        return DefaultCreateSubscriptionComponent(
            componentContext,
            editId,
            navigateBack = {
                navigateBack()
            },
        )
    }
