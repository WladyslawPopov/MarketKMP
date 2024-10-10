package market.engine.widgets.exceptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun showNoItemLayout(
    title: String? = null,
    textButton: String? = null ,
    modifier: Modifier = Modifier.fillMaxSize(),
    onRefresh: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ){
        Column {
            Image(
                painterResource(drawables.notFoundListingIcon),
                contentDescription = null,
                modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(dimens.smallSpacer))

            Text(
                text = title ?: stringResource(strings.notFoundListingTitle),
                textAlign = TextAlign.Center,
                color = colors.steelBlue,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(dimens.mediumSpacer))

            TextButton(
                onClick = {
                    onRefresh()
                },
                colors = colors.themeButtonColors,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = MaterialTheme.shapes.small
            ){
                Text(
                    text = textButton ?: stringResource(strings.resetLabel),
                    textAlign = TextAlign.Center,
                    color = colors.black
                )
            }
        }
    }
}
