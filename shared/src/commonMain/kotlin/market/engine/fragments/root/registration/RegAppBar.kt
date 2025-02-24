package market.engine.fragments.root.registration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TextAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationAppBar(
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
        }
    )
}
