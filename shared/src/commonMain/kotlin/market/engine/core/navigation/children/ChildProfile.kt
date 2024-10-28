package market.engine.core.navigation.children

import market.engine.presentation.profile.ProfileComponent

sealed class ChildProfile {
        class ProfileChild(val component: ProfileComponent) : ChildProfile()
    }
