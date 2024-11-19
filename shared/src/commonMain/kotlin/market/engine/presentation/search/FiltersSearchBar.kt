package market.engine.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.baseFilters.SD
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            )
            {
                ActiveStringButton(
                    selectedCategory.value ?: stringResource(strings.categoryMain),
                    color =  if (searchData.value.searchCategoryID == 1L)
                        colors.simpleButtonColors
                    else
                        colors.themeButtonColors,
                    onClick = {
                        goToCategory()
                    },
                    onCancelClick = {
                        if (searchData.value.searchCategoryID != 1L) {
                            val category = stringResource(strings.categoryMain)
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifier = modifier.size(dimens.smallIconSize),
                                modifierIconSize = modifier.size(dimens.extraSmallIconSize),
                            ) {
                                selectedCategory.value = category
                                searchData.value.clearCategory()
                            }
                        }
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            )
            {
                ActiveStringButton(
                    if (!selectedUser.value)
                        searchData.value.userLogin ?: stringResource(strings.searchUsersSearch)
                    else
                        searchData.value.userLogin ?: stringResource(strings.searchUsersSearch),
                    color = if (!selectedUser.value)
                        colors.simpleButtonColors
                    else
                        colors.themeButtonColors,
                    onClick = {
                        selectedUser.value = !selectedUser.value
                        searchData.value.userSearch = !searchData.value.userSearch
                    },
                    onCancelClick = {
                        if (searchData.value.userLogin != null && searchData.value.userSearch) {
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifier = modifier.size(dimens.smallIconSize),
                                modifierIconSize = modifier.size(dimens.extraSmallIconSize),
                            ) {
                                selectedUser.value = false
                                searchData.value.userSearch = false
                                searchData.value.userLogin = null
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                ActiveStringButton(
                    text = stringResource(strings.searchUserFinishedStringChoice),
                    color = if (!selectedUserFinished.value) colors.simpleButtonColors else colors.themeButtonColors,
                    onClick = {
                        selectedUserFinished.value = !selectedUserFinished.value
                        searchData.value.searchFinished = !searchData.value.searchFinished
                    },
                )
            }
        }
    }
}
