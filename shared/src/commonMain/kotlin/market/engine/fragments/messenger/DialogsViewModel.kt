package market.engine.fragments.messenger

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import app.cash.paging.cachedIn
import app.cash.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import market.engine.common.AnalyticsFactory
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DialogsData
import market.engine.core.data.items.ListingData
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.types.MessageType
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.Dialog
import market.engine.core.network.networkObjects.MesImage
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.core.utils.convertDateYear
import market.engine.fragments.base.BaseViewModel

class DialogsViewModel(
    private val apiService: APIService,
    private val userRepository: UserRepository,
    private val conversationsOperations: ConversationsOperations,
    private val offerOperations: OfferOperations,
    private val orderOperations: OrderOperations,
) : BaseViewModel() {
    private val dialogsPagingRepository: PagingRepository<Dialog> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    private val _responseGetConversation = MutableStateFlow<Conversations?>(null)
    val responseGetConversation : StateFlow<Conversations?> = _responseGetConversation.asStateFlow()

    private val _responseGetOfferInfo = MutableStateFlow<Offer?>(null)
    val responseGetOfferInfo : StateFlow<Offer?> = _responseGetOfferInfo.asStateFlow()

    private val _responseGetOrderInfo = MutableStateFlow<Order?>(null)
    val responseGetOrderInfo : StateFlow<Order?> = _responseGetOrderInfo.asStateFlow()

    private val _responseImages = MutableStateFlow<List<PhotoTemp>>(emptyList())
    val responseImages: StateFlow<List<PhotoTemp>> = _responseImages.asStateFlow()

    val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    val messageTextState = mutableStateOf("")

    fun init(dialogId : Long): Flow<PagingData<DialogsData>> {

        getConversation(dialogId)

        listingData.value.data.value.filters = arrayListOf(
            Filter(
                "dialog_id",
                dialogId.toString(),
                "",
                null
            )
        )
        listingData.value.data.value.sort = Sort(
            "created_ts",
            "desc",
            "",
            null,
            null
        )
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "private_messages"

        return dialogsPagingRepository.getListing(listingData.value, apiService, Dialog.serializer())
            .map { pagingData ->
                pagingData.map { dialog ->
                    val isIncoming = (responseGetConversation.value?.interlocutor?.id == dialog.sender)
                    val type = if (isIncoming) MessageType.INCOMING else MessageType.OUTGOING

                    DialogsData.MessageItem(
                        id = dialog.id,
                        message = dialog.message.orEmpty(),
                        dateTime = dialog.createdTs,
                        user = if (isIncoming)
                            responseGetConversation.value?.interlocutor?.login.orEmpty()
                        else
                            UserData.userInfo?.login.toString(),
                        messageType = type,
                        images = dialog.images?.mapTo(ArrayList()) { MesImage(it.thumbUrl, it.url) },
                        readByReceiver = dialog.readByReceiver
                    )
                }.insertSeparators { before: DialogsData.MessageItem?, after: DialogsData.MessageItem? ->
                    val beforeDate = before?.dateTime.toString().convertDateYear()
                    val afterDate = after?.dateTime.toString().convertDateYear()

                    if (beforeDate != afterDate && before != null) {
                        DialogsData.SeparatorItem(
                            dateTime = beforeDate
                        )
                    } else {
                        null
                    }
                }
            }
            .cachedIn(viewModelScope)
    }

    fun onRefresh(){
        dialogsPagingRepository.refresh()
    }

    fun getImages(pickImagesRaw : List<PhotoTemp>) {
        viewModelScope.launch {
            _responseImages.value = buildList {
                addAll(_responseImages.value)
                pickImagesRaw.forEach {
                    if (size < MAX_IMAGE_COUNT) {
                        add(it)
                    }
                }
            }
        }
    }

    fun deleteImage(item : PhotoTemp){
        _responseImages.value = buildList {
            addAll(_responseImages.value)
            remove(item)
        }
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateToken()
            userRepository.updateUserInfo()
        }
    }

    fun sendMessage(dialogId: Long, message : String){
        viewModelScope.launch {
            setLoading(true)

            val userId = responseGetConversation.value?.interlocutor?.id
            val interlocutorRole = responseGetConversation.value?.interlocutor?.role
            val idAboutDialog = responseGetConversation.value?.aboutObjectId
            val aboutObject = responseGetConversation.value?.aboutObjectClass

            val bodyMessage = buildJsonObject {
                put("message", JsonPrimitive(message))
                responseImages.value.forEachIndexed { index, photo ->
                    put("image_${index + 1}", JsonPrimitive(photo.tempId))
                }
            }
            val res = withContext(Dispatchers.IO){
                conversationsOperations.postAddMessage(dialogId,bodyMessage)
            }

            withContext(Dispatchers.Main) {
                setLoading(false)
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
                            analyticsHelper.reportEvent("sent_message_to_buyer", eventParameters)
                        }
                        _responseImages.value = emptyList()
                        messageTextState.value = ""
                        onRefresh()
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = res
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getConversation(id : Long) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    conversationsOperations.getConversation(id)
                }
                if (res != null) {
                    updateDialogInfo(res)
                    _responseGetConversation.value = res
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    private fun updateDialogInfo(
        conversations: Conversations,
    ){
        viewModelScope.launch(Dispatchers.IO) {
            if(conversations.aboutObjectClass == "offer") {
                val buffer = offerOperations.getOffer(conversations.aboutObjectId)
                val res = buffer.success
                res.let { offer ->
                    _responseGetOfferInfo.value = offer
                }
            }
            else
            {
                val buf = orderOperations.getOrder(conversations.aboutObjectId)
                val res = buf.success
                res.let {
                    _responseGetOrderInfo.value = it
                }
            }
        }
    }
}
