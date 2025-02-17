package market.engine.fragments.root.main.proposalPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalAppBar(
    modifier: Modifier = Modifier,
    goBack: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        TopAppBar(
            modifier = modifier
                .fillMaxWidth(),
            title = {
                Column(
                    modifier = modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    TextAppBar(stringResource(strings.proposalTitle))
                }
            },
            navigationIcon = {
                NavigationArrowButton {
                    goBack()
                }
            }
        )
    }
}
