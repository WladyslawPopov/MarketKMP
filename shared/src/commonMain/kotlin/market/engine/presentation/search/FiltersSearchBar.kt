package market.engine.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.SD
import market.engine.widgets.buttons.ActiveStringButton
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersSearchBar(
    modifier: Modifier = Modifier,
    selectedUser: MutableState<Boolean>,
    selectedUserFinished: MutableState<Boolean>,
    searchData: State<SD>,
    selectedCategory: MutableState<String?>,
    goToCategory: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.primaryColor)
            .padding(vertical = dimens.smallPadding, horizontal = dimens.smallPadding)
            .wrapContentHeight()
    ) {
        Column {
            Row {
                ActiveStringButton(
                    text = selectedCategory.value ?: stringResource(strings.categoryMain),
                    color =  colors.simpleButtonColors,
                    onClick = {
                        searchData.value.fromSearch = true
                        goToCategory()
                    }
                )

                SmallIconButton(
                    icon = drawables.filterIcon,
                    contentDescription = stringResource(strings.parameters),
                    color = colors.inactiveBottomNavIconColor,
                    modifierIconSize = modifier.size(dimens.smallIconSize),
                ){
                    goToCategory()
                }
            }
            Row {

                ActiveStringButton(
                    stringResource(strings.searchUserStringChoice),
                    if (!selectedUser.value) colors.simpleButtonColors else colors.themeButtonColors,
                    { selectedUser.value = !selectedUser.value },
                    {
                        if (searchData.value.searchUsersLots != null) {
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifierIconSize = modifier.size(dimens.extraSmallIconSize),
                            ){

                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                ActiveStringButton(
                    text = stringResource(strings.searchUserFinishedStringChoice),
                    color = if (!selectedUser.value) colors.simpleButtonColors else colors.themeButtonColors,
                    onClick = { selectedUserFinished.value = !selectedUserFinished.value  },
                )
            }
        }
    }
}
