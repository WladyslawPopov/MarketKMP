package market.engine.fragments.root.verifyPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack

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
}

class DefaultVerificationComponent(
    owner : Long?,
    code : String?,
    settingsType : String,
    componentContext: ComponentContext,
) : VerificationComponent, ComponentContext by componentContext
{
    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private  val verificationViewModel : VerificationViewModel = VerificationViewModel()

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

    override fun onBack() {
        goBack()
    }

    init {
        verificationViewModel.userRepository.updateToken()
        if (UserData.token != "") {
            verificationViewModel.init(settingsType, owner, code)
        }else{
            goBack()
        }
        val eventParameters = mapOf("settings_type" to settingsType)
        analyticsHelper.reportEvent("view_verification_page", eventParameters)
    }
}
