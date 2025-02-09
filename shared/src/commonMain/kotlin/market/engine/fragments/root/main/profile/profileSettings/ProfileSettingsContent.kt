package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
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
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.dropdown_menu.getDropdownMenu
import market.engine.fragments.base.BackHandler
import market.engine.widgets.exceptions.LoadImage
import market.engine.fragments.base.onError
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSettingsContent(
    component : ProfileSettingsComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.profileSettingsViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()
    val settingsType = model.type
    val err = viewModel.errorMessage.collectAsState()

    val refresh = {
        viewModel.onError(ServerErrorException())
        viewModel.refresh()
    }

    BackHandler(model.backHandler){
        when{
            viewModel.activeFiltersType.value != "" ->{
                viewModel.activeFiltersType.value = ""
            }
            else -> {
                component.goToBack()
            }
        }
    }

    val error : @Composable () -> Unit = {
        if (err.value.humanMessage.isNotBlank()){
            onError(err.value){
                refresh()
            }
        }
    }

    BaseContent(
        modifier = Modifier.fillMaxSize(),
        isLoading = isLoading.value,
        onRefresh = {
            refresh()
        },
        error = error,
        toastItem = viewModel.toastItem
    ) {
        when(settingsType){
            ProfileSettingsTypes.GLOBAL_SETTINGS -> globalSettings(component, viewModel)
            ProfileSettingsTypes.SELLER_SETTINGS -> {}
            ProfileSettingsTypes.ADDITIONAL_SETTINGS -> {}
        }
    }
}

@Composable
fun globalSettings(
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

    Box(modifier = Modifier.background(colors.white).fillMaxSize()) {
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
                        size = 100.dp,
                        isShowLoading = false,
                        isShowEmpty = false
                    )
                }

                AcceptedPageButton(
                    strings.actionChangeLabel,
                    modifier = Modifier.wrapContentSize()
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(strings.loginParameterName),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.grayText
                )

                Text(
                    user?.login ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                ActionButton(
                    strings.actionChangeLabel,
                    modifier = Modifier.wrapContentSize(),
                ){
                    component.navigateToDynamicSettings("set_login")
                }
            }

            Divider(
                color = colors.steelBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(strings.promptEmail),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.grayText
                )

                Text(
                    user?.email ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                ActionButton(
                    strings.actionChangeLabel,
                    modifier = Modifier.wrapContentSize(),
                ){
                    component.navigateToDynamicSettings("set_email")
                }
            }

            Divider(
                color = colors.steelBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(strings.promptPassword),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.grayText
                )

                Text(
                    "**************",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                ActionButton(
                    strings.actionChangeLabel,
                    modifier = Modifier.wrapContentSize(),
                ){
                    component.navigateToDynamicSettings("set_password")
                }
            }

            Divider(
                color = colors.steelBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(strings.genderParameterName),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.grayText
                )

                getDropdownMenu(
                    selectedGender.value,
                    selects = genderSelects.map { it.name?: "" },
                    onItemClick = {
                        selectedGender.value = it
                    },
                    onClearItem = {
                        selectedGender.value = choose
                    },
                    modifier = Modifier.fillMaxWidth(0.5f)
                )

                AcceptedPageButton(
                    strings.actionSaveLabel,
                    modifier = Modifier.wrapContentSize(),
                ){
                    viewModel.setGender(selectedGender.value)
                }
            }

            Divider(
                color = colors.steelBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(strings.phoneParameterName),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.grayText
                )

                Text(
                    user?.phone ?: stringResource(strings.notSetParameterName),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                if (user?.phone == null) {
                    ActionButton(
                        strings.actionChangeLabel,
                        modifier = Modifier.wrapContentSize(),
                    ) {
                        component.navigateToDynamicSettings("set_phone")
                    }
                }else{
                    Image(
                        painterResource(drawables.verifySellersIcon),
                        contentDescription = null,
                        modifier = Modifier.size(dimens.mediumIconSize)
                    )
                }
            }

            Divider(
                color = colors.steelBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.mediumPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(strings.pageAboutMeParameterName),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.grayText
                )

                ActionButton(
                    strings.actionChangeLabel,
                    modifier = Modifier.wrapContentSize(),
                ){
                    component.navigateToDynamicSettings("set_about_me")
                }
            }

            Divider(
                color = colors.steelBlue
            )
        }
    }
}
