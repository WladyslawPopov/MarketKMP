package market.engine.fragments.root.main.user

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.DealTypeGroup

fun userFactory(
    componentContext: ComponentContext,
    userId: Long,
    isClickedAboutMe: Boolean,
    goToLogin: (ListingData) -> Unit,
    goBack: () -> Unit,
    goToSnapshot: (Long) -> Unit,
    goToUser: (Long) -> Unit,
    goToSubscriptions: () -> Unit,
    goToOrder: (Long, DealTypeGroup) -> Unit,
    ): UserComponent {
        return DefaultUserComponent(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            componentContext = componentContext,
            goToListing = goToLogin,
            navigateBack = goBack,
            navigateToOrder = { id, type ->
                goToOrder(id, type)
            },
            navigateToSnapshot = goToSnapshot,
            navigateToUser = goToUser,
            navigateToSubscriptions = {
                goToSubscriptions()
            }
        )
    }
