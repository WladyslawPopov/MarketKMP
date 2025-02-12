package market.engine.fragments.root.main.proposalPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Proposal
import market.engine.core.network.networkObjects.Proposals
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.rows.UserRow
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProposalItem(
    proposals: Proposals,
    type: ProposalType,
    fields: ArrayList<Fields>?,
    refresh : () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = colors.cardColors
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (proposals.proposals?.isNotEmpty() == true){
                proposals.buyerInfo?.let { UserRow(it, Modifier.fillMaxWidth()) }

                if (fields != null) {
                    SetUpDynamicFields(fields)
                }

                proposals.proposals.forEach { proposal ->

                }
            }else{
                if (type == ProposalType.MAKE_PROPOSAL) {
                    if (fields != null) {
                        SetUpDynamicFields(fields)
                    }
                }else{
                    showNoItemLayout(
                        image = drawables.proposalIcon,
                        title = stringResource(strings.notFoundProposalsLabel),
                    ){
                        refresh()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.smallPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallSpacer, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextButton(
                    stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                ){

                }
            }
        }
    }
}
