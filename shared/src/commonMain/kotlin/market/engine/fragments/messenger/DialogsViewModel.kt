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
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.DialogsData
import market.engine.core.data.items.ListingData
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

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateToken()
            userRepository.updateUserInfo()
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
