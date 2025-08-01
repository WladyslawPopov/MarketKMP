package market.engine.fragments.root.login

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack
import market.engine.fragments.root.DefaultRootComponent.Companion.goToMain

interface LoginComponent {
    val model: Value<Model>

    data class Model(
        val loginViewModel: LoginViewModel,
        val backHandler: BackHandler
    )

    fun goToRegistration()
    fun goToForgotPassword()
    fun onBack()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultLoginComponent(
    componentContext: JetpackComponentContext,
    private val isReset: Boolean,
    private val navigateToRegistration: () -> Unit,
    private val navigateToForgotPassword: () -> Unit,
) : LoginComponent, JetpackComponentContext by componentContext  {

    val viewModel : LoginViewModel = viewModel("loginViewModel") {
        LoginViewModel(this@DefaultLoginComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        LoginComponent.Model(
            loginViewModel = viewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        viewModel.analyticsHelper.reportEvent("view_login_screen", mapOf())
    }

    override fun goToRegistration() {
        navigateToRegistration()
    }

    override fun goToForgotPassword() {
        navigateToForgotPassword()
    }

    override fun onBack() {
        if (!isReset) {
            goBack()
        }else{
            goToMain()
        }
    }
}
