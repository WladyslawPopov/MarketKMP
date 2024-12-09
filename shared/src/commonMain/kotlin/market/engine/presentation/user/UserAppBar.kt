package market.engine.presentation.user

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.widgets.buttons.NavigationArrowButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAppBar(
    modifier: Modifier = Modifier,
    onBeakClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {

        },
        navigationIcon = {

        },
        actions = {
        }
    )
}
