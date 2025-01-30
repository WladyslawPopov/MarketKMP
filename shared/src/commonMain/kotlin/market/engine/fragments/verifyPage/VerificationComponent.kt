package market.engine.fragments.verifyPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import org.koin.mp.KoinPlatform.getKoin

interface VerificationComponent {
    val model : Value<Model>

    data class Model(
        var settingsType : String,
        val verificationViewModel: VerificationViewModel,
    )

    fun onBack()
}

class DefaultVerificationComponent(
    val navigateBack : () -> Unit,
    settingsType : String,
    componentContext: ComponentContext,
) : VerificationComponent, ComponentContext by componentContext
{
    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    private  val verificationViewModel : VerificationViewModel = getKoin().get()

    private val _model = MutableValue(
        VerificationComponent.Model(
            settingsType = settingsType,
            verificationViewModel = verificationViewModel
        )
    )

    override val model = _model

    init {
        verificationViewModel.userRepository.updateToken()
        if (UserData.token != "") {
            verificationViewModel.init(settingsType)
        }else{
            navigateBack()
        }
    }

    override fun onBack() {
        navigateBack()
    }
}
