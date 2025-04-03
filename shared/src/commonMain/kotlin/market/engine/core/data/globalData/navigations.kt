package market.engine.core.data.globalData

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import market.engine.core.data.items.TopCategory
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getWindowType

var listTopCategory : MutableList<TopCategory> = mutableListOf()

val isBigScreen : MutableState<Boolean> = mutableStateOf(getWindowType() == WindowType.Big)
