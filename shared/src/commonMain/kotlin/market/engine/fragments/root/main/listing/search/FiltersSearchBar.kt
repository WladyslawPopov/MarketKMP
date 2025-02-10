package market.engine.fragments.root.main.listing.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersSearchBar(
    selectedCategory: MutableState<String>,
    selectedCategoryID: MutableState<Long>,
    selectedUser: MutableState<Boolean>,
    selectedUserLogin: MutableState<String?>,
    selectedUserFinished: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    goToCategory: () -> Unit,
) {
    val us = stringResource(strings.searchUsersSearch)

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ){
        item{
            FilterButton(
                text = selectedCategory.value,
                color =  if (selectedCategoryID.value == 1L)
                    colors.simpleButtonColors
                else
                    colors.themeButtonColors,
                onClick = {
                    goToCategory()
                },
                onCancelClick = {
                    if (selectedCategoryID.value != 1L) {
                        val category = stringResource(strings.categoryMain)
                        SmallIconButton(
                            icon = drawables.cancelIcon,
                            contentDescription = stringResource(strings.actionClose),
                            color = colors.steelBlue,
                            modifier = modifier.size(dimens.smallIconSize),
                            modifierIconSize = modifier.size(dimens.smallIconSize),
                        ) {
                            selectedCategoryID.value = 1L
                            selectedCategory.value = category
                        }
                    }
                }
            )
        }

        item {
            FilterButton(
                text = selectedUserLogin.value ?: us,
                color = if (!selectedUser.value)
                    colors.simpleButtonColors
                else
                    colors.themeButtonColors,
                onClick = {
                    selectedUser.value = !selectedUser.value
                    selectedUserLogin.value = selectedUserLogin.value
                },
                onCancelClick = {
                    if (selectedUserLogin.value != null) {
                        SmallIconButton(
                            icon = drawables.cancelIcon,
                            contentDescription = stringResource(strings.actionClose),
                            color = colors.steelBlue,
                            modifier = modifier.size(dimens.smallIconSize),
                            modifierIconSize = modifier.size(dimens.smallIconSize),
                        ) {
                            selectedUser.value = false
                            selectedUserLogin.value = null
                        }
                    }
                }
            )
        }

        item {
            FilterButton(
                text = stringResource(strings.searchUserFinishedStringChoice),
                color = if (!selectedUserFinished.value) colors.simpleButtonColors else colors.themeButtonColors,
                onClick = {
                    selectedUserFinished.value = !selectedUserFinished.value
                },
            )
        }
    }
}
