package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.MutableState
import market.engine.core.data.baseFilters.LD
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SortingOrdersContent(
    isRefreshing: MutableState<Boolean>,
    listingData: LD,
    onClose: () -> Unit,
) {
    val sortSections = listOf(
        stringResource(strings.sortCreationDate) to listOf(
            Sort("created_ts", "asc", stringResource(strings.sortModeOldestFirst), null, null),
            Sort("created_ts", "desc", stringResource(strings.sortModeNewestFirst), null, null)
        ),
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
                        onClose()
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

            if(listingData.sort != null) {
                Button(
                    onClick = {
                        isRefreshing.value = true
                        listingData.sort = null
                        onClose()
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
            true,
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
                                    listingData.sort = sortOption
                                    onClose()
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

                            if (listingData.sort?.key == sortOption.key && listingData.sort?.value == sortOption.value) {
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
