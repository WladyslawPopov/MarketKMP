package market.engine.widgets.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.utils.convertDateOnlyYear
import market.engine.core.utils.getCurrentDate
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DateDialog(
    showDialog : Boolean,
    isSelectableDates : Boolean = false,
    onDismiss : () -> Unit,
    onSucceed : (Long) -> Unit,
) {
    AnimatedVisibility(
        showDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val selectedDate = remember { mutableStateOf<String?>(null) }
        val currentDate = getCurrentDate()

        val year = currentDate.convertDateOnlyYear().toInt()

        val selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis > currentDate.toLong()*1000
            }
        }

        val oneDayInMillis = 24 * 60 * 60 * 1000
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate.toLong()*1000 + oneDayInMillis,
            yearRange = year..(year + 100),
            selectableDates = if (isSelectableDates) selectableDates else DatePickerDefaults. AllDates
        )

        val timePickerState  = rememberTimePickerState(
            is24Hour = true
        )

        DatePickerDialog(
            colors = DatePickerDefaults.colors(
                containerColor = colors.white
            ),
            tonalElevation = 0.dp,
            properties = DialogProperties(usePlatformDefaultWidth = true),
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                SimpleTextButton(
                    text = stringResource(strings.acceptAction),
                    backgroundColor = colors.inactiveBottomNavIconColor,
                    textColor = colors.alwaysWhite,
                    onClick = {
                        if (selectedDate.value == null) {
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                selectedDate.value = selectedDateMillis.toString()
                            }
                        } else {
                            val selectedDateMillis = selectedDate.value?.toLongOrNull() ?: 0L
                            val selectedHour = timePickerState.hour
                            val selectedMinute = timePickerState.minute

                            val localDateTime = Instant
                                .fromEpochMilliseconds(selectedDateMillis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                                .atTime(selectedHour, selectedMinute)

                            val futureTimeInSeconds = localDateTime
                                .toInstant(TimeZone.currentSystemDefault())
                                .epochSeconds

                            onSucceed(futureTimeInSeconds)
                        }
                    },
                    modifier = Modifier.padding(dimens.smallPadding),
                )
            },
            dismissButton = {
                SimpleTextButton(
                    text = stringResource(strings.closeWindow),
                    backgroundColor = colors.grayLayout,
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.padding(dimens.smallPadding),
                )
            }
        ) {
            if (selectedDate.value == null) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.white,
                    ),
                    modifier = Modifier.padding(dimens.smallPadding)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        containerColor = colors.white,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                        .clip(MaterialTheme.shapes.medium),
                    layoutType = TimePickerLayoutType.Vertical
                )
            }
        }
    }
}
