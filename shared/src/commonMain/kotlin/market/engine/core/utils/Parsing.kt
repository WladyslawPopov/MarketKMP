package market.engine.core.utils


import androidx.compose.runtime.MutableState
import androidx.compose.ui.util.fastForEach
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.removeNotification
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.NotificationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.Order
import market.engine.core.network.networkObjects.User
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.NotificationsHistory
import org.jetbrains.compose.resources.DrawableResource
import org.koin.mp.KoinPlatform.getKoin

fun<T> deserializePayload(
    jsonElement: JsonElement?,
    serializer: KSerializer<T>
): T {
    if (jsonElement != null) {
        try {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
            return json.decodeFromJsonElement(serializer, jsonElement)
        } catch (e: Exception) {
            throw ServerErrorException(e.message.toString(), "")
        }
    } else {
        throw ServerErrorException("empty_payload", "empty payload!")
    }
}

fun Offer.getOfferImagePreview(): String {
    return when {
        images?.isNotEmpty() == true -> images?.firstOrNull()?.urls?.small?.content ?: ""
        externalImages?.isNotEmpty() == true -> externalImages.firstOrNull() ?: ""
        externalUrl != null -> externalUrl
        image?.small?.content != null -> image.small.content
        else -> ""
    }
}

fun String.cleanSearchString(): String {
    return this.replace(Regex("[^a-zA-Zа-яА-Я0-9]"), "_")
}

fun provideByType(notificationItem: NotificationsHistory, onProvide : (DeepLink) -> Unit) {
    notificationItem.run {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
        val je = json.parseToJsonElement(data_)

        when (type) {
            "message about offer" -> {
                val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
                if (dialogId != null) {
                    val deepLink = DeepLink.GoToDialog(dialogId, null)
                    onProvide(deepLink)
                }
            }

            "message about order" -> {
                val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
                if (dialogId != null) {
                    val deepLink = DeepLink.GoToDialog(dialogId, null)
                    onProvide(deepLink)
                }
            }
        }
    }
}

fun NotificationItem.getIconByType() : DrawableResource {
    return when(type) {
        "message about offer" -> {
             drawables.mail
        }
        "message about order" -> {
            drawables.mail
        }
        else -> {
            drawables.notification
        }
    }
}

fun NotificationItem.getDeepLinkByType() : DeepLink? {
    val db : MarketDB = getKoin().get()

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    val je = json.parseToJsonElement(data)

    return when(type) {
        "message about offer" -> {
            unreadIds.forEach {
                val androidID = db.notificationsHistoryQueries.selectNotificationById(it).executeAsOneOrNull()?.isRead
                if (androidID != null) {
                    removeNotification(androidID.toString())
                }else{
                    removeNotification(it)
                }
                db.notificationsHistoryQueries.deleteNotificationById(it)
            }
            val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
            if (dialogId != null) {
                DeepLink.GoToDialog(dialogId, null)
            }else{
                null
            }
        }
        "message about order" -> {
            unreadIds.forEach {
                val androidID = db.notificationsHistoryQueries.selectNotificationById(it).executeAsOneOrNull()?.isRead
                if (androidID != null) {
                    removeNotification(androidID.toString())
                }else{
                    removeNotification(it)
                }
                db.notificationsHistoryQueries.deleteNotificationById(it)
            }

            val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
            if (dialogId != null) {
                DeepLink.GoToDialog(dialogId, null)
            }else{
                null
            }
        }
        else -> {
            null
        }
    }
}

fun deleteReadNotifications() {
    try {
        val db : MarketDB = getKoin().get()
        db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList().
        filter { it.isRead == 1L }.fastForEach {
            when(it.type){
                "message about offer" ->{
                    db.notificationsHistoryQueries.deleteNotificationById(it.id)
                }
                "message about order" ->{
                    db.notificationsHistoryQueries.deleteNotificationById(it.id)
                }
            }
        }
    } catch (_ : Exception) {}
}


fun Operations.onClickOrderOperationItem(
    item : Order,
    title : MutableState<String>,
    baseViewModel : BaseViewModel,
    showOperationsDialog : MutableState<String>,
    onUpdateOfferItem : () -> Unit,
) {
    when (id) {
        "give_feedback_to_seller" -> {
            title.value = name.toString()
            showOperationsDialog.value = "give_feedback_to_seller"
        }
        "give_feedback_to_buyer" -> {
            title.value = name.toString()
            showOperationsDialog.value = "give_feedback_to_buyer"
        }
        "set_comment" -> {
            title.value = name.toString()
            showOperationsDialog.value = "set_comment"
        }
        "provide_track_id" -> {
            title.value = name.toString()
            showOperationsDialog.value = "provide_track_id"
        }
        else -> {
            title.value = name.toString()
            baseViewModel.postOperationFields(
                item.id,
                id ?: "",
                "orders",
                onSuccess = {
                    onUpdateOfferItem()
                },
                errorCallback = {

                }
            )
        }
    }
}


fun OfferItem.setNewParams(offer: Offer) {
    images = buildList {
        when {
            offer.images?.isNotEmpty() == true -> addAll(offer.images?.map { it.urls?.small?.content ?: "" }?.toList() ?: emptyList())
            offer.externalImages?.isNotEmpty() == true -> addAll(offer.externalImages)
            offer.externalUrl != null -> add(offer.externalUrl)
            offer.image?.small?.content != null -> add(offer.image.small.content)
        }
    }
    price = offer.currentPricePerItem ?: ""
    title = offer.title ?: ""
    note = offer.note
    relistingMode = offer.relistingMode
    isWatchedByMe = offer.isWatchedByMe
    viewsCount = offer.viewsCount
    promoOptions = offer.promoOptions
    bids = offer.bids
    state = offer.state
    session = offer.session
    watchersCount = offer.watchersCount
    myMaximalBid = offer.myMaximalBid
}

fun Offer.parseToOfferItem() : OfferItem {

    var isPromo = false
    if (promoOptions != null && sellerData?.id != UserData.login) {
        val isBackLight = promoOptions.find { it.id == "backlignt_in_listing" }
        if (isBackLight != null) {
            isPromo = true
        }
    }

    return OfferItem(
        id = id,
        title = title ?: "",
        images = buildList {
             when {
                images?.isNotEmpty() == true -> addAll(images?.map { it.urls?.small?.content ?: "empty" }?.toList() ?: emptyList())
                externalImages?.isNotEmpty() == true -> addAll(externalImages)
                externalUrl != null -> add(externalUrl)
                image?.small?.content != null -> add(image.small.content)
                else -> listOf("empty")
            }
        },
        note = note,
        isWatchedByMe = isWatchedByMe,
        videoUrls = videoUrls,
        isPrototype = isPrototype,
        quantity = quantity,
        price = currentPricePerItem ?: "",
        type = saleType ?: "",
        seller = sellerData ?: User(),
        buyer = buyerData,
        numParticipants = numParticipants,
        bids = bids,
        location = buildString {
            freeLocation?.let { append(it) }
            region?.name?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
        },
        discount = discountPercentage,
        safeDeal = safeDeal,
        promoOptions = promoOptions,
        myMaximalBid = myMaximalBid,
        isPromo = isPromo,
        createdTs = createdTs,
        catPath = catpath,
        publicUrl = publicUrl,
        watchersCount = watchersCount,
        viewsCount = viewsCount,
        relistingMode = relistingMode,
        session = session,
        state = state,
        currentQuantity = currentQuantity
    )
}
