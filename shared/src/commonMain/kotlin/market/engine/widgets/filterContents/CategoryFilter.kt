package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.core.baseFilters.LD
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Category
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.ilustrations.getCategoryIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun CategoryFilter(
    listingData: LD,
    onClosed: () -> Unit
) {
    val categoryOperations : CategoryOperations = koinInject()

    val categories = remember { mutableStateListOf<Category>() }

    val cat = remember { listingData.filters.find { it.key == "category" } }

    val defCat = stringResource(strings.categoryMain)

    val catTitle = remember {
        mutableStateOf(cat?.interpritation ?: defCat)
    }

    val pastCategory = remember { mutableStateListOf(cat) }

    LaunchedEffect(catTitle.value) {
        withContext(Dispatchers.IO){
            val res = categoryOperations.getCategories(cat?.value?.toLongOrNull() ?: 1L)

            withContext(Dispatchers.Main){
                if (res != null) {
                    categories.clear()
                    categories.addAll(res)
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(dimens.smallPadding)

    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 60.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (pastCategory.size > 1 ||
                    (pastCategory.lastOrNull() != null &&
                    pastCategory.lastOrNull()?.value?.toLongOrNull() != 1L)
                ) {
                    IconButton(
                        onClick = {
                            val item = pastCategory.lastOrNull()
                            if (item != null) {
                                listingData.filters.find { it.key == "category" }?.value = item.value
                                listingData.filters.find { it.key == "category" }?.interpritation = item.interpritation

                                if(!item.interpritation.isNullOrEmpty()) {
                                    catTitle.value = item.interpritation!!
                                }else{
                                    catTitle.value = defCat
                                }

                                pastCategory.removeLast()
                            }
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(strings.menuTitle),
                            modifier = Modifier.size(dimens.smallIconSize),
                            tint = colors.black
                        )
                    }
                }

                Text(
                    catTitle.value,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(0.6f)
                )

                IconButton(
                    onClick = {
                        onClosed()
                    },
                    content = {
                        Icon(
                            painterResource(drawables.closeBtn),
                            tint = colors.black,
                            contentDescription = stringResource(strings.actionClose)
                        )
                    },
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
            val isSelect = remember { mutableStateOf(1L) }
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    LazyColumn {
                        items(categories) { category ->
                            Spacer(modifier = Modifier.height(dimens.smallSpacer))

                            NavigationDrawerItem(
                                label = {
                                    Box(
                                        modifier = Modifier.wrapContentWidth()
                                            .wrapContentHeight(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            category.name ?: "",
                                            color = colors.black,
                                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                            lineHeight = dimens.largeText
                                        )
                                    }
                                },
                                onClick = {
                                    if (!category.isLeaf) {
                                        catTitle.value = category.name ?: defCat
                                        pastCategory.add(cat?.copy())
                                        listingData.filters.find { it.key == "category" }?.value =
                                            category.id.toString()
                                        listingData.filters.find { it.key == "category" }?.interpritation =
                                            category.name
                                    } else {
                                        isSelect.value = category.id
                                        listingData.filters.find { it.key == "category" }?.value =
                                            category.id.toString()
                                        listingData.filters.find { it.key == "category" }?.interpritation =
                                            category.name
                                    }
                                },
                                icon = {
                                    getCategoryIcon(category.name)?.let {
                                        Image(
                                            painterResource(it),
                                            contentDescription = null,
                                            modifier = Modifier.size(dimens.smallIconSize)
                                        )
                                    }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = colors.selected,
                                    unselectedContainerColor = colors.white,
                                    selectedIconColor = colors.grayLayout,
                                    unselectedIconColor = colors.white,
                                    selectedTextColor = colors.grayLayout,
                                    selectedBadgeColor = colors.grayLayout,
                                    unselectedTextColor = colors.white,
                                    unselectedBadgeColor = colors.white
                                ),
                                shape = MaterialTheme.shapes.small,
                                selected = isSelect.value == category.id
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                .align(Alignment.BottomCenter)
        ) {
            AcceptedPageButton(
                strings.actionAcceptFilters,
                Modifier.align(Alignment.Center),
            ) {
                onClosed()
            }
        }
    }
}
