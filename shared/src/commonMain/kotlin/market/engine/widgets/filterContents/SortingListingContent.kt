package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import market.engine.core.globalData.LD
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.baseFilters.Sort
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SortingListingContent(
    isRefreshing: MutableState<Boolean>,
    listingData: State<LD>,
    sheetState: BottomSheetScaffoldState,
    scope: CoroutineScope
) {
    val sortSections = listOf(
        stringResource(strings.timeToEnd) to listOf(
            Sort("session_end", "asc", stringResource(strings.sortModeIncreasing), null, null),
            Sort("session_end", "desc", stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.priceParameterName) to listOf(
            Sort("current_price", "asc", stringResource(strings.sortModeIncreasing), null, null),
            Sort("current_price", "desc", stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.numberOfBids) to listOf(
            Sort("popularity", "asc", stringResource(strings.sortModeIncreasing), null, null),
            Sort("popularity", "desc", stringResource(strings.sortModeDecreasing), null, null)
        ),
        stringResource(strings.titleParameterName) to listOf(
            Sort("title", "asc", stringResource(strings.sortModeIncreasingAlphabetically), null, null),
            Sort("title", "desc", stringResource(strings.sortModeDecreasingAlphabetically), null, null)
        ),
        stringResource(strings.offersGroupStartTSTile) to listOf(
            Sort("session_start", "asc", stringResource(strings.sortModeOldestFirst), null, null),
            Sort("session_start", "desc", stringResource(strings.sortModeNewestFirst), null, null)
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            sheetState.bottomSheetState.collapse()
                        }
                    },
                    content = {
                        Icon(
                            painterResource(drawables.closeBtn),
                            tint = colors.black,
                            contentDescription = stringResource(strings.actionClose)
                        )
                    },
                )

                Text(
                    stringResource(strings.sort),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(dimens.smallPadding)
                )
            }

            if(listingData.value.sort != null) {
                Button(
                    onClick = {
                        isRefreshing.value = true
                        listingData.value.sort = null
                        scope.launch {
                            sheetState.bottomSheetState.collapse()
                        }
                    },
                    content = {
                        Text(
                            stringResource(strings.clear),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.black
                        )
                    },
                    colors = colors.simpleButtonColors
                )
            }
        }

        AnimatedVisibility(
            sheetState.bottomSheetState.isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ){
            LazyColumn(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .fillMaxSize()
            ) {
                // For each section, display the heading and options under it
                sortSections.forEach { (sectionTitle, sortOptions) ->
                    item {
                        // Section title
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .padding(dimens.smallPadding),
                            color = colors.textA0AE
                        )
                    }

                    items(sortOptions) { sortOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isRefreshing.value = true
                                    listingData.value.sort = sortOption
                                    scope.launch {
                                        sheetState.bottomSheetState.collapse()
                                    }
                                }
                                .background(colors.white)
                                .clip(MaterialTheme.shapes.small)
                                .padding(dimens.smallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = sortOption.interpritation ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f).padding(dimens.smallPadding),
                                color = colors.black
                            )

                            if (listingData.value.sort?.key == sortOption.key && listingData.value.sort?.value == sortOption.value) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.inactiveBottomNavIconColor
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 1.dp), color = colors.textA0AE)
                    }
                }
            }
        }
    }
}
