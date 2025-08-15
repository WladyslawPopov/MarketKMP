package market.engine.fragments.root.main.notificationsHistory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.utils.getDeepLinkByType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.items.NotificationsHistoryItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotificationsHistoryContent(
    component : NotificationsHistoryComponent,
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.notificationsHistoryViewModel

    val isLoading by viewModel.isShowProgress.collectAsState()
    val err by viewModel.errorMessage.collectAsState()

    val responseGetPage by viewModel.responseGetPage.collectAsState()
    val toastItem by viewModel.toastItem.collectAsState()

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage.isNotBlank()) {
            {
                OnError(err)
                {
                    viewModel.refresh()
                }
            }
        } else {
            null
        }
    }

    val noFound = remember(responseGetPage) {
        if (responseGetPage.isEmpty()) {
            @Composable {
                NoItemsFoundLayout(
                    viewModel = viewModel,
                    goToOffer = { offer ->
                        component.goToOffer(offer.id)
                    }
                ) {
                    viewModel.getPage()
                }
            }
        } else {
            null
        }
    }

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = SimpleAppBarData(
                    listItems = listOf(
                        NavigationItem(
                            title = "",
                            hasNews = false,
                            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                            badgeCount = null,
                            icon = drawables.recycleIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            onClick = viewModel::getPage
                        )
                    ),
                    onBackClick = component::onBackClicked
                )
            ){
                Text(
                    text = stringResource(strings.notificationsHistoryTitle),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        onRefresh = viewModel::getPage,
        error = error,
        noFound = noFound,
        isLoading = isLoading,
        toastItem = toastItem,
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            listModifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            items(responseGetPage, key = { i -> i.id }) { item ->
                NotificationsHistoryItem(item) {
                    val link = item.getDeepLinkByType()
                    if (link != null) {
                        component.goToDeepLink(link)
                    }else{
                        viewModel.deleteNotification(item.id)
                        component.onBackClicked()
                    }
                }
            }
        }
    }
}
