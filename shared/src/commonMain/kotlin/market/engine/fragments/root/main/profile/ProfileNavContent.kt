package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.bars.UserPanel
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileNavContent(
    list: List<NavigationItem>,
    activeTitle: String? = null,
    goToSettings: ((String) -> Unit)? = null,
) {
    LazyColumnWithScrollBars(
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            UserPanel(
                modifier = Modifier.wrapContentSize().padding(dimens.mediumPadding),
                UserData.userInfo,
                updateTrigger = 1,
                goToUser = {
                    list.find { it.icon == drawables.profileIcon }?.onClick?.invoke()
                },
                goToSettings = {
                    goToSettings?.invoke(it)
                },
            )
        }

        itemsIndexed(list) { _, item ->
            if (item.isVisible) {
                getNavigationItem(
                    item,
                    label = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.Start
                        ) {
                            val builder = buildAnnotatedString {
                                if (item.icon != drawables.balanceIcon){
                                    append(item.title)
                                }else{
                                    val color = when{
                                        (UserData.userInfo?.balance?:0.0) > 0 -> colors.positiveGreen
                                        (UserData.userInfo?.balance?:0.0) < 0 -> colors.negativeRed
                                        else -> colors.black
                                    }
                                    append(item.title)
                                    withStyle(
                                        SpanStyle(
                                            color = color,
                                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ){
                                        append(" ${UserData.userInfo?.balance} ${
                                            stringResource(
                                                strings.currencyCode)
                                        }")
                                    }
                                }
                            }
                            Text(
                                builder,
                                color = colors.black,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                lineHeight = dimens.largeText,
                            )
                            if (item.subtitle != null) {
                                Text(
                                    item.subtitle,
                                    color = colors.grayText,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    lineHeight = dimens.largeText
                                )
                            }
                        }
                    },
                    isSelected = item.title == activeTitle
                ){
                    item.onClick()
                }
            }
        }

        item {  }
    }
}
