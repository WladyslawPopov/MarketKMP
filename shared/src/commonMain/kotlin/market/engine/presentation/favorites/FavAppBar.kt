package market.engine.presentation.favorites

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.widgets.texts.TitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesAppBar(
    title : String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
           TitleText(title)
        }
    )
}
