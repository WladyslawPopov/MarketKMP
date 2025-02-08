package market.engine.fragments.root.contactUs

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import market.engine.common.AnalyticsFactory
import org.koin.mp.KoinPlatform.getKoin

interface ContactUsComponent {
    val model: Value<Model>

    data class Model(
        val contactUsViewModel: ContactUsViewModel,
        val backHandler: BackHandler
    )

    fun onBack()
}

class DefaultContactUsComponent(
    componentContext: ComponentContext,
    private val onBackSelected: () -> Unit
) : ContactUsComponent, ComponentContext by componentContext  {

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private val contactUsViewModel = getKoin().get<ContactUsViewModel>()
    private val _model = MutableValue(
        ContactUsComponent.Model(
            contactUsViewModel = contactUsViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        contactUsViewModel.getFields()
        analyticsHelper.reportEvent("open_support_form", mapOf())
    }

    override fun onBack() {
        onBackSelected()
    }
}
