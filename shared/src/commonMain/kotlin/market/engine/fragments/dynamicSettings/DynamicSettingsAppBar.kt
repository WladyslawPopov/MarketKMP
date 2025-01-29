package market.engine.fragments.dynamicSettings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TextAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicAppBar(
    title: String = "",
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            NavigationArrowButton {
                navigateBack()
            }
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextAppBar(title)
        }
    )
}
