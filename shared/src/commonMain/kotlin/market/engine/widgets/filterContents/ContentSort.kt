package market.engine.widgets.filterContents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ContentSort(
    currentSort: Sort?,
    sortSections: List<Pair<String, List<Sort>>>,
    selectItem: (Sort) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(dimens.smallPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        FilterContentHeaderBar(
            stringResource(strings.sort),
            currentSort != null,
            onClosed = onClose,
            onClear = {
                onClear()
                onClose()
            }
        )

        LazyColumnWithScrollBars(
            heightMod = Modifier.fillMaxWidth(if (isBigScreen.value) 0.7f else 1f).padding(top = 60.dp),
            modifierList = Modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
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
                                selectItem(sortOption)
                            }
                            .background(colors.white, MaterialTheme.shapes.small)
                            .clip(MaterialTheme.shapes.small)
                            .padding(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = sortOption.interpretation?.split(":")?.lastOrNull() ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(dimens.smallPadding),
                            color = colors.black
                        )

                        if (currentSort?.key == sortOption.key && currentSort.value == sortOption.value) {
                            Icon(
                                painterResource(drawables.checkIcon),
                                contentDescription = null,
                                tint = colors.inactiveBottomNavIconColor
                            )
                        }
                    }
                }
            }
        }
    }
}
