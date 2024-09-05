package market.engine.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.business.constants.ThemeResources.colors

@Composable
fun CategoryRow(categories: List<String?>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            Box(
                modifier = modifier
                    .padding(8.dp)
                    .background(colors.lightGray, shape = RoundedCornerShape(16.dp))
                    .clickable { /* Handle category click */ }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = category ?: "", color = colors.black)
            }
        }
    }
}
