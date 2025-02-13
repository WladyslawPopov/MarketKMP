package market.engine.fragments.root.main.proposalPage

import com.arkivanov.decompose.ComponentContext
import market.engine.core.data.types.ProposalType

fun proposalFactory(
    offerId: Long,
    proposalType: ProposalType,
    componentContext: ComponentContext,
    navigateBack: () -> Unit,
    navigateToOffer: (Long) -> Unit,
    navigateToUser: (Long) -> Unit,
): ProposalComponent {
    return DefaultProposalComponent(
        offerId,
        proposalType,
        componentContext,
        navigateToOffer,
        navigateToUser,
        navigateBack
    )
}
