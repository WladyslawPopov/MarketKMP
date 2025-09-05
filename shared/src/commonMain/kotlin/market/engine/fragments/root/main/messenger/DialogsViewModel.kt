package market.engine.fragments.root.main.messenger

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.SavedStateHandle

import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import app.cash.paging.PagingData
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import market.engine.common.clipBoardEvent
import market.engine.common.compressImage
import market.engine.common.getImageUriFromPlatformFile
import market.engine.common.openUrl
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.DialogItemEvents
import market.engine.core.data.events.MessengerBarEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DeepLink
import market.engine.core.data.items.DialogsData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.MenuData
import market.engine.core.data.items.MesHeaderItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.PhotoSave
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.states.MessengerBarState
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.states.DialogContentState
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.data.types.MessageType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.functions.PrivateMessagesOperation
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.Dialog
import market.engine.core.network.networkObjects.MesImage
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.Base64.encodeToBase64
import market.engine.core.utils.convertDateYear
import market.engine.core.utils.getOfferImagePreview
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.parseDeepLink
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.String
import kotlin.collections.map
import kotlin.getValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DialogsViewModel(
    val dialogId: Long,
    val message: String?,
    val component: DialogsComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {
    private val privateMessagesOperation: PrivateMessagesOperation = getKoin().get()
    private val pagingRepository: PagingRepository<Dialog> = PagingRepository()

    private val _responseGetConversation = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetConversation",
        Conversations(),
        Conversations.serializer()
    )

    private val _responseGetOfferInfo = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetOfferInfo",
        Offer(),
        Offer.serializer()
    )

    private val _responseGetOrderInfo = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetOrderInfo",
        Order(),
        Order.serializer()
    )

    private val _responseImages = savedStateHandle.getSavedStateFlow(
        scope,
        "responseImages",
        emptyList(),
        ListSerializer(PhotoSave.serializer())
    )

    private val _messageTextState = savedStateHandle.getSavedStateFlow(
        scope,
        "messageTextState",
        "",
        String.serializer()
    )

    private val _isDisabledSendMes = savedStateHandle.getSavedStateFlow(
        scope,
        "isDisabledSendMes",
        true,
        Boolean.serializer()
    )
    private val _isDisabledAddPhotos = savedStateHandle.getSavedStateFlow(
        scope,
        "isDisabledAddPhotos",
        true,
        Boolean.serializer()
    )

    private val _selectedImageIndex = savedStateHandle.getSavedStateFlow(
        scope,
        "selectedImageIndex",
        0,
        Int.serializer()
    )
    val selectedImageIndex = _selectedImageIndex.state

    private val _images = savedStateHandle.getSavedStateFlow(
        scope,
        "images",
        emptyList(),
        ListSerializer(String.serializer())
    )
    val images = _images.state

    private val _isMenuVisible = savedStateHandle.getSavedStateFlow(
        scope,
        "isMenuVisible",
        false,
        Boolean.serializer()
    )

    private val conversationsOperations : ConversationsOperations by lazy { getKoin().get() }
    private val orderOperations : OrderOperations by lazy { getKoin().get() }

    val messageBarEvents = MessageBarEventsImpl(this)

    val listingBaseViewModel = component.additionalModels.value.listingBaseViewModel

    val listingData = listingBaseViewModel.listingData

    val messageBarState : StateFlow<MessengerBarState> = combine(
        _isDisabledSendMes.state,
        _isDisabledAddPhotos.state,
        _responseImages.state,
        _messageTextState.state,
    ){ disabledSendMes, disabledAddPhotos, images, messageTextState ->
        MessengerBarState(
            isDisabledSendMes = disabledSendMes,
            isDisabledAddPhotos = disabledAddPhotos,
            imagesUpload = images,
            messageTextState = messageTextState
        )
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        MessengerBarState()
    )

    val dialogContentState : StateFlow<DialogContentState> = combine(
        _isMenuVisible.state,
        _responseGetConversation.state,
        _responseGetOfferInfo.state,
        _responseGetOrderInfo.state,
        listingData
    )
    { isMenuVisible, conversation, offerInfo, orderInfo, listingData ->

        val copyId = getString(strings.idCopied)

        val offer = offerInfo
        val order = orderInfo
        val sign = withContext(Dispatchers.IO){
            getString(strings.currencySign)
        }
        val orderLabel = withContext(Dispatchers.IO){
            getString(strings.orderLabel)
        }
        val copyOfferId = withContext(Dispatchers.IO){
            getString(strings.copyOfferId)
        }
        val copyOrderId = withContext(Dispatchers.IO){
            getString(strings.copyOrderId)
        }
        val deleteDialogLabel = withContext(Dispatchers.IO){
            getString(strings.deleteDialogLabel)
        }
        var userRole = ""

        val headerItem = when {
            offer.id != 1L -> {
                if (offer.sellerData?.markedAsDeleted == true) {
                    _isDisabledSendMes.value = true
                    _isDisabledAddPhotos.value = true
                } else {
                    _isDisabledSendMes.value = false

                    if (offer.sellerData?.id == conversation.interlocutor?.id) {
                        userRole = "buyer"
                        _isDisabledAddPhotos.value = true
                    } else {
                        userRole = "seller"
                        _isDisabledAddPhotos.value = false
                    }
                }

                val title = buildAnnotatedString {
                    append(offer.title ?: "")
                }

                val s = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = colors.priceTextColor
                        )
                    ) {
                        append(offer.currentPricePerItem.toString())
                        append(sign)
                    }
                }

                val imageUrl = offer.getOfferImagePreview()


                MesHeaderItem(
                    title = title,
                    subtitle = s,
                    image = imageUrl,
                ) {
                    component.goToOffer(offer.id)
                }

            }

            order.id != 1L -> {
                if (order.sellerData?.markedAsDeleted == true) {
                    _isDisabledSendMes.value = true
                    _isDisabledAddPhotos.value = true
                } else {
                    _isDisabledSendMes.value = false
                    _isDisabledAddPhotos.value = false

                    userRole = if (order.sellerData?.id == conversation.interlocutor?.id) {
                        "buyer"
                    } else {
                        "seller"
                    }
                }
                val title = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.titleTextColor)) {
                        append(orderLabel)
                    }
                    append(" #${order.id}")
                }

                val subtitle = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = colors.actionTextColor,
                        )
                    ) {
                        append(order.suborders.firstOrNull()?.title)
                    }
                }

                val imageUrl =
                    order.suborders.firstOrNull()?.getOfferImagePreview()

                MesHeaderItem(
                    title = title,
                    subtitle = subtitle,
                    image = imageUrl,
                ) {
                    val type = if (userRole != "seller") {
                        DealTypeGroup.BUY
                    } else {
                        DealTypeGroup.SELL
                    }
                    component.goToOrder(order.id, type)
                }


            }

            else -> {
                null
            }
        }

        DialogContentState(
            appBarState = SimpleAppBarData(
                menuData = MenuData(
                    isMenuVisible = isMenuVisible,
                    menuItems = listOf(
                        MenuItem(
                            id = "copyId",
                            title = if (conversation.aboutObjectClass == "offer")
                                copyOfferId
                            else copyOrderId,
                            icon = drawables.copyIcon,
                        ) {
                            clipBoardEvent(conversation.aboutObjectId.toString())
                            showToast(
                                successToastItem.copy(
                                    message = copyId
                                )
                            )
                        },
                        MenuItem(
                            id = "delete_dialog",
                            title = deleteDialogLabel,
                            icon = drawables.deleteIcon,
                        ) {
                            deleteConversation(conversation.id)
                        }
                    ),
                    closeMenu = {
                        _isMenuVisible.value = false
                    }
                ),
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        hasNews = false,
                        badgeCount = null,
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = {
                            updatePage()
                        }
                    ),
                    NavigationItem(
                        title = getString(strings.menuTitle),
                        hasNews = false,
                        badgeCount = null,
                        icon = drawables.menuIcon,
                        tint = colors.black,
                        onClick = {
                            _isMenuVisible.value = true
                        }
                    )
                ),
                onBackClick = {
                    if (!images.value.isNotEmpty()) {
                        component.onBackClicked()
                    } else {
                        closeImages()
                    }
                },
            ),
            responseGetOfferInfo = offerInfo,
            responseGetOrderInfo = orderInfo,
            mesHeader = headerItem,
            conversations = conversation
        )
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        DialogContentState()
    )

    val pagingParamsFlow: Flow<Pair<Conversations?,ListingData>> = combine(
        _responseGetConversation.state,
        listingData,
        updatePage
    ) { conversations, listingData, _ ->
        resetScroll()
        markReadConversation(dialogId)
        Pair(conversations, listingData)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<DialogsData>> = pagingParamsFlow.flatMapLatest { pair ->
        val conversation = pair.first
        val listingData = pair.second

        pagingRepository.getListing(
            listingData,
            apiService,
            Dialog.serializer(),
            onTotalCountReceived = {
                listingBaseViewModel.setTotalCount(it)
            }
        ).map { pagingData ->
            pagingData.map { dialog ->
                val isIncoming = (UserData.login != dialog.sender)
                val type = if (isIncoming) MessageType.INCOMING else MessageType.OUTGOING
                val textCopied = withContext(Dispatchers.IO){
                    getString(strings.textCopied)
                }

                DialogsData.MessageItem(
                    id = dialog.id,
                    message = dialog.message.orEmpty(),
                    dateTime = dialog.createdTs ?: 1L,
                    user = if (isIncoming)
                        conversation?.interlocutor?.id.toString()
                    else
                        UserData.userInfo?.login.toString(),
                    messageType = type,
                    images = dialog.images?.mapTo(ArrayList()) {
                        MesImage(
                            it.thumbUrl,
                            it.url
                        )
                    },
                    readByReceiver = dialog.readByReceiver == true,
                    options = listOf(
                        MenuItem(
                            id = "delete_message",
                            title = getString(strings.actionDelete),
                            icon = drawables.deleteIcon,
                        ) {
                            deleteMessage(dialog.id)
                        },
                        MenuItem(
                            id = "copy_message",
                            title = getString(strings.actionCopy),
                            icon = drawables.copyIcon,
                        ) {
                            clipBoardEvent(dialog.message ?: "")
                            showToast(
                                successToastItem.copy(
                                    message = textCopied
                                )
                            )
                        }
                    ),
                    events = DialogItemEventsImpl(this, dialog, component)
                )
            }
                .insertSeparators { before: DialogsData.MessageItem?, after: DialogsData.MessageItem? ->
                    val beforeDate = before?.dateTime?.convertDateYear()
                    val afterDate = after?.dateTime?.convertDateYear()

                    if (before != null && beforeDate != afterDate) {
                        DialogsData.SeparatorItem(
                            dateTime = beforeDate!!
                        )
                    } else {
                        null
                    }
                }
        }
    }.cachedIn(scope)

    init {
        update()
    }

    fun update(){
        setLoading(true)
        listingBaseViewModel.setReversingPaging(true)
        listingBaseViewModel.setListingData(
            ListingData(
                data = LD(
                    filters = listOf(
                        Filter(
                            "dialog_id",
                            dialogId.toString(),
                            "",
                            null
                        )
                    ),
                    sort = Sort(
                        "created_ts",
                        "desc",
                        "",
                        null,
                        null
                    ),
                    methodServer = "get_cabinet_listing",
                    objServer = "private_messages"
                )
            )
        )
        _messageTextState.value = message ?: ""

        getConversation(
            dialogId,
            onSuccess = { conversation ->
                _responseGetConversation.value = conversation
                updateDialogInfo(conversation)
                printLogD("updateDialogInfo", conversation.toString())
            },
            error = {
                component.onBackClicked()
            }
        )
    }

    fun markReadConversation(id : Long) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    conversationsOperations.postMarkAsReadByInterlocutor(id)
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    fun deleteConversation(id : Long) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                conversationsOperations.postDeleteForInterlocutor(id)
            }

            if(res != null){
                component.onBackClicked()
            }else{
                showToast(errorToastItem.copy(message = getString(strings.operationFailed)))
            }
        }
    }

    fun getConversation(id : Long, onSuccess: (Conversations) -> Unit, error: () -> Unit) {
        scope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    conversationsOperations.getConversation(id)
                }
                val buf = res.success
                val e = res.error

                if (buf!= null) {
                    onSuccess(res.success!!)
                }else{
                    error()
                    e?.let { throw it }
                }
            }catch (e : ServerErrorException){
                onError(e)
            }catch (e : Exception){
                onError(ServerErrorException(e.message ?: "", ""))
            }finally {
                setLoading(false)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getImages(files: PlatformFiles) {
        scope.launch {
            val newImages = withContext(Dispatchers.IO){
                files.map { file ->
                    val barr = file.readBytes()
                    val resizeImage = compressImage(barr, 40)

                    PhotoTemp(
                        file = file,
                        uri = getImageUriFromPlatformFile(file),
                        id = Uuid.random().toString(),
                        tempId = resizeImage.encodeToBase64()
                    )
                }
            }

            _responseImages.value = buildList {
                addAll(_responseImages.value)
                addAll(newImages.map {
                    PhotoSave(
                        id = it.id,
                        uri = it.uri,
                        tempId = it.tempId,
                        url = it.url,
                        rotate = it.rotate
                    )
                })
            }
        }
    }

    fun deleteImage(item: PhotoSave) {
        _responseImages.value = buildList {
            addAll(_responseImages.value)
            remove(item)
        }
    }

    fun sendMessage() {
        scope.launch {
            setLoading(true)

            val userId = _responseGetConversation.value.interlocutor?.id
            val interlocutorRole = _responseGetConversation.value.interlocutor?.role
            val idAboutDialog = _responseGetConversation.value.aboutObjectId
            val aboutObject = _responseGetConversation.value.aboutObjectClass
            val message = _messageTextState.value

            val bodyMessage = buildJsonObject {
                put("message", JsonPrimitive(message))
                _responseImages.value.forEachIndexed { index, photo ->
                    put("image_${index + 1}", JsonPrimitive(photo.tempId))
                }
            }
            _responseImages.value = emptyList()
            _messageTextState.value = ""

            val res = withContext(Dispatchers.IO) {
                conversationsOperations.postAddMessage(dialogId, bodyMessage)
            }

            if (res != null) {
                if (res == "true") {
                    if (interlocutorRole == "buyer") {
                        val eventParameters = mapOf(
                            "buyer_id" to userId,
                            "seller_id" to UserData.userInfo?.id,
                            (if (aboutObject == "offer") "lot_id" else "order_id") to idAboutDialog,
                            "message_type" to if (aboutObject == "offer") "lot" else "deal",
                        )
                        analyticsHelper.reportEvent("sent_message_to_buyer", eventParameters)
                    } else {
                        val eventParameters = mapOf(
                            "seller_id" to userId,
                            "buyer_id" to UserData.userInfo?.id,
                            (if (aboutObject == "offer") "lot_id" else "order_id") to idAboutDialog,
                            "message_type" to if (aboutObject == "offer") "lot" else "deal",
                        )
                        analyticsHelper.reportEvent("sent_message_to_seller", eventParameters)
                    }

                    updatePage()
                } else {
                    showToast(
                        errorToastItem.copy(
                            message = getString(strings.operationFailed)
                        )
                    )
                }
            }

            setLoading(false)
        }
    }

    fun updateDialogInfo(
        conversations: Conversations,
    ) {
        scope.launch(Dispatchers.IO) {
            if (conversations.aboutObjectClass == "offer") {
                val buffer = offerOperations.getOffer(conversations.aboutObjectId ?: 1L)
                val res = buffer.success
                res?.let { offer ->
                    _responseGetOfferInfo.value = offer
                }
            } else {
                val buf = orderOperations.getOrder(conversations.aboutObjectId ?: 1L)
                val res = buf.success
                res?.let {
                    _responseGetOrderInfo.value = it
                }
            }
        }
    }

    fun deleteMessage(id: Long) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                privateMessagesOperation.postDeleteForInterlocutor(id)
            }
            val buf = res.success
            val err = res.error
            if (buf != null) {
                showToast(
                    newToast = successToastItem.copy(
                        message = getString(strings.operationSuccess)
                    )
                )
                update()
            } else {
                showToast(
                    errorToastItem.copy(
                        message = err?.humanMessage ?: getString(strings.operationFailed)
                    )
                )
            }
        }
    }

    fun onMessageTextChanged(text: String){
        _messageTextState.value = text
    }

    fun openImages(index: Int, dialogItem: Dialog){
        _images.value = dialogItem.images?.map {
            it.url ?: ""
        } ?: emptyList()
        _selectedImageIndex.value = index
    }

    fun closeImages(){
        _selectedImageIndex.value = 0
    }
}

data class DialogItemEventsImpl(
    val viewModel: DialogsViewModel,
    val dialogItem: Dialog,
    val component: DialogsComponent,
) : DialogItemEvents {
    override fun openImage(index: Int) {
        viewModel.openImages(index, dialogItem)
    }

    override fun linkClicked(url: String) {
        when (val deepLink = parseDeepLink(url)) {
            is DeepLink.GoToOffer -> {
                component.goToOffer(deepLink.offerId)
            }

            is DeepLink.GoToListing -> {
                component.goToNewSearch(deepLink.ownerId ?: 1)
            }

            is DeepLink.GoToUser -> {
                component.goToUser(deepLink.userId)
            }

            is DeepLink.GoToAuth -> {
                openUrl(url)
            }

            is DeepLink.GoToDialog -> {
                openUrl(url)
            }

            is DeepLink.GoToDynamicSettings -> {
                openUrl(url)
            }

            DeepLink.GoToRegistration -> {
                openUrl(url)
            }

            is DeepLink.GoToVerification -> {
                openUrl(url)
            }

            is DeepLink.Unknown -> {
                openUrl(url)
            }

            null -> {}
        }
    }
}

data class MessageBarEventsImpl(
    val viewModel: DialogsViewModel,
) : MessengerBarEvents {
    override fun getImages(images: PlatformFiles) {
        viewModel.getImages(images)
    }

    override fun deleteImage(image: PhotoSave) {
        viewModel.deleteImage(image)
    }

    override fun onTextChanged(text: String) {
        viewModel.onMessageTextChanged(text)
    }

    override fun sendMessage() {
        viewModel.sendMessage()
    }
}
