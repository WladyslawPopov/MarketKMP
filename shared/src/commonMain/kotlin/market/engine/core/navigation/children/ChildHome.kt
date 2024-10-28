package market.engine.core.navigation.children

import market.engine.presentation.home.HomeComponent

sealed class ChildHome {
        class HomeChild(val component: HomeComponent) : ChildHome()
    }
