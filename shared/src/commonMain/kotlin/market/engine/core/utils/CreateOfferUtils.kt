package market.engine.core.utils

import androidx.compose.ui.text.input.KeyboardType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.network.networkObjects.Fields


fun checkNumberKeyBoard(field : Fields): KeyboardType {
    if (field.validators?.isNotEmpty() == true) {
        return when(field.validators[0].type){
            "positive_integer","integer" -> {
                KeyboardType.Number
            }
            "positive_float", "float" -> {
                KeyboardType.Number
            }

            else -> {
                KeyboardType.Text
            }
        }
    }else{
        return KeyboardType.Text
    }
}

fun checkValidation(field : Fields, value : String): JsonPrimitive {
    if (field.validators?.isNotEmpty() == true) {
        return when(field.validators[0].type){
            "positive_integer","integer" -> {
                JsonPrimitive(value.toLongOrNull() ?: 1)
            }

            "positive_float", "float" -> {
                JsonPrimitive(value.toFloatOrNull() ?: 0f)
            }

            else -> {
                JsonPrimitive(value)
            }
        }
    }else{
        return JsonPrimitive(value)
    }
}

fun processInput(input: Any?): String? {
    return when (input) {
        is JsonPrimitive -> input.content
        is JsonArray -> input.filterIsInstance<JsonPrimitive>()
            .filter { it.isString }
            .joinToString(separator = " ") { it.content }
        else -> null
    }
}
