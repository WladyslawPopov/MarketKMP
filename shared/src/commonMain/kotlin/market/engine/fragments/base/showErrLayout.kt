package market.engine.fragments.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun showErrLayout(err: String, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().zIndex(50f).background(color = colors.primaryColor),
        contentAlignment = Alignment.Center
    ){
        Column {
            Image(
                painterResource(drawables.oopsIcon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.5f).align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(dimens.mediumSpacer))

            Text(
                text = stringResource(strings.oopsTitle),
                textAlign = TextAlign.Center,
                color = colors.titleTextColor,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(dimens.mediumSpacer))

            Text(
                text = err,
                textAlign = TextAlign.Center,
                color = colors.steelBlue,
                style = MaterialTheme.typography.bodyMedium
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
                    text = stringResource(strings.refreshButton),
                    textAlign = TextAlign.Center,
                    color = colors.black
                )
            }
        }
    }
}
