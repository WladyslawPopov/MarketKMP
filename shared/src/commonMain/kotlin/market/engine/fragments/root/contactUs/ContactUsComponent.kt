package market.engine.fragments.root.contactUs

import com.arkivanov.decompose.ComponentContext
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

class DefaultContactUsComponent(
    selectedType: String?,
    componentContext: ComponentContext
) : ContactUsComponent, ComponentContext by componentContext  {
    private val contactUsViewModel = ContactUsViewModel(this)

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
