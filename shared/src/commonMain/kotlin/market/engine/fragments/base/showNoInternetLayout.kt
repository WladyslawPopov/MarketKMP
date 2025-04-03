package market.engine.fragments.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun showNoInternetLayout(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.background(color = colors.primaryColor).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.mediumSpacer)
    ) {
        Spacer(modifier = Modifier.height(dimens.largeSpacer))

        Image(
            painterResource(drawables.noInternetIcon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.5f).align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(strings.noInternetTitle),
            textAlign = TextAlign.Center,
            color = colors.titleTextColor,
            style = MaterialTheme.typography.titleMedium
        )


        Text(
            text = stringResource(strings.noInternetSubtitle),
            textAlign = TextAlign.Center,
            color = colors.steelBlue,
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(
            onClick = {
                onRefresh()
            },
            colors = colors.themeButtonColors,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = stringResource(strings.refreshButton),
                textAlign = TextAlign.Center,
                color = colors.black
            )
        }
    }
}
