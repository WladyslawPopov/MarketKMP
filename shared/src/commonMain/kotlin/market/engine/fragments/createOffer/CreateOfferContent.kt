package market.engine.fragments.createOffer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.common.getPermissionHandler
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateOnlyYear
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.checkboxs.RadioGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.exceptions.DynamicPayloadContent
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.grids.PhotoDraggableGrid
import market.engine.widgets.rows.RichTextStyleRow
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferContent(
    component: CreateOfferComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOfferViewModel
    val offerId = model.value.offerId
    val type = model.value.type
    val getPage = viewModel.responseGetPage.collectAsState()
    val postPage = viewModel.responsePostPage.collectAsState()
    val catHistory = viewModel.responseCatHistory.collectAsState()

    val images = viewModel.responseImages.collectAsState()

    val focusManager = LocalFocusManager.current

    val isEditCat = remember { mutableStateOf(false) }
    val categoryName = remember { mutableStateOf("") }
    val categoryID = remember { mutableStateOf(model.value.catPath?.get(0) ?: 1L) }
    val parentID : MutableState<Long?> = remember { mutableStateOf(model.value.catPath?.get(0) ?: 1L) }
    val isLeaf = remember { mutableStateOf(true) }
    val isRefreshingFromFilters = remember { mutableStateOf(true) }

    val catPath = remember { mutableStateOf(arrayListOf<Long>()) }

    val selectedDate = remember { mutableStateOf<String?>(null) }

    val richTextState = rememberRichTextState()


    val refresh = {
        if (isEditCat.value){
            // update params
            viewModel.updateParams(categoryID.value)
            isEditCat.value = false
        }else {
            if (catPath.value.isEmpty()){
                viewModel.getCategoriesHistory(model.value.catPath ?: emptyList())
            }else{
                viewModel.getCategoriesHistory(catPath.value)
            }


            when (type) {
                CreateOfferType.CREATE -> viewModel.getPage("categories/${categoryID.value}/operations/create_offer")
                CreateOfferType.EDIT -> viewModel.getPage("offers/$offerId/operations/edit_offer")
                CreateOfferType.COPY -> viewModel.getPage("offers/$offerId/operations/copy_offer")
                CreateOfferType.COPY_WITHOUT_IMAGE -> viewModel.getPage("offers/$offerId/operations/copy_offer_without_old_photo")
                CreateOfferType.COPY_PROTOTYPE -> viewModel.getPage("offers/$offerId/operations/copy_offer_from_prototype")
            }
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (categoryID.value == 1L)
                BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )

    LaunchedEffect(scaffoldState.bottomSheetState.isCollapsed){
        if (scaffoldState.bottomSheetState.isCollapsed){
           refresh()
        }
    }

    LaunchedEffect(viewModel.activeFiltersType){
        snapshotFlow{
            viewModel.activeFiltersType.value
        }.collect { filter ->
            if (filter == "") {
                scaffoldState.bottomSheetState.collapse()
                refresh()
            } else {
                scaffoldState.bottomSheetState.expand()
                catPath.value.removeLast()
            }
        }
    }

    LaunchedEffect(categoryID){
        snapshotFlow{
            categoryID.value
        }.collect { id ->
            if (!catPath.value.contains(id)) {
                catPath.value.add(id)
            }else{
                catPath.value.remove(id)
            }
        }
    }

    LaunchedEffect(selectedDate){
        snapshotFlow{
            selectedDate.value
        }.collect { date ->
            getPage.value?.fields?.find { it.key == "session_start" }?.data = JsonPrimitive(date)
        }
    }

    val isLoading = viewModel.isShowProgress.collectAsState()
    val error : (@Composable () -> Unit)? = null

    BaseContent(
        topBar = {
            CreateOfferAppBar(
                        type,
                getPage.value?.description,
                onBackClick = {
                    component.onBackClicked()
                }
            )
        },
        onRefresh = {
            refresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            sheetContentColor = colors.primaryColor,
            sheetBackgroundColor = colors.primaryColor,
            contentColor = colors.primaryColor,
            backgroundColor = colors.primaryColor,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = false,
            sheetContent = {
                val searchData = SD(
                    searchCategoryID = categoryID.value,
                    searchCategoryName = categoryName.value,
                    searchParentID = parentID.value,
                    searchIsLeaf = isLeaf.value
                )
                val listingData = LD()

                CategoryContent(
                    baseViewModel = viewModel,
                    complete = {
                        viewModel.activeFiltersType.value = ""
                    },
                    isCreateOffer = true,
                    searchData = searchData,
                    listingData = listingData,
                    searchCategoryId = categoryID,
                    searchCategoryName = categoryName,
                    searchParentID = parentID,
                    searchIsLeaf = isLeaf,
                    isRefreshingFromFilters = isRefreshingFromFilters,
                )
            },
        ) {
            AnimatedVisibility(scaffoldState.bottomSheetState.isCollapsed && getPage.value != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.mediumPadding),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (catHistory.value.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier.weight(3f)
                                ) {
                                    catHistory.value.reversed().forEachIndexed { index, cat ->
                                        Text(
                                            text = if (catHistory.value.size - 1 == index)
                                                cat.name ?: ""
                                            else (cat.name ?: "") + "->",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (catHistory.value.size - 1 == index) colors.black else colors.steelBlue,
                                            modifier = Modifier.padding(dimens.extraSmallPadding)
                                        )
                                    }
                                }
                            }
                            if (type != CreateOfferType.EDIT) {
                                ActionButton(
                                    strings.changeCategory,
                                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                    alignment = Alignment.TopEnd,
                                    modifier = Modifier.weight(2.7f)
                                ) {
                                    isEditCat.value = true
                                    viewModel.activeFiltersType.value = "categories"
                                    isRefreshingFromFilters.value = true
                                }
                            }
                        }
                    }

                    item {
                        val titleField = getPage.value?.fields?.find { it.key == "title" }

                        if (titleField != null) {
                            SeparatorLabel(titleField.shortDescription ?: "")

                            DynamicInputField(
                                titleField,
                                Modifier.fillMaxWidth().padding(dimens.smallPadding)
                            )
                        }
                    }

                    // Images
                    item {
                        SeparatorLabel(stringResource(strings.photoLabel))

                        val photos = getPage.value?.fields?.filter { it.key?.contains("photo_") == true } ?: emptyList()
                        val tempPhotos : ArrayList<PhotoTemp> = arrayListOf()
                        photos.forEach { field ->
                            if (field.links != null) {
                                tempPhotos.add(
                                    PhotoTemp(
                                        url = field.links.mid?.jsonPrimitive?.content
                                    )
                                )
                            }
                        }
                        if (tempPhotos.isNotEmpty()) {
                            viewModel.setImages(tempPhotos.toList())
                        }else{
                            if (model.value.externalImages != null){
                                model.value.externalImages?.forEach {
                                    tempPhotos.add(
                                        PhotoTemp(
                                            url = it
                                        )
                                    )
                                }
                                viewModel.setImages(tempPhotos.toList())
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.mediumPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(strings.actionAddPhoto),
                                    color = colors.black,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(dimens.smallPadding)
                                )

                                Icon(
                                    painterResource(drawables.addGalleryIcon),
                                    contentDescription = stringResource(strings.actionAddPhoto),
                                    tint = colors.black
                                )
                            }


                            ActionButton(
                                strings.chooseAction,
                                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                alignment = Alignment.TopEnd,
                                enabled = images.value.size < MAX_IMAGE_COUNT
                            ) {
                                if (!getPermissionHandler().checkImagePermissions()){
                                    getPermissionHandler().requestImagePermissions {
                                        if (it) {
                                            viewModel.getImages()
                                        }
                                    }
                                }else{
                                    viewModel.getImages()
                                }
                            }
                        }

                        AnimatedVisibility(images.value.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ){
                            PhotoDraggableGrid(images.value, viewModel){
                                val newList = images.value.toMutableList()
                                newList.remove(it)
                                viewModel.setImages(newList)
                            }
                        }
                    }

                    item {
                        val paramList = getPage.value?.fields?.filter { it.key?.contains("par_") == true } ?: emptyList()

                        SeparatorLabel(stringResource(strings.parametersLabel))

                        DynamicPayloadContent(
                            paramList,
                            Modifier.fillMaxWidth()
                                .padding(dimens.smallPadding)
                        )
                    }

                    val desiredOrder = listOf(
                        "category_id",
                        "saletype",
                        "priceproposaltype",
                        "length_in_days",
                        "quantity",
                        "relisting_mode",
                        "whopaysfordelivery",
                        "region",
                        "freelocation",
                        "dealtype",
                        "paymentmethods",
                        "deliverymethods",
                        "session_start",
                        "description",
                    )

                    desiredOrder.forEach { key ->
                        getPage.value?.fields?.find { it.key == key }?.let { field ->
                            when (field.key) {
                                "category_id" -> {
                                    field.data?.jsonPrimitive?.longOrNull?.let {

                                    }
                                }
                                "saletype" -> {
                                    item {
                                        SeparatorLabel(
                                            stringResource(strings.saleTypeLabel)
                                        )

                                        val choiceCode = remember { mutableStateOf<Int?>(null) }
                                        DynamicSelect(field, Modifier.fillMaxWidth().padding(dimens.smallPadding)){ choice ->
                                            choiceCode.value = choice?.code?.int
                                        }

                                        AnimatedVisibility(choiceCode.value != null,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            Column {
                                                when (choiceCode.value) {
                                                    1 -> {
                                                        getPage.value?.fields?.find { it.key == "buynowprice" }
                                                            ?.let {
                                                                DynamicInputField(
                                                                    it,
                                                                    Modifier.fillMaxWidth()
                                                                        .padding(dimens.smallPadding),
                                                                    sufix = stringResource(
                                                                            strings.currencySign),
                                                                    mandatory = true
                                                                )
                                                            }
                                                        getPage.value?.fields?.find { it.key == "startingprice" }
                                                            ?.let {
                                                                DynamicInputField(
                                                                    it,
                                                                    Modifier.fillMaxWidth()
                                                                        .padding(dimens.smallPadding),
                                                                    sufix = stringResource(
                                                                        strings.currencySign),
                                                                    mandatory = true
                                                                )
                                                            }
                                                    }

                                                    2 -> {
                                                        getPage.value?.fields?.find { it.key == "buynowprice" }
                                                            ?.let {
                                                                DynamicInputField(
                                                                    it,
                                                                    Modifier.fillMaxWidth()
                                                                        .padding(dimens.smallPadding),
                                                                    sufix = stringResource(
                                                                        strings.currencySign),
                                                                    mandatory = true
                                                                )
                                                            }
                                                    }

                                                    0 -> {
                                                        getPage.value?.fields?.find { it.key == "startingprice" }
                                                            ?.let {
                                                                DynamicInputField(
                                                                    it,
                                                                    Modifier.fillMaxWidth()
                                                                        .padding(dimens.smallPadding),
                                                                    sufix = stringResource(
                                                                        strings.currencySign),
                                                                    mandatory = true
                                                                )
                                                            }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                "priceproposaltype" ->{
                                    item {
                                        DynamicSelect(field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "length_in_days" ->{
                                    item {
                                        DynamicSelect(
                                            field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                        )
                                    }
                                }
                                "quantity" -> {
                                    item {
                                        DynamicInputField(
                                            field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                            mandatory = true
                                        )
                                    }
                                }
                                "relisting_mode" ->{
                                    item {
                                        DynamicSelect(field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "whopaysfordelivery"->{
                                    item {
                                        SeparatorLabel(
                                            stringResource(strings.paymentAndDeliveryLabel)
                                        )
                                        DynamicSelect(field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "region" ->{
                                    item {
                                        DynamicSelect(field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "freelocation" ->{
                                    item {
                                        DynamicInputField(
                                            field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                        )
                                    }
                                }
                                "dealtype" ->{
                                    item {
                                        DynamicCheckboxGroup(field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "paymentmethods" ->{
                                    item {
                                        DynamicCheckboxGroup(field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "deliverymethods" ->{
                                    item {
                                        SeparatorLabel(
                                            stringResource(strings.deliveryMethodLabel)
                                        )

                                        DynamicCheckboxGroup(
                                            field,
                                            Modifier.fillMaxWidth().padding(dimens.smallPadding)
                                        )
                                    }
                                }
                                "session_start" -> {
                                    item {
                                        SeparatorLabel(
                                            stringResource(strings.offersGroupStartTSTile)
                                        )
                                        SessionStartContent(selectedDate, field)
                                    }
                                }
                                "description" -> {
                                    item {

                                        SeparatorLabel(
                                            stringResource(strings.description)
                                        )


                                        if (field.hasData || field.data != null){
                                            richTextState.setHtml(field.data?.jsonPrimitive?.content ?: "")
                                        }

                                        RichTextStyleRow(
                                            modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.Center),
                                            state = richTextState,
                                        )

                                        BasicRichTextEditor(
                                            state = richTextState,
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .background(colors.white, MaterialTheme.shapes.medium)
                                                .fillMaxWidth()
                                                .heightIn(min= 300.dp, max = 500.dp).padding(dimens.smallPadding),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        val label = when (type) {
                            CreateOfferType.EDIT -> strings.actionSaveLabel
                            else -> strings.sellOfferLabel
                        }
                        AcceptedPageButton(
                            text = label,
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.mediumPadding),
                        ) {
                            getPage.value?.fields?.find { it.key == "description" }?.data = JsonPrimitive(richTextState.toHtml())
                            val i = getPage.value?.fields?.filter { it.data != null }
                            val j = i
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionStartContent(
    selectedDate : MutableState<String?>,
    field: Fields,
){
    val saleTypeFilters = listOf(
        0 to stringResource(strings.offerStartNowLabel),
        1 to stringResource(strings.offerStartInactiveLabel),
        2 to stringResource(strings.offerStartInFutureLabel)
    )

    val selectedFilterKey = remember {
        mutableStateOf(
            field.data?.jsonPrimitive?.int ?: 0
        )
    }

    val showActivateOfferForFutureDialog = remember { mutableStateOf(false) }

    RadioGroup(
        saleTypeFilters,
        selectedFilterKey.value
    ){ isChecked, choice ->
        if(!isChecked) {
            selectedFilterKey.value = choice
            field.data = JsonPrimitive(choice)
        }else{
            selectedFilterKey.value = 0
            field.data = JsonPrimitive(0)
        }
    }

    AnimatedVisibility(selectedFilterKey.value == 2,
        enter = fadeIn(),
        exit = fadeOut()
    ){
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.mediumPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(strings.selectTimeActiveLabel),
                style = MaterialTheme.typography.bodyMedium
            )

            ActionButton(
                strings.actionChangeLabel,
                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                alignment = Alignment.TopEnd,
            ){
                showActivateOfferForFutureDialog.value = true
            }
        }

        AnimatedVisibility(showActivateOfferForFutureDialog.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val currentDate = getCurrentDate()

            val year = currentDate.convertDateOnlyYear().toInt()

            val selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis > currentDate.toLong() * 1000
                }
            }
            val oneDayInMillis = 24 * 60 * 60 * 1000
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = currentDate.toLong() * 1000 + oneDayInMillis,
                yearRange = year..(year + 100),
                selectableDates = selectableDates
            )

            val timePickerState = rememberTimePickerState(
                is24Hour = true
            )

            DatePickerDialog(
                colors = DatePickerDefaults.colors(
                    containerColor = colors.white
                ),
                tonalElevation = 0.dp,
                properties = DialogProperties(usePlatformDefaultWidth = true),
                onDismissRequest = {
                    showActivateOfferForFutureDialog.value = false
                },
                confirmButton = {
                    SimpleTextButton(
                        text = stringResource(strings.acceptAction),
                        backgroundColor = colors.inactiveBottomNavIconColor,
                        onClick = {
                            val selectedDateMillis = datePickerState.selectedDateMillis
                            if (selectedDateMillis != null) {
                                selectedDate.value = (selectedDateMillis/1000L).toString()
                            }
                        },
                        modifier = Modifier.padding(dimens.smallPadding),
                    )
                },
                dismissButton = {
                    SimpleTextButton(
                        text = stringResource(strings.closeWindow),
                        backgroundColor = colors.grayLayout,
                        onClick = {
                            showActivateOfferForFutureDialog.value = false
                        },
                        modifier = Modifier.padding(dimens.smallPadding),
                    )
                }
            ) {
                if (selectedDate.value == null) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        colors = DatePickerDefaults.colors(
                            containerColor = colors.white,
                        ),
                        modifier = Modifier.padding(dimens.smallPadding)
                            .clip(MaterialTheme.shapes.medium)
                    )
                } else {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            containerColor = colors.white,
                        ),
                        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                            .clip(MaterialTheme.shapes.medium),
                        layoutType = TimePickerLayoutType.Vertical
                    )
                }
            }
        }
    }
}
