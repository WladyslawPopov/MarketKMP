package market.engine.ui.profile

import com.arkivanov.decompose.ComponentContext


interface ProfileComponent {

    fun onCloseClicked()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    // Omitted code

    override fun onCloseClicked() {
        onBackPressed()
    }
}
