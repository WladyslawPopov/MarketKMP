package market.engine.widgets.filterContents

import androidx.compose.runtime.MutableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.checkboxs.RadioOptionRow
import market.engine.widgets.dropdown_menu.ExpandableSection
import market.engine.widgets.bars.FilterContentHeaderBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogsFilterContent(
    isRefreshing: MutableState<Boolean>,
    filters: ArrayList<Filter>,
    onClose : () -> Unit,
) {
    val focusManager: FocusManager = LocalFocusManager.current

    val checkSize: () -> Boolean = {
        filters.any { it.interpretation?.isNotBlank() == true }
    }

    val isShowClear = remember { mutableStateOf(checkSize()) }

    val typeFilters = listOf(
        "about_order" to stringResource(strings.aboutOrderLabel),
        "about_offer" to stringResource(strings.aboutOfferLabel)
    )
    
    var isExpanded by remember {
        mutableStateOf(true)
    }

    val selectedFilterKey = remember {
        mutableStateOf(
            typeFilters.find { f->
                filters.find { it.key == f.first && it.interpretation?.isNotBlank() == true } != null
            }?.first
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
        }.padding(dimens.smallPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        //Header Filters
        FilterContentHeaderBar(
            stringResource(strings.filter),
            isShowClear.value,
            {
                MsgFilters.clearFilters()
                filters.clear()
                filters.addAll(MsgFilters.filters)
                isRefreshing.value = true
                isShowClear.value = checkSize()
                onClose()
            },
            {
                onClose()
            }
        )
        LazyColumnWithScrollBars(
            modifierList = Modifier.fillMaxSize().padding(bottom = 60.dp, top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = dimens.smallPadding,
        ) {
            //expand
            item {
                ExpandableSection(
                    title = stringResource(strings.conversationsLabel),
                    isExpanded = isExpanded,
                    onExpandChange = { isExpanded = !isExpanded },
                    content = {
                        Column {
                            typeFilters.forEach { pair ->
                                RadioOptionRow(
                                    pair,
                                    selectedFilterKey.value
                                ){ isChecked, choice ->
                                    if (isChecked) {
                                        filters.find { it.key == choice }?.value = ""
                                        filters.find { it.key == choice }?.interpretation = null
                                        selectedFilterKey.value = null
                                    }else{
                                        typeFilters.forEach { filter->
                                            if (filter.first != choice) {
                                                filters.find { it.key == filter.first }?.value = ""
                                                filters.find { it.key == filter.first }?.interpretation = null
                                            }
                                        }
                                        filters.find { it.key == choice }?.value = "true"
                                        filters.find { it.key == choice }?.interpretation = typeFilters.find { it.first == choice }?.second
                                        selectedFilterKey.value = choice
                                    }
                                    isRefreshing.value = true
                                    isShowClear.value = checkSize()
                                }
                            }
                        }
                    }
                )
            }
            //user search
            item {
                Row(
                    modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
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
                                        interpretation = "$userLogin: $text"
                                    }
                                } else {
                                    filters.find { it.key == "interlocutor_id" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                userIdTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            isNumber = true,
                            modifier = Modifier.weight(1f).padding(dimens.smallPadding)
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
                                        interpretation = "$userLogin: $text"
                                    }
                                } else {
                                    filters.find { it.key == "interlocutor_login" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                userLoginTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            modifier = Modifier.weight(1f).padding(dimens.smallPadding)
                        )
                    }
                }
            }
            //id object
            item {
                Row(
                    modifier = Modifier.widthIn(min = 300.dp, max = 500.dp),
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
                                        interpretation = "$objectId: $text"
                                    }
                                } else {
                                    filters.find { it.key == "object_id" }.let {
                                        it?.value = ""
                                        it?.interpretation = null
                                    }
                                }
                                idObjectTextState.value = text
                                isRefreshing.value = true
                                isShowClear.value = checkSize()
                            },
                            isNumber = true,
                            modifier = Modifier.weight(1f).padding(dimens.smallPadding)
                        )
                    }
                }
            }
        }

        AcceptedPageButton(
            strings.actionAcceptFilters,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(dimens.mediumPadding)
        ){
            onClose()
        }
    }

}

