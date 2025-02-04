package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.Subscription
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dropdown_menu.getSubscriptionOperations
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionItem(
    subscription: Subscription,
    listingData: LD,
    searchData: SD,
    baseViewModel: BaseViewModel,
) {
    val user = subscription.sellerData
    val showMenu = remember { mutableStateOf(false) }

    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            //header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ){
                Card(
                    modifier = Modifier.wrapContentSize(),
                    shape = CircleShape
                ) {
                    if (user != null) {
                        LoadImage(
                            url = user.avatar?.thumb?.content ?: "",
                            size = 40.dp,
                            isShowLoading = false,
                            isShowEmpty = false
                        )
                    }else{
                        Icon(
                            painter = painterResource(drawables.searchIcon),
                            contentDescription = null,
                            modifier = Modifier.size(dimens.mediumIconSize)
                        )
                    }
                }

                Column {
                    Text(
                        text = if (user != null) "${user.login} (${user.rating})" else subscription.name ?: subscription.searchQuery ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (user != null) colors.brightBlue else colors.black,
                        modifier = Modifier.padding(dimens.smallPadding)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
                    ) {
                        subscription.catpath?.onEachIndexed { index, cat ->
                            Text(
                                text = if (subscription.catpath.size - 1 == index)
                                    cat.value
                                else cat.value + "->",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (subscription.catpath.size - 1 == index) colors.black else colors.steelBlue,
                                modifier = Modifier.padding(dimens.smallPadding)
                                    .clickable {
                                        //go to Listing

                                    }
                            )
                        }
                    }
                }

                SmallIconButton(
                    drawables.menuIcon,
                    color = colors.steelBlue,
                ){
                    showMenu.value = true
                }

                if (showMenu.value){
                    getSubscriptionOperations(
                        subscription,
                        baseViewModel
                    ){
                        showMenu.value = false
                    }
                }
            }


        }
    }
}
