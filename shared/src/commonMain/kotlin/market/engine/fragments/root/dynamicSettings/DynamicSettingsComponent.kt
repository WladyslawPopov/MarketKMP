package market.engine.fragments.root.dynamicSettings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.fragments.root.DefaultRootComponent.Companion.goBack
import market.engine.fragments.root.DefaultRootComponent.Companion.goToVerification

interface DynamicSettingsComponent {
    val model : Value<Model>

    data class Model(
        val owner : Long?,
        val code : String?,
        var settingsType : String,
        val dynamicSettingsViewModel: DynamicSettingsViewModel,
        val backHandler: BackHandler
    )
    fun updateModel()
    fun onBack()
    fun goToVerificationPage(method : String, owner : Long?, code : String?)
}

class DefaultDynamicSettingsComponent(
    settingsType : String,
    owner : Long?,
    code : String?,
    componentContext: ComponentContext,
) : DynamicSettingsComponent, ComponentContext by componentContext
{

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private  val dynamicSettingsViewModel : DynamicSettingsViewModel = DynamicSettingsViewModel()

    private val _model = MutableValue(
        DynamicSettingsComponent.Model(
            owner = owner,
            code = code,
            settingsType = settingsType,
            dynamicSettingsViewModel = dynamicSettingsViewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    override fun updateModel() {
        dynamicSettingsViewModel.init(model.value.settingsType, model.value.owner)
    }

    init {
        updateModel()

        val eventParameters = mapOf(
            "user_id" to UserData.login,
            "profile_source" to "settings"
        )
        analyticsHelper.reportEvent("view_$settingsType", eventParameters)
    }

    override fun onBack() {
        goBack()
    }

    override fun goToVerificationPage(method: String, owner: Long?, code: String?) {
        goToVerification(method, owner, code)
        lifecycle.doOnResume {
            onBack()
        }
    }
}
