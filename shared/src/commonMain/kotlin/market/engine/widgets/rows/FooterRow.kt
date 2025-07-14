package market.engine.widgets.rows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.TopCategory
import market.engine.widgets.items.FooterItem

@Composable
fun FooterRow(items : List<TopCategory>) {
    LazyRowWithScrollBars(
        heightMod = Modifier.background(color = colors.white, MaterialTheme.shapes.small).fillMaxWidth()
    ) {
        items(items) { item ->
            FooterItem(category = item) {}
        }
    }
}
