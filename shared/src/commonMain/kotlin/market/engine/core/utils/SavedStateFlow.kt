package market.engine.core.utils

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class SavedStateFlow<T>(
    scope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    private val serializer: KSerializer<T>,
    initialValue: T
) {
    private val _state = MutableStateFlow(
        savedStateHandle.decodeFromJson(key, serializer) ?: initialValue
    )

    val state = _state.asStateFlow()

    init {
        _state
            .onEach { newValue ->
                savedStateHandle.encodeToJson(key, serializer, newValue)
            }
            .launchIn(scope)
    }

    var value: T
        get() = _state.value
        set(newValue) {
            _state.value = newValue
        }

    suspend fun asyncUpdate(function: suspend (T) -> T) {
        _state.value = function(_state.value)
    }

    fun update(function: (T) -> T) {
        _state.value = function(_state.value)
    }
}

fun <T> SavedStateHandle.decodeFromJson(key: String, serializer: KSerializer<T>): T? {
    return get<String>(key)?.let { jsonString ->
        try {
            Json.decodeFromString(serializer, jsonString)
        } catch (_: Exception) {
            null
        }
    }
}

fun <T> SavedStateHandle.encodeToJson(key: String, serializer: KSerializer<T>, value: T?) {
    if (value == null) {
        remove<String>(key)
    } else {
        set(key, Json.encodeToString(serializer, value))
    }
}

inline fun <reified T> SavedStateHandle.getSavedStateFlow(
    scope: CoroutineScope,
    key: String,
    initialValue: T,
    serializer: KSerializer<T>
) = SavedStateFlow(scope, this, key, serializer, initialValue)
