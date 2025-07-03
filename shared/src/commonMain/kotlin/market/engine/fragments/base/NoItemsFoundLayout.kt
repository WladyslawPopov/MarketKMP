package market.engine.fragments.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun NoItemsFoundLayout(
    icon: DrawableResource? = null,
    image : DrawableResource = drawables.notFoundListingIcon,
    title: String = stringResource(strings.notFoundListingTitle),
    textButton: String = stringResource(strings.refreshButton),
    modifier: Modifier = Modifier.background(color = colors.primaryColor).fillMaxSize().padding(dimens.smallPadding),
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumSpacer)
    ) {
        Spacer(modifier = Modifier.height(dimens.largeSpacer))

        when {
            icon != null -> {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = colors.textA0AE
                )
            }

            else -> {
                Image(
                    painterResource(image),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                )
            }
        }

        Text(
            text = title,
            textAlign = TextAlign.Center,
            color = colors.darkBodyTextColor,
            style = MaterialTheme.typography.titleMedium,
        )

        TextButton(
            onClick = {
                onRefresh()
            },
            colors = colors.themeButtonColors,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = textButton,
                textAlign = TextAlign.Center,
                color = colors.black
            )
        }
    }
}
