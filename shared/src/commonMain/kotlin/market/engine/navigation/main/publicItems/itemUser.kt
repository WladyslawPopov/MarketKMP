package market.engine.navigation.main.publicItems

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.items.ListingData
import market.engine.fragments.user.DefaultUserComponent
import market.engine.fragments.user.UserComponent

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
