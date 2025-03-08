package market.engine.fragments.root.main.proposalPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException


interface ProposalComponent {
    val model : Value<Model>

    data class Model(
        val offerId: Long,
        val proposalType: ProposalType,
        val proposalViewModel: ProposalViewModel
    )

    fun goToOffer(offerId: Long)

    fun goToUser(userId: Long)

    fun goBack()

    fun update()
}

class DefaultProposalComponent(
    val offerId: Long,
    proposalType: ProposalType,
    componentContext: ComponentContext,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateBack: () -> Unit
) : ProposalComponent, ComponentContext by componentContext {

    private val proposalViewModel : ProposalViewModel = ProposalViewModel()

    private val _model = MutableValue(
        ProposalComponent.Model(
            offerId = offerId,
            proposalType = proposalType,
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

    override fun update() {
        proposalViewModel.onError(ServerErrorException())
        proposalViewModel.getProposal(
            offerId,
            onSuccess = { body ->
                proposalViewModel.body.value = body
            },
            error = {
                goBack()
            }
        )
    }

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        when(proposalType){
            ProposalType.ACT_ON_PROPOSAL -> analyticsHelper.reportEvent("view_act_on_proposal_page", mapOf())
            ProposalType.MAKE_PROPOSAL -> analyticsHelper.reportEvent("view_make_proposal_page", mapOf())
        }

        lifecycle.doOnResume {
            proposalViewModel.updateUserInfo()
            if (UserData.token == ""){
                goBack()
            }
        }
        update()

    }
}
