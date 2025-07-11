package market.engine.fragments.root.main.profile.conversations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.PlatformWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.listing.ListingBaseContent
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.filterContents.DialogsFilterContent
import market.engine.widgets.filterContents.SortingOrdersContent
import market.engine.widgets.items.ConversationItem
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationsContent(
    component: ConversationsComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel

    val listingBaseViewModel = viewModel.listingBaseViewModel

    val listingDataState = listingBaseViewModel.listingData.collectAsState()

    val listingData = listingDataState.value.data

    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = viewModel.updateItem.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }

    val toastItem = viewModel.toastItem.collectAsState()

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { OnError(err.value) {  } }
        }else{
            null
        }
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
                    NoItemsFoundLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        listingBaseViewModel.clearAllFilters()
                        viewModel.updatePage()
                    }
                } else {
                    NoItemsFoundLayout(
                        title = stringResource(strings.simpleNotFoundLabel),
                        icon = drawables.dialogIcon
                    ) {
                        viewModel.updatePage()
                    }
                }
            }
        } else {
            null
        }
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    val drawerState = rememberDrawerState(initialValue = if(isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val content : @Composable (Modifier) -> Unit = {
        BaseContent(
            topBar = {
                DrawerAppBar(
                    drawerState = drawerState,
                    data = SimpleAppBarData(
                        listItems = listOf(
                            NavigationItem(
                                title = "",
                                icon = drawables.recycleIcon,
                                tint = colors.inactiveBottomNavIconColor,
                                hasNews = false,
                                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                                badgeCount = null,
                                onClick = {
                                    viewModel.updatePage()
                                }
                            ),
                        )
                    ),
                    color = colors.transparent
                ) {
                    TextAppBar(
                        stringResource(strings.messageTitle)
                    )
                }
            },
            onRefresh = {
                viewModel.updatePage()
            },
            error = error,
            noFound = null,
            isLoading = isLoading.value,
            toastItem = toastItem.value,
            modifier = modifier.fillMaxSize()
        ) {
            Column {
                if (model.message != null) {
                    Row(
                        modifier = Modifier.background(colors.white).fillMaxWidth()
                            .padding(dimens.mediumPadding)
                    ) {
                        Text(
                            stringResource(strings.selectDialogLabel),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.black
                        )
                    }
                }

                ListingBaseContent(
                    modifier = Modifier.fillMaxWidth(),
                    data = data,
                    viewModel = listingBaseViewModel,
                    noFound = noFound,
                    filtersContent = { activeWindowType ->
                        when (activeWindowType) {
                            ActiveWindowListingType.FILTERS -> {
                                DialogsFilterContent(
                                    listingData.filters,
                                ) {
                                    listingBaseViewModel.applyFilters(it)
                                }
                            }

                            ActiveWindowListingType.SORTING -> {
                                SortingOrdersContent(
                                    listingData.sort
                                ) {
                                    listingBaseViewModel.applySorting(it)
                                }
                            }

                            else -> {}
                        }
                    },
                    item = { conversation ->
                        ConversationItem(
                            conversation,
                            updateItem.value
                        )
                    }
                )
            }
        }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBigScreen.value) {
                    AnimatedVisibility(hideDrawer.value) {
                        ProfileDrawer(
                            stringResource(strings.messageTitle),
                            publicProfileNavigationItems
                        )
                    }
                }else {
                    ProfileDrawer(
                        stringResource(strings.messageTitle),
                        publicProfileNavigationItems
                    )
                }

                if (isBigScreen.value) {
                    content(Modifier.weight(1f))
                }
            }

        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        if(!isBigScreen.value) {
            content(Modifier.fillMaxWidth())
        }
    }
}
