package market.engine.fragments.root.main.createSubscription

import com.arkivanov.decompose.ComponentContext

fun createNewSubscriptionFactory(
    componentContext: ComponentContext,
    editId : Long?,
    navigateBack: () -> Unit,
    ): CreateNewSubscriptionComponent {
        return DefaultCreateNewSubscriptionComponent(
            componentContext,
            editId,
            navigateBack = {
                navigateBack()
            },
        )
    }
