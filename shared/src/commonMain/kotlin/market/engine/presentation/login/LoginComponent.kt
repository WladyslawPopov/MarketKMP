package market.engine.presentation.login

import com.arkivanov.decompose.ComponentContext

interface LoginComponent {
    fun onLogin()
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val onLoginSuccess: () -> Unit
) : LoginComponent {

    override fun onLogin() {
        onLoginSuccess()
    }
}
