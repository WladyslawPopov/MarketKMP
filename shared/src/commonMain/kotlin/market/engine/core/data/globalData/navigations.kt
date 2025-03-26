package market.engine.core.data.globalData

import market.engine.core.data.items.TopCategory
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getWindowType

var listTopCategory : MutableList<TopCategory> = mutableListOf()

var isBigScreen : Boolean = getWindowType() == WindowType.Big
