package market.engine.fragments.root.main.profile.profileSettings.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.fragments.root.DefaultRootComponent.Companion.goToContactUs
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsComponent
import market.engine.fragments.root.main.profile.profileSettings.ProfileSettingsViewModel
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun GlobalSettings(
    component: ProfileSettingsComponent,
    viewModel: ProfileSettingsViewModel
) {
    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(
            maxItems = 1
        ),
        initialDirectory = "market/temp/"
    ) { files ->
        files?.firstOrNull()?.let { viewModel.uploadNewAvatar(it) }
    }

    LaunchedEffect(Unit){
        viewModel.getGenderSelects()
    }

    val genderSelects by viewModel.genderSelects.collectAsState()
    val choose = stringResource(strings.chooseAction)
    val male = stringResource(strings.sexMaleParameterName)
    val female = stringResource(strings.sexFemaleParameterName)

    val user = UserData.userInfo

    val selectedGender = remember {
        mutableStateOf(
            when (user?.gender) {
                "male" -> {
                    male
                }
                "female" -> {
                    female
                }
                else -> {
                    choose
                }
            }
        )
    }

    val showDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            Card(
                modifier = Modifier.wrapContentSize(),
                shape = CircleShape
            ) {
                LoadImage(
                    url = user?.avatar?.thumb?.content ?: "",
                    modifier = Modifier.size(100.dp),
                    isShowLoading = false,
                    isShowEmpty = false
                )
            }

            SimpleTextButton(
                stringResource(strings.actionChangeLabel),
                backgroundColor = colors.inactiveBottomNavIconColor
            ){
                if (UserData.userInfo?.avatar?.origin?.content != "${SAPI.SERVER_BASE}images/no_avatar.svg"){
                    showDialog.value = true
                }else{
                    launcher.launch()
                }
            }

            if(showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text(stringResource(strings.dialogChooseActionLabel)) },
                    text = {  },
                    containerColor = colors.white,
                    confirmButton = {
                        SimpleTextButton(
                            text = stringResource(strings.acceptAction),
                            backgroundColor = colors.inactiveBottomNavIconColor,
                            textColor = colors.alwaysWhite,
                            onClick = {
                                launcher.launch()
                                showDialog.value = false
                            }
                        )
                    },
                    dismissButton = {
                        SimpleTextButton(
                            text = stringResource(strings.deleteAvatarLabel),
                            backgroundColor = colors.steelBlue,
                            textColor = colors.alwaysWhite,
                            onClick = {
                                viewModel.deleteAvatar()
                                showDialog.value = false
                            }
                        )
                    }
                )
            }
        }

        SettingRow(
            stringResource(strings.loginParameterName),
            body = {
                Text(
                    user?.login ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                )
            }
        ){
            component.navigateToDynamicSettings("set_login")
        }

        SettingRow(
            stringResource(strings.promptEmail),
            body = {
                Text(
                    user?.email ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                )
            }
        ){
            component.navigateToDynamicSettings("set_email")
        }

        SettingRow(
            stringResource(strings.promptPassword),
            body = {
                Text(
                    "**************",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                )
            }
        ){
            component.navigateToDynamicSettings("set_password")
        }

        SettingRow(
            stringResource(strings.genderParameterName),
            body = {
                getDropdownMenu(
                    selectedGender.value,
                    selects = genderSelects.map { it.name?: "" },
                    selectedTextDef = selectedGender.value,
                    onItemClick = {
                        selectedGender.value = it
                    },
                    onClearItem = {
                        selectedGender.value = choose
                    },
                )
            }
        ){
            viewModel.setGender(selectedGender.value)
        }

        SettingRow(
            stringResource(strings.phoneParameterName),
            body = {
                Text(
                    user?.phone ?: stringResource(strings.notSetParameterName),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                )
                if (user?.phone != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painterResource(drawables.verifiedIcon),
                            contentDescription = null,
                            modifier = Modifier.size(dimens.mediumIconSize)
                        )
                    }
                }
            },
            onClick = if (user?.phone == null) {
                {component.navigateToDynamicSettings("set_phone")}
            }else{
                null
            }
        )

        SettingRow(
            stringResource(strings.pageAboutMeParameterName),
            body = {}
        ){
            component.navigateToDynamicSettings("set_about_me")
        }

        SettingRow(
            stringResource(strings.deleteAccountLabel),
            body = {},
            action = {
                SimpleTextButton(
                    text = stringResource(strings.actionDelete),
                    backgroundColor = colors.notifyTextColor,
                    textColor = colors.alwaysWhite,
                    onClick = {it?.invoke()}
                )
            }
        ){
            goToContactUs("delete_account")
        }
    }
}

@Composable
fun SettingRow(
    label: String,
    body: @Composable () -> Unit,
    action: @Composable ((() -> Unit)?) -> Unit = { click->
        if (click != null) {
            ActionButton(
                stringResource(strings.actionChangeLabel),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
            ) {
                click()
            }
        }
    },
    onClick: (() -> Unit)? = null
){
    Row(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.grayText
            )

            body()
        }

        action(onClick)
    }
}
