package market.engine.widgets.badges

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.coil3.CoilImage
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.items.NavigationItem
import market.engine.core.util.getImage
import market.engine.core.util.printLogD
import org.jetbrains.compose.resources.painterResource

@Composable
fun getBadgedBox(
    modifier: Modifier = Modifier,
    item: NavigationItem,
    selected: Boolean = false
) {
    val imageLoadFailed = remember { mutableStateOf(false) }

    BadgedBox(
        modifier = modifier,
        badge = {
            if (item.badgeCount != null) {
                Badge{
                    Text(text = item.badgeCount.toString(), fontSize = 9.sp)
                }
            } else {
                if (item.hasNews) {
                    Badge()
                }
            }
        }
    ) {
        if (item.image != null){
            Card{
                if (imageLoadFailed.value) {
                    getImage(item.image, 30.dp)
                } else {
                    CoilImage(
                        modifier = Modifier.size(30.dp),
                        imageModel = {
                            item.image
                        },
                        previewPlaceholder = painterResource(item.icon),
                        failure = { e ->
                            imageLoadFailed.value = true
                            printLogD("Coil", e.reason?.message)
                        }
                    )
                }
            }
        }else {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = item.title,
                tint = if (!selected) item.tint else item.tintSelected,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
