package market.engine.fragments.root.main.profile.conversations

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.NavigationItem


fun conversationsFactory(
    componentContext: ComponentContext,
    navigationItems : List<NavigationItem>,
    navigateToMessenger : (Long) -> Unit,
    selectedId : Long? = null
    ): ConversationsComponent {
        return DefaultConversationsComponent(
            componentContext = componentContext,
            navigationItems = navigationItems,
            selectedId = selectedId,
            navigateToMessenger = navigateToMessenger
        )
    }
