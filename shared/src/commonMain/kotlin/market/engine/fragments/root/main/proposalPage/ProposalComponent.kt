package market.engine.fragments.root.main.proposalPage

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProposalType

interface ProposalComponent {
    val model : Value<Model>

    data class Model(
        val proposalViewModel: ProposalViewModel,
        val backHandler: BackHandler,
    )

    fun goToOffer(offerId: Long)

    fun goToUser(userId: Long)

    fun goBack()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultProposalComponent(
    offerId: Long,
    proposalType: ProposalType,
    componentContext: JetpackComponentContext,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateBack: () -> Unit
) : ProposalComponent, JetpackComponentContext by componentContext {

    private val proposalViewModel = viewModel("proposalViewModel"){
        ProposalViewModel(proposalType, offerId, this@DefaultProposalComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        ProposalComponent.Model(
            proposalViewModel = proposalViewModel,
            backHandler = backHandler
        )
    )
    override val model = _model

    override fun goToOffer(offerId: Long) {
        navigateToOffer(offerId)
    }

    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }

    override fun goBack() {
        navigateBack()
    }

    init {
        lifecycle.doOnResume {
            proposalViewModel.updateUserInfo()
            if (UserData.token == ""){
                goBack()
            }
        }
    }
}
