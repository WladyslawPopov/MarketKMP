package market.engine.fragments.messenger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.User
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.rows.UserSimpleRow
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogsAppBar(
    user: User?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    goToUser: (Long) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.white,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            NavigationArrowButton {
                onBack()
            }
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            if (user != null) {
                UserSimpleRow(
                    user = user,
                    modifier = Modifier.clickable {
                        goToUser(user.id)
                    }.fillMaxWidth()
                )
            }else{
                TextAppBar(
                    stringResource(strings.messageTitle)
                )
            }
        },
        actions = {

        }
    )
}
