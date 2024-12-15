package market.engine.presentation.listing.search

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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.baseFilters.SD
import market.engine.widgets.buttons.FilterButton
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersSearchBar(
    selectedCategory: MutableState<String>,
    selectedUser: MutableState<Boolean>,
    selectedUserLogin: MutableState<String?>,
    selectedUserFinished: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    searchData: SD,
    goToCategory: () -> Unit,
) {

    val us = stringResource(strings.searchUsersSearch)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.primaryColor)
            .wrapContentHeight()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(dimens.smallPadding)
            ){
                FilterButton(
                    modifier = Modifier.padding(dimens.smallPadding),
                    selectedCategory.value,
                    color =  if (searchData.searchCategoryID == 1L)
                        colors.simpleButtonColors
                    else
                        colors.themeButtonColors,
                    onClick = {
                        goToCategory()
                    },
                    onCancelClick = {
                        if (searchData.searchCategoryID != 1L) {
                            val category = stringResource(strings.offersCategoryParameterName)
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifier = modifier.size(dimens.extraSmallIconSize),
                                modifierIconSize = modifier.size(dimens.extraSmallIconSize),
                            ) {
                                searchData.clearCategory()
                                selectedCategory.value = category
                            }
                        }
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(dimens.smallPadding)
            )
            {
                FilterButton(
                    modifier = Modifier.padding(dimens.smallPadding),
                    selectedUserLogin.value ?: us,
                    color = if (!selectedUser.value)
                        colors.simpleButtonColors
                    else
                        colors.themeButtonColors,
                    onClick = {
                        searchData.userSearch = !searchData.userSearch
                        selectedUser.value = searchData.userSearch
                        selectedUserLogin.value = searchData.userLogin
                    },
                    onCancelClick = {
                        if (selectedUserLogin.value != null) {
                            SmallIconButton(
                                icon = drawables.cancelIcon,
                                contentDescription = stringResource(strings.actionClose),
                                color = colors.steelBlue,
                                modifier = modifier.size(dimens.extraSmallIconSize),
                                modifierIconSize = modifier.size(dimens.extraSmallIconSize),
                            ) {
                                selectedUser.value = false
                                selectedUserLogin.value = null
                                searchData.userSearch = false
                                searchData.userLogin = null
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(dimens.smallSpacer))

                FilterButton(
                    modifier = Modifier.padding(dimens.smallPadding),
                    text = stringResource(strings.searchUserFinishedStringChoice),
                    color = if (!selectedUserFinished.value) colors.simpleButtonColors else colors.themeButtonColors,
                    onClick = {
                        selectedUserFinished.value = !selectedUserFinished.value
                        searchData.searchFinished = !searchData.searchFinished
                    },
                )
            }
        }
    }
}
