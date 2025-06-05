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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.widgets.dialogs.AccessDialog
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource


@Composable
fun BasketContent(
    component: BasketComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.basketViewModel
    val userBasket = modelState.value.basketViewModel.responseGetUserCart.collectAsState()
    val isLoading = modelState.value.basketViewModel.isShowProgress.collectAsState()
    val isError = modelState.value.basketViewModel.errorMessage.collectAsState()

    val listOffers = remember { mutableStateOf(emptyList<Long>()) }

    val successToast = stringResource(strings.operationSuccess)

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItem.value
    )
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

    LaunchedEffect(UserData.userInfo){
        val countOffers = UserData.userInfo?.countOffersInCart
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

    LaunchedEffect(state){
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            viewModel.firstVisibleItem.value = it
        }
    }

    val noFound: (@Composable () ->Unit)? = if (userBasket.value.isEmpty()){
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
                isError
            ){
                viewModel.onError(ServerErrorException())
                viewModel.getUserCart()
            }
        }
    }else{
        null
    }

    val refresh = {
        viewModel.onError(ServerErrorException())
        viewModel.getUserCart()
    }

    BaseContent(
        topBar = {
            BasketAppBar(
                stringResource(strings.yourBasketTitle),
                subtitle.value,
                clearBasket = {
                    viewModel.clearBasket{
                        refresh()
                    }
                },
                onRefresh = {
                    refresh()
                }
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
            items(userBasket.value, key = { item -> item.first?.id ?: item.hashCode() }) { item ->
                BasketItemContent(
                    item,
                    goToUser = remember {
                        { userId ->
                            component.goToUser(userId)
                        }
                    },
                    goToOffer =
                        remember {
                            { offerId ->
                                component.goToOffer(offerId)
                            }
                        },
                    changeQuantity = remember {
                        { offerId, quantity ->
                            val bodyAddB = HashMap<String, JsonElement>()
                            bodyAddB["offer_id"] = JsonPrimitive(offerId)
                            bodyAddB["quantity"] = JsonPrimitive(quantity)

                            viewModel.addOfferToBasket(bodyAddB) {
                                userBasket.value.find { pair ->
                                    pair.second.find { it?.id == offerId } != null
                                }?.second?.find { it?.id == offerId }
                                    ?.quantity = quantity
                            }
                        }
                    },
                    deleteOffer =  remember {
                        { offerId ->
                            listOffers.value = buildList {
                                add(offerId)
                            }
                        }
                    },
                    clearUserOffers =  remember {
                        { offers ->
                            listOffers.value = buildList {
                                addAll(offers)
                            }
                        }
                    },
                    goToCreateOrder = remember {
                        {
                            component.goToCreateOrder(it)
                        }
                    },
                    addOfferToFavorites = remember {
                        { offer, onFinish ->
                            viewModel.addToFavorites(offer.parseToOfferItem()) {
                                offer.isWatchedByMe = it
                                onFinish(it)
                            }
                        }
                    }
                )
            }
        }

        AccessDialog(
            listOffers.value.isNotEmpty(),
            buildAnnotatedString {
                if (listOffers.value.size == 1) {
                    append(stringResource(strings.warningDeleteOfferBasket))
                } else {
                    append(stringResource(strings.warningDeleteSelectedOfferFromBasket))
                }
            },
            onDismiss = {
                listOffers.value = emptyList()
            },
            onSuccess = {
                val offerIds = arrayListOf<JsonPrimitive>()

                listOffers.value.forEach {
                    offerIds.add(JsonPrimitive(it))
                }

                val jsonArray = JsonArray(offerIds)

                val body = hashMapOf<String, JsonElement>()
                body["offer_ids"] = jsonArray

                viewModel.deleteItem(
                    body,
                    userBasket.value.find { pair ->
                        pair.second.find { it?.id == listOffers.value.firstOrNull() } != null
                    }?.second?.find { it?.id == listOffers.value.firstOrNull() },
                    userBasket.value.find { pair ->
                        pair.second.find { it?.id == listOffers.value.firstOrNull() } != null
                    }?.first?.id ?: 1L
                ) {
                    refresh()
                    listOffers.value = emptyList()
                    viewModel.showToast(
                        successToastItem.copy(message = successToast)
                    )
                }
            }
        )
    }
}
