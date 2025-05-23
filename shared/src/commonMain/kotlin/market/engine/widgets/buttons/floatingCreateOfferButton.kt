package market.engine.widgets.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun floatingCreateOfferButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    FloatingActionButton(
        modifier = modifier.wrapContentSize(),
        containerColor = colors.grayLayout,
        contentColor = colors.grayText,
        onClick = {
            onClick()
        },
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            hoveredElevation = 2.dp,
            focusedElevation = 2.dp
        ),
    ){
        Icon(
            tint = colors.actionItemColors,
            painter = painterResource(drawables.newLotIcon),
            contentDescription = stringResource(strings.createNewOfferTitle),
            modifier = Modifier.size(dimens.mediumIconSize)
        )
    }
}
