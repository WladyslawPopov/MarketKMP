package market.engine.fragments.root.verifyPage

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy

interface VerificationComponent {
    val model : Value<Model>

    data class Model(
        val verificationViewModel: VerificationViewModel,
        val backHandler: BackHandler
    )
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultVerificationComponent(
    owner : Long?,
    code : String?,
    settingsType : String,
    componentContext: JetpackComponentContext,
) : VerificationComponent, JetpackComponentContext by componentContext
{
    private  val verificationViewModel = viewModel("verificationViewModel"){
        VerificationViewModel(
            owner = owner,
            code = code,
            settingsType = settingsType,
            component = this@DefaultVerificationComponent,
            savedStateHandle = createSavedStateHandle()
        )
    }

    private val _model = MutableValue(
        VerificationComponent.Model(
            verificationViewModel = verificationViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        lifecycle.doOnDestroy {
            verificationViewModel.onClear()
        }
    }
}
