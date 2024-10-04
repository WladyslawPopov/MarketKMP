package market.engine.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import market.engine.presentation.main.MainComponent
import market.engine.presentation.main.ProfileConfig


interface ProfileComponent {

    fun onCloseClicked()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val navigation: StackNavigation<ProfileConfig>
) : ProfileComponent, ComponentContext by componentContext {

    // Omitted code

    override fun onCloseClicked() {
        navigation.pop()
    }
}
