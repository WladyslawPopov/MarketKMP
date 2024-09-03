package market.engine.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.theme.ThemeResources

@Composable
fun SearchBar(modifier: Modifier = Modifier, themeResources: ThemeResources, onSearchClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(themeResources.colors.lightGray, shape = RoundedCornerShape(24.dp))
            .clickable { onSearchClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Search", color = themeResources.colors.black, fontSize = 18.sp)
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = themeResources.colors.black)
        }
    }
}
