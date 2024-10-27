package market.engine.presentation.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.koin.mp.KoinPlatform.getKoin

interface LoginComponent {
    val model: Value<Model>

    data class Model(
        val loginViewModel: LoginViewModel,
    )

    fun onLogin(email : String, password : String, captcha : String? = null)

    fun onBack()
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val onBackSelected: () -> Unit
) : LoginComponent {

    private val _model = MutableValue(
        LoginComponent.Model(
            loginViewModel = getKoin().get()
        )
    )

    override val model = _model

    override fun onLogin(email : String, password : String, captcha : String?) {
        val body = HashMap<String, String>()
        body["identity"] = email
        body["password"] = password
        body["workstation_data"] = ""
        if (captcha != "") {
            captcha?.let {
                body["captcha_key"] = model.value.loginViewModel.responseAuth.value?.captchaKey?:""
                body["captcha_response"] = it
            }
        }
        model.value.loginViewModel.postAuth(body)
    }

    override fun onBack() {
        onBackSelected()
    }
}
