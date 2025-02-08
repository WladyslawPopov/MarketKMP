package market.engine.fragments.root.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.SAPI
import org.koin.mp.KoinPlatform.getKoin

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

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val navigateToRegistration: () -> Unit,
    private val navigateToForgotPassword: () -> Unit,
    private val onBackSelected: () -> Unit
) : LoginComponent, ComponentContext by componentContext  {

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()


    private val _model = MutableValue(
        LoginComponent.Model(
            loginViewModel = getKoin().get(),
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        analyticsHelper.reportEvent("view_login_screen", mapOf())
    }

    override fun goToRegistration() {
        navigateToRegistration()
    }

    override fun goToForgotPassword() {
        navigateToForgotPassword()
    }

    override fun onBack() {
        onBackSelected()
    }
}
