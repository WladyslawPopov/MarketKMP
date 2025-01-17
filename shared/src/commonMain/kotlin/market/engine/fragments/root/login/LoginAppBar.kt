package market.engine.fragments.root.login

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TextAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginAppBar(
    title : String,
    modifier: Modifier = Modifier,
    onBeakClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextAppBar(title)
        },
        navigationIcon = {
            NavigationArrowButton {
                onBeakClick()
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {

            }
        }
    )
}
