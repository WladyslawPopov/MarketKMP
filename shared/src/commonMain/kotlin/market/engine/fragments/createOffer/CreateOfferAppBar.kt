package market.engine.fragments.createOffer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.CreateOfferType
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferAppBar(
    type : CreateOfferType,
    onBackClick: () -> Unit = {},
) {
    val title = when(type){
        CreateOfferType.EDIT -> stringResource(strings.editOfferLabel)
        else -> {
            stringResource(strings.createNewOfferTitle)
        }
    }

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                TextAppBar(title)
            }
        },
        navigationIcon = {
            NavigationArrowButton {
                onBackClick()
            }
        }
    )
}
