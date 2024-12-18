package market.engine.widgets.exceptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun showNoItemLayout(
    icon: DrawableResource? = null,
    image : DrawableResource = drawables.notFoundListingIcon,
    title: String = stringResource(strings.notFoundListingTitle),
    textButton: String = stringResource(strings.refreshButton),
    modifier: Modifier = Modifier.fillMaxSize(),
    onRefresh: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ){
        Column {
            when{
                icon != null -> {
                    Icon(
                        painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp).align(Alignment.CenterHorizontally),
                        tint = colors.textA0AE
                    )
                }
                else -> {
                    Image(
                        painterResource(image),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally),
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimens.smallSpacer))

            Text(
                text = title,
                textAlign = TextAlign.Center,
                color = colors.darkBodyTextColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(dimens.mediumSpacer))

            TextButton(
                onClick = onRefresh,
                colors = colors.themeButtonColors,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = MaterialTheme.shapes.small
            ){
                Text(
                    text = textButton,
                    textAlign = TextAlign.Center,
                    color = colors.black
                )
            }
        }
    }
}
