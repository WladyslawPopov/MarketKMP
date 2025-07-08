package market.engine.fragments.root.dynamicSettings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
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
    private val dynamicSettingsViewModel = DynamicSettingsViewModel(settingsType, owner, code, this)

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
