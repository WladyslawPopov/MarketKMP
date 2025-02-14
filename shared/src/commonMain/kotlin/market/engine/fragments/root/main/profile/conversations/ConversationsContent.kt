package market.engine.fragments.root.main.profile.conversations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.fragments.base.BackHandler
import market.engine.fragments.root.main.profile.main.ProfileDrawer
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.DialogsFilterContent
import market.engine.widgets.filterContents.SortingOrdersContent
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationsContent(
    component: ConversationsComponent,
    modifier: Modifier,
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

    val refresh = {
        viewModel.resetScroll()
        viewModel.onRefresh()
        updateFilters.value++
    }

    val noFound = @Composable {
        if (listingData.value.filters.any { it.interpritation != null && it.interpritation != "" }) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                MsgFilters.clearFilters()
                listingData.value.filters = MsgFilters.filters
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
            withContext(Dispatchers.Default) {
                val res = viewModel.getConversation(viewModel.updateItem.value!!)
                withContext(Dispatchers.Main) {
                    if (res != null){
                        val item = data.itemSnapshotList.find { it?.id == viewModel.updateItem.value }
                        if (item != null) {
                            item.interlocutor = res.interlocutor
                            item.newMessage = res.newMessage
                            item.newMessageTs = res.newMessageTs
                            item.countUnreadMessages = res.countUnreadMessages
                            item.aboutObjectIcon = res.aboutObjectIcon
                        }
                        viewModel.updateItemTrigger.value++
                    }
                    viewModel.updateItem.value = null
                }
            }
        }
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawer(strings.messageTitle, model.navigationItems)
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        BaseContent(
            topBar = {
                ConversationsAppBar(
                    drawerState = drawerState,
                    modifier = modifier
                )
            },
            onRefresh = {
                refresh()
            },
            error = null,
            noFound = null,
            isLoading = isLoading.value,
            toastItem = viewModel.toastItem,
            modifier = modifier.fillMaxSize()
        ) {
            ListingBaseContent(
                modifier = Modifier.fillMaxWidth(),
                listingData = listingData.value,
                searchData = searchData.value,
                data = data,
                baseViewModel = viewModel,
                onRefresh = {
                    refresh()
                },
                noFound = noFound,
                additionalBar = {
                    DeletePanel(
                        selectedItems.size,
                        onCancel = {
                            viewModel.selectItems.clear()
                            isSelectedMode.value = false
                        },
                        onDelete = {
                            selectedItems.forEach { item ->
                                viewModel.deleteConversation(item){
                                    updateFilters.value--
                                }
                            }
                            if (updateFilters.value == 0){
                                viewModel.selectItems.clear()
                                viewModel.updateUserInfo()
                                viewModel.onRefresh()
                                isSelectedMode.value = false
                            }
                        }
                    )


                    FiltersBar(
                        searchData.value,
                        listingData.value,
                        updateFilters.value,
                        isShowGrid = false,
                        onFilterClick = {
                            viewModel.activeFiltersType.value = "filters"
                        },
                        onSortClick = {
                            viewModel.activeFiltersType.value = "sorting"
                        },
                        onRefresh = {
                            refresh()
                            updateFilters.value++
                        }
                    )
                },
                filtersContent = { isRefreshingFromFilters, onClose ->
                    when (viewModel.activeFiltersType.value) {
                        "filters" -> {
                            DialogsFilterContent(
                                isRefreshingFromFilters,
                                listingData.value.filters,
                                onClose
                            )
                        }

                        "sorting" -> SortingOrdersContent(
                            isRefreshingFromFilters,
                            listingData.value,
                            onClose
                        )
                    }
                },
                item = { conversation ->
                    val isSelect = rememberUpdatedState(selectedItems.contains(conversation.id))

                    if (conversation.interlocutor != null && viewModel.updateItemTrigger.value >= 0) {
                        ConversationItem(
                            conversation = conversation,
                            isVisibleCBMode = isSelectedMode.value,
                            isSelected = isSelect.value,
                            updateTrigger = viewModel.updateItemTrigger.value,
                            onSelectionChange = {
                                if (it) {
                                    viewModel.selectItems.add(conversation.id)
                                } else {
                                    viewModel.selectItems.remove(conversation.id)
                                }

                                isSelectedMode.value = selectedItems.isNotEmpty()
                            },
                            goToMessenger = {
                                if (isSelectedMode.value) {
                                    if (!isSelect.value) {
                                        viewModel.selectItems.add(conversation.id)
                                    } else {
                                        viewModel.selectItems.remove(conversation.id)
                                    }
                                    isSelectedMode.value = selectedItems.isNotEmpty()
                                } else {
                                    component.goToMessenger(conversation)
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}
