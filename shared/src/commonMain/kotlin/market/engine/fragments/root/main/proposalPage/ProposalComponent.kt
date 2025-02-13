package market.engine.fragments.root.main.proposalPage

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProposalType
import org.koin.mp.KoinPlatform.getKoin


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
}

class DefaultProposalComponent(
    offerId: Long,
    proposalType: ProposalType,
    componentContext: ComponentContext,
    val navigateToOffer: (Long) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateBack: () -> Unit
) : ProposalComponent, ComponentContext by componentContext {

    private val proposalViewModel : ProposalViewModel = getKoin().get()

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

        proposalViewModel.getProposal(offerId)
    }
}
