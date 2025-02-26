package market.engine.fragments.root.main.profile.conversations

import com.arkivanov.decompose.ComponentContext


fun conversationsFactory(
    copyMessage: String?,
    componentContext: ComponentContext,
    navigateToMessenger : (Long, String?) -> Unit,
    navigateBack : () -> Unit,
    ): ConversationsComponent {
        return DefaultConversationsComponent(
            copyMessage = copyMessage,
            componentContext = componentContext,
            navigateToMessenger = navigateToMessenger,
            navigateBack = navigateBack
        )
    }
