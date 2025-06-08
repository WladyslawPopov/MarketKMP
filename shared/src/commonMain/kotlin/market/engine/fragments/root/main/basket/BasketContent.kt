package market.engine.fragments.root.main.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.dialogs.AccessDialog
import market.engine.widgets.items.BasketItemContent
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource


@Composable
fun BasketContent(
    component: BasketComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.basketViewModel
    val isLoading = modelState.value.basketViewModel.isShowProgress.collectAsState()
    val isError = modelState.value.basketViewModel.errorMessage.collectAsState()
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItem.value
    )

    val basketItemsState = viewModel.uiState.collectAsState()

    val deleteIds = remember { mutableStateOf(emptyList<Long>()) }
    val showMenu = remember { mutableStateOf(false) }

    val subtitle : MutableState<String?> = remember {
        mutableStateOf(null)
    }

    BackHandler(
        modelState.value.backHandler
    ){

    }

    val oneOffer = stringResource(strings.oneOfferLabel)
    val manyOffers = stringResource(strings.manyOffersLabel)
    val exManyOffers = stringResource(strings.exManyOffersLabel)
    val menuString = stringResource(strings.menuTitle)
    val clearBasketString = stringResource(strings.actionClearBasket)

    LaunchedEffect(Unit){
        snapshotFlow {
            UserData.userInfo
        }.collectLatest { info ->
            val countOffers = info?.countOffersInCart

            subtitle.value = buildString {
                if (countOffers.toString()
                        .matches(Regex("""([^1]1)$""")) || countOffers == 1
                ) {
                    append("$countOffers $oneOffer")
                } else if (countOffers.toString()
                        .matches(Regex("""([^1][234])$""")) || countOffers == 2 || countOffers == 3 || countOffers == 4
                ) {
                    append("$countOffers $exManyOffers")
                } else {
                    append("$countOffers $manyOffers")
                }
            }
        }
    }

    LaunchedEffect(state){
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            viewModel.firstVisibleItem.value = it
        }
    }

    val noFound: (@Composable () ->Unit)? = if (basketItemsState.value.isEmpty()){
        {
            showNoItemLayout(
                image = drawables.cartEmptyIcon,
                title = stringResource(strings.cardIsEmptyLabel),
                textButton = stringResource(strings.startShoppingLabel),
            ) {
                //go to listing
                component.goToListing()
            }
        }
    }else{
        null
    }

    val error : (@Composable () ->Unit)? = if (isError.value.humanMessage.isNotBlank()){
        {
            onError(
                isError.value
            ){
                viewModel.onError(ServerErrorException())
                viewModel.getUserCart()
            }
        }
    }else{
        null
    }

    val refresh = remember {{
        viewModel.onError(ServerErrorException())
        viewModel.getUserCart()
    }}

    val listItems = remember {
        listOf(
            NavigationItem(
                title = "",
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                onClick = {
                    refresh()
                }
            ),
            NavigationItem(
                title = menuString,
                icon = drawables.menuIcon,
                tint = colors.black,
                hasNews = false,
                badgeCount = null,
                onClick = {
                    showMenu.value = true
                }
            ),
        )
    }

    val menuItems = remember {
        listOf(
            MenuItem(
                id = "delete_basket",
                title = clearBasketString,
                icon = drawables.deleteIcon,
                onClick = {
                    viewModel.clearBasket{
                        refresh()
                    }
                }
            )
        )
    }

    BaseContent(
        topBar = {
            BasketAppBar(
                stringResource(strings.yourBasketTitle),
                subtitle.value,
                menuItems = menuItems,
                listItems = listItems,
                showMenu = showMenu,
                modifier = Modifier
            )
        },
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = noFound,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumnWithScrollBars(
            modifierList = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            state = state,
            contentPadding = dimens.smallPadding
        ) {
            items(basketItemsState.value, key = { item -> item.user.id }) { itemState ->
                BasketItemContent(
                    state = itemState,
                    events = viewModel.getEvents(
                        goToOffer = {
                            component.goToOffer(it)
                        },
                        goToUser = {
                            component.goToUser(it)
                        },
                        goToCreateOrder = {
                            component.goToCreateOrder(it)
                        },
                        onDeleteOffers = { ids ->
                            deleteIds.value = ids
                        }
                    )
                )
            }
        }

        AccessDialog(
            deleteIds.value.isNotEmpty(),
            buildAnnotatedString {
                if (deleteIds.value.size == 1) {
                    append(stringResource(strings.warningDeleteOfferBasket))
                } else {
                    append(stringResource(strings.warningDeleteSelectedOfferFromBasket))
                }
            },
            onDismiss = {
                deleteIds.value = emptyList()
            },
            onSuccess = {
                viewModel.deleteItems(
                    deleteIds.value
                ) {
                    deleteIds.value = emptyList()
                    refresh()
                }
            }
        )
    }
}
