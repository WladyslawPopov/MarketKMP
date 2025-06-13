package market.engine.fragments.root.main.profile.conversations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.root.main.profile.ProfileDrawer
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.DialogsFilterContent
import market.engine.widgets.filterContents.SortingOrdersContent
import market.engine.widgets.items.ConversationItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationsContent(
    component: ConversationsComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.viewModel
    val listingData = viewModel.listingData.value.data
    val searchData = viewModel.listingData.value.searchData
    val data = model.pagingDataFlow.collectAsLazyPagingItems()

    val isSelectedMode = remember { mutableStateOf(false) }
    val selectedItems = remember { viewModel.selectItems }

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val updateFilters = remember { mutableStateOf(0) }

    val hideDrawer = remember { mutableStateOf(isBigScreen.value) }

    val refresh = remember {{
        viewModel.resetScroll()
        viewModel.onRefresh()
        data.refresh()
        updateFilters.value++
    }}

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err.value) { refresh() } }
    }else{
        null
    }

    val noFound = @Composable {
        if (listingData.filters.any { it.interpretation != null && it.interpretation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                MsgFilters.clearFilters()
                listingData.filters = MsgFilters.filters
                refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.simpleNotFoundLabel),
                icon = drawables.dialogIcon
            ) {
                refresh()
            }
        }
    }

    //update item when we back
    LaunchedEffect(viewModel.updateItem.value) {
        if (viewModel.updateItem.value != null) {
            val oldItem = data.itemSnapshotList.find { it?.id == viewModel.updateItem.value }
            component.updateItem(oldItem)
        }
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    val drawerState = rememberDrawerState(initialValue = if(isBigScreen.value) DrawerValue.Open else DrawerValue.Closed)

    val content : @Composable (Modifier) -> Unit = {
        BaseContent(
            topBar = {
                ConversationsAppBar(
                    showMenu = hideDrawer.value,
                    openMenu = if (isBigScreen.value) {
                        {
                            hideDrawer.value = !hideDrawer.value
                        }
                    }else{
                        null
                    },
                    drawerState = drawerState,
                    modifier = modifier,
                    onRefresh = {
                        refresh()
                    }
                )
            },
            onRefresh = {
                refresh()
            },
            error = error,
            noFound = null,
            isLoading = isLoading.value,
            toastItem = viewModel.toastItem,
            modifier = modifier.fillMaxSize()
        ) {
//            ListingBaseContent(
//                modifier = Modifier.fillMaxWidth(),
//                listingData = listingData.value,
//                searchData = searchData,
//                data = data,
//                baseViewModel = viewModel,
//                onRefresh = {
//                    refresh()
//                },
//                noFound = noFound,
//                additionalBar = {
//                    if(model.message != null){
//                        Row(
//                            modifier = Modifier.background(colors.white).fillMaxWidth()
//                                .padding(dimens.mediumPadding)
//                        ) {
//                            Text(
//                                stringResource(strings.selectDialogLabel),
//                                style = MaterialTheme.typography.titleMedium,
//                                color = colors.black
//                            )
//                        }
//                    }
//
//                    DeletePanel(
//                        selectedItems.size,
//                        onCancel = {
//                            viewModel.selectItems.clear()
//                            isSelectedMode.value = false
//                        },
//                        onDelete = {
//                            selectedItems.forEach { item ->
//                                viewModel.deleteConversation(item){
//                                    updateFilters.value--
//                                }
//                            }
//                            if (updateFilters.value == 0){
//                                viewModel.selectItems.clear()
//                                viewModel.updateUserInfo()
//                                refresh()
//                                isSelectedMode.value = false
//                            }
//                        }
//                    )
//
//
////                    FiltersBar(
////                        searchData,
////                        listingData.value,
////                        updateFilters.value,
////                        isShowGrid = false,
////                        onFilterClick = {
////                            viewModel.activeFiltersType.value = "filters"
////                        },
////                        onSortClick = {
////                            viewModel.activeFiltersType.value = "sorting"
////                        },
////                        onRefresh = {
////                            refresh()
////                            updateFilters.value++
////                        }
////                    )
//                },
//                filtersContent = { isRefreshingFromFilters , onClose ->
//                    when (viewModel.activeFiltersType.value) {
//                        "filters" -> {
//                            DialogsFilterContent(
//                                isRefreshingFromFilters,
//                                listingData.value.filters,
//                                onClose
//                            )
//                        }
//
//                        "sorting" -> SortingOrdersContent(
//                            isRefreshingFromFilters,
//                            listingData.value,
//                            onClose
//                        )
//                    }
//                },
//                item = { conversation ->
//                    val isSelect = rememberUpdatedState(selectedItems.contains(conversation.id))
//
//                    ConversationItem(
//                        conversation = conversation,
//                        isVisibleCBMode = isSelectedMode.value,
//                        isSelected = isSelect.value,
//                        updateTrigger = viewModel.updateItemTrigger.value,
//                        onSelectionChange = {
//                            if (it) {
//                                viewModel.selectItems.add(conversation.id)
//                            } else {
//                                viewModel.selectItems.remove(conversation.id)
//                            }
//
//                            isSelectedMode.value = selectedItems.isNotEmpty()
//                        },
//                        goToMessenger = {
//                            if (isSelectedMode.value) {
//                                if (!isSelect.value) {
//                                    viewModel.selectItems.add(conversation.id)
//                                } else {
//                                    viewModel.selectItems.remove(conversation.id)
//                                }
//                                isSelectedMode.value = selectedItems.isNotEmpty()
//                            } else {
//                                component.goToMessenger(conversation)
//                            }
//                        }
//                    )
//                }
//            )
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
