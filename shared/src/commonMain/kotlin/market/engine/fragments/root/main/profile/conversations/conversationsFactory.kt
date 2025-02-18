package market.engine.fragments.root.main.profile.conversations

import com.arkivanov.decompose.ComponentContext


fun conversationsFactory(
    componentContext: ComponentContext,
    navigateToMessenger : (Long) -> Unit,
    navigateBack : () -> Unit,
    ): ConversationsComponent {
        return DefaultConversationsComponent(
            componentContext = componentContext,
            navigateToMessenger = navigateToMessenger,
            navigateBack = navigateBack
        )
    }
