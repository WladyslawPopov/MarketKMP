package market.engine.presentation.profileMyOffers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.types.LotsType
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMyOffersAppBar(
    currentTab : LotsType,
    modifier: Modifier = Modifier,
    navigationClick : (LotsType) -> Unit,
) {
    val active = stringResource(strings.activeTab)
    val inactive = stringResource(strings.inactiveTab)
    val future = stringResource(strings.futureTab)

    val isTypeSelected = remember { mutableStateOf(currentTab) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        modifier = modifier
            .fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.largePadding, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ){

                SimpleTextButton(
                    active,
                    backgroundColor = if (isTypeSelected.value == LotsType.MYLOT_ACTIVE) colors.rippleColor else colors.white,
                ){
                    isTypeSelected.value = LotsType.MYLOT_ACTIVE
                    navigationClick(isTypeSelected.value)

                }

                SimpleTextButton(
                    inactive,
                    if (isTypeSelected.value == LotsType.MYLOT_UNACTIVE) colors.rippleColor else colors.white,
                ){
                    isTypeSelected.value = LotsType.MYLOT_UNACTIVE
                    navigationClick(isTypeSelected.value)
                }

                SimpleTextButton(
                    future,
                    if (isTypeSelected.value == LotsType.MYLOT_FUTURE) colors.rippleColor else colors.white,
                ){
                    isTypeSelected.value = LotsType.MYLOT_FUTURE
                    navigationClick(isTypeSelected.value)
                }
            }
        }
    )
}
