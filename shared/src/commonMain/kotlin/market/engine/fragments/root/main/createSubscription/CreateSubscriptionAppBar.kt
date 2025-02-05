package market.engine.fragments.root.main.createSubscription

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.widgets.buttons.NavigationArrowButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSubscriptionAppBar(
    title : String,
    onBackClick: () -> Unit = {},
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            NavigationArrowButton {
                onBackClick()
            }
        }
    )
}
