package market.engine.fragments.root.main.listing.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.FilterButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersSearchBar(
    selectedCategory: MutableState<String>,
    selectedCategoryID: MutableState<Long>,
    selectedUser: MutableState<Boolean>,
    selectedUserLogin: MutableState<String?>,
    selectedUserFinished: MutableState<Boolean>,
    goToCategory: () -> Unit,
    clearCategory: () -> Unit,
) {
    val us = stringResource(strings.searchUsersSearch)
    val catDef = stringResource(strings.categoryMain)

    val clearUser = remember {
        {
            selectedUser.value = false
            selectedUserLogin.value = null
        }
    }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ){
        item {  }
        item{
            FilterButton(
                text = buildString {
                    if(selectedCategoryID.value == 1L){
                        append(catDef)
                    }else{
                        append(selectedCategory.value)
                    }
                },
                color =  if (selectedCategoryID.value == 1L)
                    colors.simpleButtonColors
                else
                    colors.themeButtonColors,
                onClick = {
                    goToCategory()
                },
                onCancelClick = if(selectedCategoryID.value != 1L) clearCategory else null
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
                onCancelClick = if(selectedUserLogin.value != null) clearUser else null
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
