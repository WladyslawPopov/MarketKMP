package market.engine.fragments.root.main.profile.conversations

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.NavigationItem


fun conversationsFactory(
    componentContext: ComponentContext,
    navigationItems : List<NavigationItem>,
    ): ConversationsComponent {
        return DefaultConversationsComponent(
            componentContext = componentContext,
            navigationItems = navigationItems
        )
    }
