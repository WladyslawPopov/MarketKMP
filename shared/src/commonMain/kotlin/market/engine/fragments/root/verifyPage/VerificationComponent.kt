package market.engine.fragments.root.verifyPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import org.koin.mp.KoinPlatform.getKoin

interface VerificationComponent {
    val model : Value<Model>

    data class Model(
        val owner : Long?,
        val code : String?,
        var settingsType : String,
        val verificationViewModel: VerificationViewModel,
        val backHandler: BackHandler
    )

    fun onBack()
    fun goToLogin()
}

class DefaultVerificationComponent(
    val navigateLogin : () -> Unit,
    val navigateBack : () -> Unit,
    owner : Long?,
    code : String?,
    settingsType : String,
    componentContext: ComponentContext,
) : VerificationComponent, ComponentContext by componentContext
{
    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private  val verificationViewModel : VerificationViewModel = getKoin().get()

    private val _model = MutableValue(
        VerificationComponent.Model(
            owner = owner,
            code = code,
            settingsType = settingsType,
            verificationViewModel = verificationViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        verificationViewModel.userRepository.updateToken()
        if (UserData.token != "") {
            verificationViewModel.init(settingsType, owner, code)
        }else{
            navigateBack()
        }
    }

    override fun onBack() {
        navigateBack()
    }

    override fun goToLogin() {
        navigateLogin()
        lifecycle.doOnResume {
            navigateBack()
        }
    }
}
