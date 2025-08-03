package market.engine.core.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import market.engine.core.network.networkObjects.User
import market.engine.core.network.networkObjects.Value
import market.engine.fragments.base.CoreViewModel
import market.engine.shared.AuctionMarketDb
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

fun formatRemainingTimeAnnotated(
    millisUntilFinished: Long,
    beforeGraduationLabel: String,
    daysLabel: String,
    hoursLabel: String,
    minutesLabel: String,
    secondsLabel: String
): AnnotatedString {
    val days = millisUntilFinished / (1000 * 60 * 60 * 24)
    val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
    val minutes = (millisUntilFinished / (1000 * 60)) % 60
    val seconds = (millisUntilFinished / 1000) % 60

    return buildAnnotatedString {
        if (days > 0) {
            append("$days ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(daysLabel)
            }
            append(" $beforeGraduationLabel")
        } else {
            if (hours > 0) {
                append("$hours ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(hoursLabel)
                }
                append(" ")
            }
            if (minutes > 0) {
                append("$minutes ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(minutesLabel)
                }
                append(" ")
            }

            if (seconds > 0) {
                append("$seconds ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(secondsLabel)
                }
            }

            append(" $beforeGraduationLabel")
        }
    }
}

fun formatParameterValue(value: Value?): String {
    return buildString {
        value?.valueFree?.let { append(it) }

        value?.valueChoices?.forEach {
            if (isNotEmpty()) append("; ")
            append(it.name)
        }

        if (isNotEmpty() && last() == ' ') {
            dropLast(2)
        }
    }
}

fun provideByType(notificationItem: NotificationsHistory, onProvide : (DeepLink) -> Unit) {
    try {
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
    } catch (e: Exception) {
        println("Error in provideByType: ${e.message}")
        e.printStackTrace()
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
    try {
        val db: AuctionMarketDb = getKoin().get()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
        val je = json.parseToJsonElement(data)

        return when (type) {
            "message about offer" -> {
                unreadIds.forEach {
                    val androidID = db.notificationsHistoryQueries.selectNotificationById(it)
                        .executeAsOneOrNull()?.isRead
                    if (androidID != null) {
                        removeNotification(androidID.toString())
                    } else {
                        removeNotification(it)
                    }
                    db.notificationsHistoryQueries.deleteNotificationById(it)
                }
                val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
                if (dialogId != null) {
                    DeepLink.GoToDialog(dialogId, null)
                } else {
                    null
                }
            }

            "message about order" -> {
                unreadIds.forEach {
                    val androidID = db.notificationsHistoryQueries.selectNotificationById(it)
                        .executeAsOneOrNull()?.isRead
                    if (androidID != null) {
                        removeNotification(androidID.toString())
                    } else {
                        removeNotification(it)
                    }
                    db.notificationsHistoryQueries.deleteNotificationById(it)
                }

                val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
                if (dialogId != null) {
                    DeepLink.GoToDialog(dialogId, null)
                } else {
                    null
                }
            }

            else -> {
                null
            }
        }
    } catch (e: Exception) {
        // Log this exception
        println("Error in getDeepLinkByType: ${e.message}")
        e.printStackTrace()
        return null
    }
}

fun deleteReadNotifications() {
    try {
        val db : AuctionMarketDb = getKoin().get()
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
    } catch (e: Exception) {
        // Log.error("Failed to delete read notifications", e) // Using a hypothetical logger
        println("Error in deleteReadNotifications: ${e.message}")
        e.printStackTrace()
    }
}

fun OfferItem.setNewParams(offer: Offer) : OfferItem {
    var isPromo = false
    if (offer.promoOptions != null && offer.sellerData?.id != UserData.login) {
        val isBackLight = offer.promoOptions.find { it.id == "backlignt_in_listing" }
        if (isBackLight != null) {
            isPromo = true
        }
    }

    return this.copy(
        id = offer.id,
        title = offer.title ?: "",
        images = buildList {
            when {
                offer.images?.isNotEmpty() == true -> addAll(offer.images?.map { it.urls?.small?.content ?: "empty" } ?: emptyList())
                offer.externalImages?.isNotEmpty() == true -> addAll(offer.externalImages)
                offer.externalUrl != null -> add(offer.externalUrl)
                offer.image?.small?.content != null -> add(offer.image.small.content)
                else -> listOf("empty")
            }
        },
        note = offer.note,
        isWatchedByMe = offer.isWatchedByMe,
        videoUrls = offer.videoUrls ?: emptyList(),
        isPrototype = offer.isPrototype,
        quantity = offer.originalQuantity,
        currentQuantity = offer.currentQuantity,
        price = offer.currentPricePerItem ?: offer.buyNowPrice  ?: offer.pricePerItem ?: "",
        seller = offer.sellerData ?: User(),
        buyer = offer.buyerData,
        numParticipants = offer.numParticipants,
        watchersCount = offer.watchersCount,
        viewsCount = offer.viewsCount,
        publicUrl = offer.publicUrl,
        relistingMode = offer.relistingMode,
        bids = offer.bids,
        location = buildString {
            offer.freeLocation?.let { append(it) }
            offer.region?.name?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
        },
        safeDeal = offer.safeDeal,
        promoOptions = offer.promoOptions ?: emptyList(),
        myMaximalBid = offer.myMaximalBid,
        catPath = offer.catpath,
        discount = offer.discountPercentage,
        type = offer.saleType ?: "",
        isPromo = isPromo,
        createdTs = offer.createdTs,
        state = offer.state,
        session = offer.session,
        externalImages = offer.externalImages,
        version = offer.version,
        standardDescriptions = offer.standardDescriptions,
        addedDescriptions = offer.addedDescriptions,
        description = offer.description,
        params = offer.params,
        region = offer.region,
        hasTempImages = offer.hasTempImages,
        minimalAcceptablePrice = offer.minimalAcceptablePrice ?: "0",
        deliveryMethods = offer.deliveryMethods,
        removedBids = offer.removedBids,
        whoPaysForDelivery = offer.whoPaysForDelivery,
        antisniper = offer.antisniper
    )
}

fun CoreViewModel.getMainTread(content : suspend () -> Unit): Job {
    return viewModelScope.launch {
        withContext(Dispatchers.Main) {
            content()
        }
    }
}

fun CoreViewModel.getIoTread(content : suspend () -> Unit): Job {
    return viewModelScope.launch {
        withContext(Dispatchers.IO) {
            content()
        }
    }
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
                images?.isNotEmpty() == true -> addAll(images?.map { it.urls?.small?.content ?: "empty" } ?: emptyList())
                externalImages?.isNotEmpty() == true -> addAll(externalImages)
                externalUrl != null -> add(externalUrl)
                image?.small?.content != null -> add(image.small.content)
                else -> listOf("empty")
            }
        },
        note = note,
        isWatchedByMe = isWatchedByMe,
        videoUrls = videoUrls ?: emptyList(),
        isPrototype = isPrototype,
        quantity = originalQuantity,
        currentQuantity = currentQuantity,
        price = currentPricePerItem ?: pricePerItem ?: buyNowPrice ?: "",
        type = saleType ?: "",
        seller = sellerData ?: User(),
        buyer = buyerData,
        numParticipants = numParticipants,
        watchersCount = watchersCount,
        viewsCount = viewsCount,
        publicUrl = publicUrl,
        relistingMode = relistingMode,
        bids = bids,
        location = buildString {
            freeLocation?.let { append(it) }
            region?.name?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
        },
        safeDeal = safeDeal,
        promoOptions = promoOptions ?: emptyList(),
        myMaximalBid = myMaximalBid,
        catPath = catpath,
        discount = discountPercentage,
        isPromo = isPromo,
        createdTs = createdTs,
        state = state,
        session = session,
        externalImages = externalImages,
        version = version,
        standardDescriptions = standardDescriptions,
        addedDescriptions = addedDescriptions,
        description = description,
        params = params,
        region = region,
        hasTempImages = hasTempImages,
        minimalAcceptablePrice = minimalAcceptablePrice ?: "0",
        deliveryMethods = deliveryMethods,
        removedBids = removedBids,
        whoPaysForDelivery = whoPaysForDelivery,
        antisniper = antisniper
    )
}
