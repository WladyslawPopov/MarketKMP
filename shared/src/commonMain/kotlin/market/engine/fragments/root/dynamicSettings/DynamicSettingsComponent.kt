package market.engine.fragments.root.dynamicSettings

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
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

    fun goToVerificationPage(method : String, owner : Long?, code : String?)
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultDynamicSettingsComponent(
    settingsType : String,
    owner : Long?,
    code : String?,
    componentContext: JetpackComponentContext,
) : DynamicSettingsComponent, JetpackComponentContext by componentContext
{
    private val dynamicSettingsViewModel = viewModel {
        DynamicSettingsViewModel(
            settingsType,
            owner,
            code,
            this@DefaultDynamicSettingsComponent,
            createSavedStateHandle()
        )
    }

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

    override fun goToVerificationPage(method: String, owner: Long?, code: String?) {
        goToVerification(method, owner, code)
        lifecycle.doOnResume {
            goBack()
        }
    }
}
