package market.engine.fragments.root.contactUs

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack

interface ContactUsComponent {
    val model: Value<Model>

    data class Model(
        val selectedType: String?,
        val contactUsViewModel: ContactUsViewModel,
        val backHandler: BackHandler
    )

    fun onBack()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultContactUsComponent(
    selectedType: String?,
    componentContext: JetpackComponentContext
) : ContactUsComponent, JetpackComponentContext by componentContext  {

    private val contactUsViewModel = viewModel(
        key = "ContactUsViewModel"
    ) {
        ContactUsViewModel(this@DefaultContactUsComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        ContactUsComponent.Model(
            selectedType = selectedType,
            contactUsViewModel = contactUsViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model


    override fun onBack() {
        goBack()
    }
}
