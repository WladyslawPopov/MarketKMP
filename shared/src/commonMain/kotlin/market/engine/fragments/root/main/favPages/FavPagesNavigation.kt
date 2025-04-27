package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.pointerInput
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.onError
import market.engine.fragments.root.main.favPages.favorites.DefaultFavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesContent
import market.engine.fragments.root.main.favPages.subscriptions.DefaultSubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent
import market.engine.widgets.dialogs.CustomDialog


@Serializable
data class FavPagesConfig(
    @Serializable
    val favItem: FavoriteListItem
)

@Composable
fun FavPagesNavigation(
    component: FavPagesComponent,
    modifier: Modifier
) {
    val select = remember {
        mutableStateOf(0)
    }

    val model = component.model.subscribeAsState()
    val viewModel = model.value.viewModel
    val favTabList = viewModel.favoritesTabList.collectAsState()

    val isDragMode = remember { viewModel.isDragMode }
    val showCreatedDialog = remember { mutableStateOf("") }
    val postId = remember { mutableStateOf(0L) }
    val isClicked = remember { mutableStateOf(false) }
    val fields = remember { mutableStateOf<List<Fields>>(emptyList()) }
    val title = remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.initPosition.value
    )

    val err = viewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err) { component.onRefresh() } }
    }else{
        null
    }

    LaunchedEffect(lazyListState){
        snapshotFlow {
            lazyListState.firstVisibleItemIndex
        }.collect { index ->
            viewModel.initPosition.value = index
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        toastItem = viewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        Column {
            FavPagesAppBar(
                select.value,
                favTabList = favTabList.value,
                isDragMode = isDragMode.value,
                modifier = Modifier.fillMaxWidth(),
                navigationClick = {
                    select.value = it
                    component.selectPage(select.value)
                },
                onTabsReordered = {
                    viewModel.updateFavTabList(it)
                },
                getOperations = { id, callback ->
                    viewModel.getOperationFavTab(id, callback)
                },
                makeOperation = { type, id ->
                    when (type) {
                        "create_blank_offer_list", "copy_offers_list", "rename_offers_list" -> {
                            viewModel.getOperationFields(type, id) { t, f ->
                                title.value = t
                                fields.value = f
                                showCreatedDialog.value = type
                                postId.value = id
                            }
                        }

                        "reorder" -> {
                            viewModel.isDragMode.value = true
                        }

                        "delete_offers_list" -> {
                            viewModel.deleteFavTab(id) {
                                component.fullRefresh()
                            }
                        }

                        "mark_as_primary_offers_list" -> {
                            viewModel.pinFavTab(id) {
                                component.fullRefresh()
                            }
                        }

                        "unmark_as_primary_offers_list" -> {
                            viewModel.unpinFavTab(id) {
                                component.fullRefresh()
                            }
                        }

                        else -> {

                        }
                    }
                },
                settings = viewModel.settings,
                lazyListState = lazyListState,
                onRefresh = {
                    component.onRefresh()
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                ChildPages(
                    modifier = Modifier.fillMaxSize(),
                    pages = component.componentsPages,
                    scrollAnimation = PagesScrollAnimation.Default,
                    onPageSelected = {
                        select.value = it
                        component.selectPage(select.value)
                    }
                ) { _, page ->
                    when (page) {
                        is FavPagesComponents.SubscribedChild -> {
                            SubscriptionsContent(
                                page.component,
                                Modifier
                            )
                        }

                        is FavPagesComponents.FavoritesChild -> {
                            FavoritesContent(
                                page.component,
                                Modifier
                            )
                        }
                    }
                }

                if (isDragMode.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.black.copy(alpha = 0.3f))
                            .blur(dimens.extraLargePadding)
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    viewModel.isDragMode.value = false
                                    component.fullRefresh()
                                }
                            }
                    )
                }
            }

            CustomDialog(
                showDialog = showCreatedDialog.value != "",
                containerColor = colors.primaryColor,
                title = title.value,
                body = {
                    SetUpDynamicFields(fields.value)
                },
                onDismiss = {
                    showCreatedDialog.value = ""
                    isClicked.value = false
                },
                onSuccessful = {
                    if (!isClicked.value) {
                        isClicked.value = true
                        val bodyPost = HashMap<String, JsonElement>()
                        fields.value.forEach { field ->
                            if (field.data != null) {
                                bodyPost[field.key ?: ""] = field.data!!
                            }
                        }

                        viewModel.postFieldsSend(
                            showCreatedDialog.value,
                            postId.value,
                            bodyPost,
                            onSuccess = {
                                showCreatedDialog.value = ""
                                isClicked.value = false
                                component.fullRefresh()
                            },
                            errorCallback = { f ->
                                if (f != null) {
                                    fields.value = f
                                } else {
                                    showCreatedDialog.value = ""
                                }
                                isClicked.value = false
                            }
                        )
                    }
                }
            )
        }
    }
}

fun itemFavorites(
    componentContext: ComponentContext,
    selectedType : FavScreenType,
    idList : Long?,
    navigateToOffer: (id: Long) -> Unit,
    updateTabs : () -> Unit
): FavoritesComponent {
    return DefaultFavoritesComponent(
        componentContext = componentContext,
        goToOffer = { id ->
            navigateToOffer(id)
        },
        favType = selectedType,
        idList = idList,
        updateTabs = updateTabs
    )
}


fun itemSubscriptions(
    componentContext: ComponentContext,
    selectedType : FavScreenType,
    navigateToCreateNewSubscription : (Long?) -> Unit,
    navigateToListing : (ListingData) -> Unit
): SubscriptionsComponent {
    return DefaultSubscriptionsComponent(
        componentContext = componentContext,
        favType = selectedType,
        navigateToCreateNewSubscription = {
            navigateToCreateNewSubscription(it)
        },
        navigateToListing = {
            navigateToListing(it)
        }
    )
}

