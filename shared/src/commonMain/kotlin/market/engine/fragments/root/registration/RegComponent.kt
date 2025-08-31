package market.engine.fragments.root.registration

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack

interface RegistrationComponent {
    val model: Value<Model>

    data class Model(
        val regViewModel: RegViewModel,
        val backHandler: BackHandler
    )

    fun onBack()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultRegistrationComponent(
    componentContext: JetpackComponentContext
) : RegistrationComponent, JetpackComponentContext by componentContext {

    private val regViewModel = viewModel("regViewModel"){
        RegViewModel(createSavedStateHandle())
    }

    private val _model = MutableValue(
        RegistrationComponent.Model(
            regViewModel = regViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    override fun onBack() {
        goBack()
    }

    init {
        lifecycle.doOnDestroy {
            regViewModel.onClear()
        }
    }
}
