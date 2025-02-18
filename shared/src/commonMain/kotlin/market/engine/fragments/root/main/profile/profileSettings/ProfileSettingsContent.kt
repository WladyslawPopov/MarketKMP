package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
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

    val sellerSettingsItems = listOf(
        NavigationItem(
            title = stringResource(strings.pageAboutMeParameterName),
            icon = drawables.infoIcon,
            onClick = {
                component.navigateToDynamicSettings("set_about_me")
            }
        ),
        NavigationItem(
            title = stringResource(strings.vacationLabel),
            icon = drawables.vacationIcon,
            onClick = {
                component.navigateToDynamicSettings("set_vacation")
            }
        ),
        NavigationItem(
            title = stringResource(strings.messageToBuyersLabel),
            icon = drawables.dialogIcon,
            onClick = {
                component.navigateToDynamicSettings("set_message_to_buyer")
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsBiddingStepsLabel),
            icon = drawables.listIcon,
            onClick = {
                component.navigateToDynamicSettings("set_bidding_step")
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsAutoFeedbacksLabel),
            icon = drawables.timerListIcon,
            onClick = {
                component.navigateToDynamicSettings("set_auto_feedback")
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsWatermarkLabel),
            icon = drawables.watermarkIcon,
            onClick = {
                component.navigateToDynamicSettings("set_watermark")
            }
        ),
    )

    val addressItems = listOf(
        NavigationItem(
            title = stringResource(strings.outgoingAddressLabel),
            icon = drawables.locationIcon,
            onClick = {
                component.navigateToDynamicSettings("set_outgoing_address")
            }
        ),
        NavigationItem(
            title = stringResource(strings.addressCardsTitle),
            icon = drawables.emptyOffersIcon,
            onClick = {
                component.navigateToDynamicSettings("set_address_cards")
            }
        ),
    )

    val blackListItems = listOf(
        NavigationItem(
            title = stringResource(strings.settingsBlackListSellersLabel),
            icon = drawables.blackSellersIcon,
            onClick = {
                component.navigateToDynamicSettings("add_to_seller_blacklist")
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsBlackListBuyersLabel),
            icon = drawables.blackBuyersIcon,
            onClick = {
                component.navigateToDynamicSettings("add_to_buyer_blacklist")
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsWhiteListBuyersLabel),
            icon = drawables.whiteBuyersIcon,
            onClick = {
                component.navigateToDynamicSettings("add_to_whitelist")
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsBlockRatingLabel),
            icon = drawables.blockRatingIcon,
            onClick = {
                component.navigateToDynamicSettings("set_block_rating")
            }
        )
    )

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
            onError(err){
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
        ) {
            item {
                when(settingsType){
                    ProfileSettingsTypes.GLOBAL_SETTINGS -> globalSettings(component, viewModel)
                    ProfileSettingsTypes.SELLER_SETTINGS -> settingsContent(
                        stringResource(strings.sellerSettingsTitle),
                        sellerSettingsItems
                    )
                    ProfileSettingsTypes.ADDITIONAL_SETTINGS -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            settingsContent(
                                stringResource(strings.addressesSettingsTitle),
                                addressItems
                            )
                            settingsContent(
                                stringResource(strings.blackListSettingsTitle),
                                blackListItems
                            )
                        }
                    }
                }
            }
        }
    }
}
