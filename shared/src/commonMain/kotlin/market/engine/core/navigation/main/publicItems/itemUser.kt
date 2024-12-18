package market.engine.core.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.items.ListingData
import market.engine.presentation.user.DefaultUserComponent
import market.engine.presentation.user.UserComponent

fun itemUser(
    componentContext: ComponentContext,
    userId: Long,
    isClickedAboutMe: Boolean,
    goToLogin: (ListingData) -> Unit,
    goBack: () -> Unit,
    goToSnapshot: (Long) -> Unit,
    goToUser: (Long) -> Unit,
    ): UserComponent {
        return DefaultUserComponent(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            componentContext = componentContext,
            goToListing = goToLogin,
            navigateBack = goBack,
            navigateToOrder = {

            },
            navigateToSnapshot = goToSnapshot,
            navigateToUser = goToUser
        )
    }
