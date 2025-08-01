package market.engine.widgets.filterContents.deliveryCardsContents

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform

class DeliveryCardsViewModel(savedStateHandle: SavedStateHandle): CoreViewModel(savedStateHandle) {
    private val _deliveryCards = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "deliveryCards",
        emptyList(),
        ListSerializer(DeliveryAddress.serializer())
    )
    val deliveryCardsState = _deliveryCards.state

    private val _deliveryFields = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "deliveryFields",
        emptyList(),
        ListSerializer(Fields.serializer())
    )
    val deliveryFieldsState = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "deliveryFields",
        emptyList(),
        ListSerializer(Fields.serializer())
    )

    private val _showFields = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "showFields",
        false,
        Boolean.serializer()
    )
    val showFieldsState = _showFields.state

    private val _selectedCard = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "selectedCard",
        1L,
        Long.serializer()
    )
    val selectedCardState = _selectedCard.state


    private val userOperations by lazy { KoinPlatform.getKoin().get<UserOperations>() }

    init {
        getDeliveryCards()
    }

    fun refreshCards(){
        getDeliveryCards()
        refresh()
    }

    fun setDeliveryFields(selectedId : Long?) {
        val cards = _deliveryCards.value

        viewModelScope.launch {
            val cards = cards
            val card = cards.find { it.id == selectedId }
            _deliveryFields.value = if (card != null) {
                val fields = (getDeliveryFields() ?: emptyList())
                fields.forEach { field ->
                    when (field.key) {
                        "zip" -> {
                            if (card.zip != null) {
                                field.data = JsonPrimitive(card.zip)
                            }
                        }

                        "city" -> {
                            field.data = card.city
                        }

                        "address" -> {
                            if (card.address != null) {
                                field.data = JsonPrimitive(card.address)
                            }
                        }

                        "phone" -> {
                            if (card.phone != null) {
                                field.data = JsonPrimitive(card.phone)
                            }
                        }

                        "surname" -> {
                            if (card.surname != null) {
                                field.data = JsonPrimitive(card.surname)
                            }
                        }

                        "other_country" -> {
                            if (card.country != null) {
                                field.data = JsonPrimitive(card.country)
                            }
                        }

                        "country" -> {
                            field.data =
                                JsonPrimitive(if (card.country == getString(ThemeResources.strings.countryDefault)) 0 else 1)
                        }
                    }
                    field.errors = null
                }
                fields
            } else {
                getDeliveryFields() ?: emptyList()
            }
        }
    }

    fun getDeliveryCards() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    userOperations.getUsersOperationsAddressCards(UserData.login)
                }

                val payload = response.success
                val err = response.error
                val cards = payload?.body?.addressCards

                if (cards != null) {
                    _deliveryCards.value = payload.body.addressCards

                    cards.find { it.isDefault }?.id?.let { card ->
                        setDeliveryFields(card)
                        _selectedCard.value = card
                        _showFields.value = cards.find { it.isDefault }?.address?.isBlank() == true
                    }
                } else {
                    throw err ?: ServerErrorException(errorCode = "Error", humanMessage = "")
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
            }
        }
    }

    fun saveDeliveryCard(cardId: Long?) {
        val cards = _deliveryCards.value
        val deliveryFields = _deliveryFields.value
        setLoading(true)
        viewModelScope.launch {
            val jsonBody : HashMap<String, JsonElement> = hashMapOf()
            deliveryFields.forEach { field ->
                when (field.widgetType) {
                    "input" -> {
                        if (field.data != null) {
                            jsonBody.put(field.key.toString(), field.data!!)
                        }
                    }

                    "hidden" -> {
                        if (cardId != null) {
                            jsonBody.put(field.key.toString(), JsonPrimitive(cardId))
                        }
                    }

                    else -> {}
                }
            }

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "save_address_cards",
                    "users",
                    jsonBody
                )
            }

            withContext(Dispatchers.Main) {
                val payload = res.success
                val err = res.error

                if (payload != null) {
                    if (payload.status == "operation_success") {

                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to jsonBody
                        )
                        analyticsHelper.reportEvent(
                            "save_address_cards_success",
                            eventParameters
                        )

                        _showFields.value = false
                        _selectedCard.value = cards.find { it.isDefault }?.id ?: 1L

                        refreshCards()
                    } else {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to jsonBody
                        )
                        analyticsHelper.reportEvent(
                            "save_address_cards_failed",
                            eventParameters
                        )
                        payload.recipe?.fields?.let { _deliveryFields.value = it }
                    }
                } else {
                    err?.let { onError(it) }
                }

                setLoading(false)
            }
        }
    }

    fun addNewDeliveryCard(){
        viewModelScope.launch {
            _deliveryFields.value = getDeliveryFields() ?: emptyList()
            _showFields.value = true
        }
    }

    fun setActiveCard(card: DeliveryAddress){
        setDeliveryFields(card.id)
        _selectedCard.value = card.id
    }

    fun setNewField(field: Fields){
        _deliveryFields.update {
            it.map { item ->
                if (item.key == field.key) {
                    field
                } else {
                    item
                }
            }
        }
    }

    fun editCard(){
        val selectedCards = _selectedCard.value
        setDeliveryFields(selectedCards)
        _showFields.value = true
    }

    fun closeFields(){
        val cards = _deliveryCards.value
        _showFields.value = false
        setDeliveryFields(cards.find { it.isDefault }?.id)
        _selectedCard.value = cards.find { it.isDefault }?.id ?: 1L
    }

    fun deleteCard(){
        val cards = _deliveryCards.value
        val selectedCards = _selectedCard.value
        cards.find { it.id == selectedCards }?.let { address ->
            updateDeleteCard(
                address
            ){
                _showFields.value = false
                _selectedCard.value = 1L
                refreshCards()
            }
        }
    }

    suspend fun getDeliveryFields(): List<Fields>? {
        val res = withContext(Dispatchers.IO) {
            operationsMethods.getOperationFields(
                UserData.login,
                "save_address_cards",
                "users"
            )
        }
        return withContext(Dispatchers.Main) {
            val payload = res.success
            val err = res.error

            if (payload != null) {
                return@withContext payload.fields
            } else {
                if (err != null)
                    onError(err)
                return@withContext null
            }
        }
    }

    fun updateDeleteCard(card: DeliveryAddress, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val b = HashMap<String, JsonElement>()
            b["id_as_ts"] = JsonPrimitive(card.id)

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "remove_address_card",
                    "users",
                    b
                )
            }
            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    showToast(
                        successToastItem.copy(
                            message = getString(ThemeResources.strings.operationSuccess)
                        )
                    )
                    onSuccess()
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    fun updateDefaultCard(card: DeliveryAddress, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val b = HashMap<String, JsonElement>()
            b["id_as_ts"] = JsonPrimitive(card.id)

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "set_default_address_card",
                    "users",
                    b
                )
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    showToast(
                        successToastItem.copy(
                            message = getString(ThemeResources.strings.operationSuccess)
                        )
                    )
                    delay(2000)

                    onSuccess()
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }
}
