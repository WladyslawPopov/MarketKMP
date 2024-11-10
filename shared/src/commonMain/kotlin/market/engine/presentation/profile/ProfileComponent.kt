package market.engine.presentation.profile

import com.arkivanov.decompose.ComponentContext

interface ProfileComponent {

    fun navigateToMyOffers()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    val selectMyOffers: () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    override fun navigateToMyOffers() {
        selectMyOffers()
    }
}


