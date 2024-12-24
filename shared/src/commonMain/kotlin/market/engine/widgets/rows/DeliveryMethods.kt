package market.engine.widgets.rows

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.network.networkObjects.Fields

@Composable
fun DeliveryMethods(
    field: Fields,
    modifier: Modifier = Modifier,
) {

    LazyColumn {
        field.choices?.let {
            items(it){

            }
        }
    }
}
