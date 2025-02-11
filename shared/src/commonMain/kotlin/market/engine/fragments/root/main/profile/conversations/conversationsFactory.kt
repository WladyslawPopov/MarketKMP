package market.engine.fragments.root.main.profile.conversations

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.NavigationItem


fun conversationsFactory(
    componentContext: ComponentContext,
    navigationItems : List<NavigationItem>,
    navigateToMessenger : (Long) -> Unit,
    navigateBack : () -> Unit,
    ): ConversationsComponent {
        return DefaultConversationsComponent(
            componentContext = componentContext,
            navigationItems = navigationItems,
            navigateToMessenger = navigateToMessenger,
            navigateBack = navigateBack
        )
    }
