package market.engine.fragments.root.main.user

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.ListingData

fun userFactory(
    componentContext: ComponentContext,
    userId: Long,
    isClickedAboutMe: Boolean,
    goToLogin: (ListingData) -> Unit,
    goBack: () -> Unit,
    goToSnapshot: (Long) -> Unit,
    goToUser: (Long) -> Unit,
    goToSubscriptions: () -> Unit,
    goToOrder: (Long) -> Unit,
    ): UserComponent {
        return DefaultUserComponent(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            componentContext = componentContext,
            goToListing = goToLogin,
            navigateBack = goBack,
            navigateToOrder = {
                goToOrder(it)
            },
            navigateToSnapshot = goToSnapshot,
            navigateToUser = goToUser,
            navigateToSubscriptions = {
                goToSubscriptions()
            }
        )
    }
