package market.engine.widgets.filterContents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogsFilterContent(
    isRefreshing: MutableState<Boolean>,
    filters: ArrayList<Filter>,
    onClose : () -> Unit,
) {
    val listingData by remember { mutableStateOf(filters) }

    val focusManager: FocusManager = LocalFocusManager.current

    val typeFilters = listOf(
        "about_order" to stringResource(strings.aboutOrderLabel),
        "about_offer" to stringResource(strings.aboutOfferLabel)
    )
    var isExpanded by remember {
        mutableStateOf(true)
    }

    val selectedFilterKey = remember {
        mutableStateOf(
            listingData.find {
                it.key == "about_order"
            }?.interpritation ?:
            listingData.find {
                it.key == "about_offer"
            }?.interpritation
        )
    }


    val userLoginTextState = remember { mutableStateOf(filters.find { it.key == "interlocutor_login" }?.value ?: "") }
    val userIdTextState = remember { mutableStateOf(filters.find { it.key == "interlocutor_id" }?.value ?: "") }
    val idObjectTextState = remember { mutableStateOf(filters.find { it.key == "object_id"}?.value ?: "") }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        contentAlignment = Alignment.TopCenter
    ) {
        //Header Filters
        Row(
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        onClose()
                    },
                    content = {
                        Icon(
                            painterResource(drawables.closeBtn),
                            tint = colors.black,
                            contentDescription = stringResource(strings.actionClose)
                        )
                    },
                )

                Text(
                    stringResource(strings.filter),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(dimens.smallPadding)
                )
            }

            if (isRefreshing.value || listingData.find { it.interpritation != null && it.interpritation != "" && it.key !in listOf("state", "with_sales", "without_sales") } != null) {
                Button(
                    onClick = {
                        listingData.clear()
                        MsgFilters.clearFilters()
                        listingData.addAll(MsgFilters.filters)
                        isRefreshing.value = true
                        onClose()
                    },
                    content = {
                        Text(
                            stringResource(strings.clear),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.black
                        )
                    },
                    colors = colors.simpleButtonColors
                )
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.padding(bottom = 60.dp, top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                //expand
                item {
                    ExpandableSection(
                        title = stringResource(strings.conversationsLabel),
                        isExpanded = isExpanded,
                        onExpandChange = { isExpanded = !isExpanded },
                        content = {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 500.dp)
                            ) {
                                items(typeFilters) { filter ->
                                    val (filterKey, filterText) = filter
                                    val isChecked = selectedFilterKey.value == filterText
                                    val onClick = {
                                        if (isChecked) {
                                            listingData.find { it.key == filterKey }?.value = ""
                                            listingData.find { it.key == filterKey }?.interpritation = null
                                            selectedFilterKey.value = null
                                        }else{
                                            typeFilters.forEach { filter->
                                                if (filter.first != filterKey) {
                                                    listingData.find { it.key == filter.first }?.value = ""
                                                    listingData.find { it.key == filter.first }?.interpritation = null
                                                }
                                            }
                                            listingData.find { it.key == filterKey }?.value = "true"
                                            listingData.find { it.key == filterKey }?.interpritation = filterText
                                            selectedFilterKey.value = filterText
                                        }
                                        isRefreshing.value = true
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                onClick()
                                            }
                                    ) {
                                        RadioButton(
                                            isChecked,
                                            {
                                                onClick()
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colors.inactiveBottomNavIconColor,
                                                unselectedColor = colors.black
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(dimens.smallPadding))
                                        Text(
                                            filterText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.width(dimens.smallPadding))
                                    }
                                }
                            }
                        }
                    )
                }
                //user search
                item {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val userId = stringResource(strings.userIdParameterName)
                        val userLogin = stringResource(strings.userLoginParameterName)

                        if (filters.find { it.key == "interlocutor_id" }?.value != null) {
                            TextFieldWithState(
                                label = userId,
                                textState = userIdTextState,
                                onTextChange = { text ->
                                    if (userIdTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "interlocutor_id" }?.apply {
                                            value = text
                                            interpritation = "$userLogin: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "interlocutor_id" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    userIdTextState.value = text
                                    isRefreshing.value = true
                                },
                                isNumber = true,
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }

                        if (filters.find { it.key == "interlocutor_login" }?.value != null) {
                            TextFieldWithState(
                                label = userLogin,
                                textState = userLoginTextState,
                                onTextChange = { text ->
                                    if (userLoginTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "interlocutor_login" }?.apply {
                                            value = text
                                            interpritation = "$userLogin: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "interlocutor_login" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    userLoginTextState.value = text
                                    isRefreshing.value = true
                                },
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }
                    }
                }
                //id object
                item {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val objectId = stringResource(strings.offerOrderIdParameterName)

                        if (filters.find { it.key == "object_id" }?.value != null) {
                            TextFieldWithState(
                                label = objectId,
                                textState = idObjectTextState,
                                onTextChange = { text ->
                                    if (idObjectTextState.value.isNotBlank()) {
                                        filters.find { filter -> filter.key == "object_id" }?.apply {
                                            value = text
                                            interpritation = "$objectId: $text"
                                        }
                                    } else {
                                        filters.find { it.key == "object_id" }.let {
                                            it?.value = ""
                                            it?.interpritation = null
                                        }
                                    }
                                    idObjectTextState.value = text
                                    isRefreshing.value = true
                                },
                                isNumber = true,
                                modifier = Modifier.widthIn(max = 250.dp).weight(1f)
                            )
                        }
                    }
                }
            }
        }

        AcceptedPageButton(
            strings.actionAcceptFilters,
            Modifier.align(Alignment.BottomCenter)
                .wrapContentWidth()
                .padding(dimens.mediumPadding)
        ){
            onClose()
        }
        Spacer(modifier = Modifier.height(dimens.mediumSpacer))
    }
}

