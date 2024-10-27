package market.engine.presentation.login

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.stringResource

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
            TitleText(title)
        },
        navigationIcon = {
            IconButton(
                modifier = modifier,
                onClick = {
                    onBeakClick()
                }
            ){
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(strings.menuTitle),
                    modifier = modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
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
