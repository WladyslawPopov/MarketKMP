package market.engine.fragments.user.feedbacks

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbackTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        stringResource(strings.allFeedbackToUserLabel),
        stringResource(strings.fromBuyerLabel),
        stringResource(strings.fromSellerLabel),
        stringResource(strings.fromUsersLabel),
        stringResource(strings.aboutMeLabel))

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(text = title, style = MaterialTheme.typography.titleSmall) }
            )
        }
    }
}
