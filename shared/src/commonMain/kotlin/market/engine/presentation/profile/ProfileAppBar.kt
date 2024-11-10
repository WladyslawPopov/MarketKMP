package market.engine.presentation.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.strings
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAppBar(
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TitleText(stringResource(strings.profileTitle))
        },
    )
}
