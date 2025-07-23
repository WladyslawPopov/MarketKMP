package market.engine.fragments.root.main.proposalPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProposalType

interface ProposalComponent {
    val model : Value<Model>

    data class Model(
        val proposalViewModel: ProposalViewModel
    )

    fun goToOffer(offerId: Long)

    fun goToUser(userId: Long)

    fun goBack()
}

class DefaultProposalComponent(
    offerId: Long,
    proposalType: ProposalType,
    componentContext: ComponentContext,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateBack: () -> Unit
) : ProposalComponent, ComponentContext by componentContext {

    private val proposalViewModel = ProposalViewModel(proposalType, offerId, this)

    private val _model = MutableValue(
        ProposalComponent.Model(
            proposalViewModel = proposalViewModel
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
