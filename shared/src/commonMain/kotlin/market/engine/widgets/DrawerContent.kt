package market.engine.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent() {
    // Содержимое бокового меню
    Column(modifier = Modifier.wrapContentWidth().padding(16.dp)) {
        Text("Top 100", modifier = Modifier.clickable { /* Handle click */ })
        Text("Help", modifier = Modifier.clickable { /* Handle click */ })
        Text("Contact Us", modifier = Modifier.clickable { /* Handle click */ })
        // Добавь другие элементы меню
    }
}
