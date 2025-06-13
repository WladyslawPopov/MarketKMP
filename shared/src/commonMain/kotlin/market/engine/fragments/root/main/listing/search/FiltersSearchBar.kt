package market.engine.fragments.root.main.listing.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.root.main.listing.SearchEvents
import market.engine.fragments.root.main.listing.SearchUiState
import market.engine.widgets.buttons.FilterButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersSearchBar(
    uiSearchState: SearchUiState,
    searchEvents: SearchEvents,
) {
    val us = stringResource(strings.searchUsersSearch)
    val catDef = stringResource(strings.categoryMain)

    val searchData = remember(uiSearchState.searchData){
        uiSearchState.searchData
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
                    if(searchData.searchCategoryID == 1L){
                        append(catDef)
                    }else{
                        append(searchData.searchCategoryName)
                    }
                },
                color =  if (searchData.searchCategoryID == 1L)
                    colors.simpleButtonColors
                else
                    colors.themeButtonColors,
                onClick = {
                    searchEvents.openSearchCategory(true)
                },
                onCancelClick =
                    if(searchData.searchCategoryID != 1L){
                        {
                            searchEvents.clearCategory()
                        }
                    } else null
            )
        }

        item {
            FilterButton(
                text = searchData.userLogin ?: us,
                color = if (!searchData.userSearch)
                    colors.simpleButtonColors
                else
                    colors.themeButtonColors,
                onClick = {
                    searchEvents.clickUser()
                },
                onCancelClick = if(searchData.userLogin != null){ {searchEvents.clearUser()} } else null
            )
        }

        item {
            FilterButton(
                text = stringResource(strings.searchUserFinishedStringChoice),
                color = if (!searchData.searchFinished) colors.simpleButtonColors else colors.themeButtonColors,
                onClick = {
                    searchEvents.clickUserFinished()
                },
            )
        }
    }
}
