package market.engine.fragments.root.contactUs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.NavigationArrowButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsAppBar(
    modifier: Modifier = Modifier,
    onBeakClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            Text(
                stringResource(strings.contactUsHeaderLabel),
                style = MaterialTheme.typography.titleSmall,
                color = colors.black
            )
        },
        navigationIcon = {
            NavigationArrowButton {
                onBeakClick()
            }
        }
    )
}
