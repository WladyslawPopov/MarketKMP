package market.engine.fragments.root.verifyPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack

interface VerificationComponent {
    val model : Value<Model>

    data class Model(
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
    private  val verificationViewModel : VerificationViewModel = VerificationViewModel(
        owner = owner,
        code = code,
        settingsType = settingsType,
        component = this
    )

    private val _model = MutableValue(
        VerificationComponent.Model(
            verificationViewModel = verificationViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    override fun onBack() {
        goBack()
    }
}
