package market.engine.widgets.bars.appBars

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.widgets.buttons.SmallIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseAppBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationIcon = {
            SmallIconButton(
                drawables.closeBtn,
                colors.black
            ) {
                onBackClick()
            }
        }
    )
}
