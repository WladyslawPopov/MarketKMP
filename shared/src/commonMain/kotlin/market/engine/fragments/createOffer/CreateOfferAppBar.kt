package market.engine.fragments.createOffer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.CreateOfferType
import market.engine.widgets.buttons.NavigationArrowButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferAppBar(
    type: CreateOfferType,
    titleField : String?,
    onBackClick: () -> Unit = {},
) {
    val title = when(type){
        CreateOfferType.EDIT -> stringResource(strings.editOfferLabel)
        CreateOfferType.CREATE -> {
            stringResource(strings.createNewOfferTitle)
        }
        else -> {
            titleField ?: stringResource(strings.createNewOfferTitle)
        }
    }

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            NavigationArrowButton {
                onBackClick()
            }
        }
    )
}
