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
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.baseFilters.LD
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Category
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.ilustrations.getCategoryIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CategoryFilter(
    listingData: State<LD>,
    sheetState: BottomSheetScaffoldState,
    scope: CoroutineScope,
    isRefreshing: MutableState<Boolean>,
) {
    val categoryOperations : CategoryOperations = koinInject()
    val isRefresh = remember { mutableStateOf(true) }
    val categories = remember { mutableStateListOf<Category>() }
    val cat = remember { mutableStateOf( listingData.value.filters?.find { it.key == "category" } ) }
    val defCat = stringResource(strings.categoryMain)
    val catTitle = remember {
        mutableStateOf(
            if(!cat.value?.interpritation.isNullOrEmpty())
            cat.value?.interpritation ?: defCat else defCat
        )
    }

    val pastCategory = remember { mutableStateListOf(cat.value) }

    LaunchedEffect(isRefresh.value) {
        snapshotFlow {
            cat.value
        }.collect { c ->
            scope.launch {
                withContext(Dispatchers.IO){
                    val res = categoryOperations.getCategories(c?.value?.toLongOrNull() ?: 1L)

                    withContext(Dispatchers.Main){
                        if (res != null) {
                            categories.clear()
                            categories.addAll(res)
                            isRefresh.value = false
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(sheetState){
        snapshotFlow {
            sheetState.bottomSheetState.isCollapsed
        }.collect {
            cat.value = listingData.value.filters?.find { it.key == "category" }
            catTitle.value = if(!cat.value?.interpritation.isNullOrEmpty())
                        cat.value?.interpritation ?: defCat else defCat

            if (cat.value?.interpritation == null) {
                pastCategory.clear()
                pastCategory.add(cat.value?.copy())
            }

            isRefresh.value = true
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
                    (pastCategory.lastOrNull()?.value != null &&
                    pastCategory.lastOrNull()?.value?.toLongOrNull() != 1L)
                ) {
                    IconButton(
                        onClick = {
                            val item = pastCategory.lastOrNull()
                            if (item != null) {
                                listingData.value.filters?.find { it.key == "category" }?.value =
                                    item.value
                                listingData.value.filters?.find { it.key == "category" }?.interpritation =
                                    item.interpritation

                                if(!item.interpritation.isNullOrEmpty()) {
                                    catTitle.value = item.interpritation!!
                                }else{
                                    catTitle.value = defCat
                                }

                                pastCategory.removeLast()
                                isRefresh.value = true
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
                        scope.launch {
                            sheetState.bottomSheetState.collapse()
                        }
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

            AnimatedVisibility(
                visible = sheetState.bottomSheetState.isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    LazyColumn {
                        items(categories) { category ->
                            Spacer(modifier = Modifier.height(dimens.smallSpacer))

                            val isSelect = remember { mutableStateOf(false) }
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
                                    catTitle.value = category.name ?: defCat
                                    if (!category.isLeaf) {
                                        pastCategory.add(cat.value?.copy())
                                        listingData.value.filters?.find { it.key == "category" }?.value =
                                            category.id.toString()
                                        listingData.value.filters?.find { it.key == "category" }?.interpritation =
                                            category.name

                                        isRefresh.value = true
                                    } else {
                                        isSelect.value = true
                                        listingData.value.filters?.find { it.key == "category" }?.value =
                                            category.id.toString()
                                        listingData.value.filters?.find { it.key == "category" }?.interpritation =
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
                                selected = isSelect.value
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
                isRefreshing.value = true
                scope.launch {
                    sheetState.bottomSheetState.collapse()
                }
            }
        }
    }
}
