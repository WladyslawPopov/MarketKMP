package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.AnalyticsFactory
import market.engine.common.navigateToAppSettings
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.repositories.SettingsRepository
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject


@Composable
fun AppSettingsContent() {
    val settings: SettingsRepository = koinInject()

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val isLightMode =
        remember { mutableStateOf(settings.themeMode.value != "night") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimens.largePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            SeparatorLabel(
                stringResource(strings.notificationSettingsLabel)
            )

            ActionButton(
                stringResource(strings.actionGoToNotificationsSettingsLabel),
            ) {
                navigateToAppSettings()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            SeparatorLabel(
                stringResource(strings.settingsThemeLabel)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    stringResource(strings.themeDarkLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.grayText
                )
                Icon(
                    painterResource(drawables.modeNightIcon),
                    contentDescription = null,
                    tint = colors.textA0AE,
                    modifier = Modifier.size(dimens.mediumIconSize)
                )
                Switch(
                    checked = isLightMode.value,
                    onCheckedChange = {
                        isLightMode.value = !isLightMode.value
                        settings.updateThemeMode(if (isLightMode.value) "day" else "night")

                        val eventParameters =
                            mapOf("mode_theme" to if (isLightMode.value) "day" else "night")
                        analyticsHelper.reportEvent(
                            "change_theme",
                            eventParameters
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = colors.transparent,
                        checkedThumbColor = colors.yellowSun,
                        checkedTrackColor = colors.transparentGrayColor,
                        uncheckedBorderColor = colors.transparent,
                        uncheckedThumbColor = colors.textA0AE,
                        uncheckedTrackColor = colors.transparentGrayColor,
                    ),
                )
                Icon(
                    painterResource(drawables.modeDayIcon),
                    contentDescription = null,
                    tint = colors.yellowSun,
                    modifier = Modifier.size(dimens.mediumIconSize)
                )
                Text(
                    stringResource(strings.themeLightLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.grayText
                )
            }
        }
    }
}
