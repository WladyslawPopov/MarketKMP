package market.engine.fragments.createOffer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Card
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
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
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.checkboxs.DeliveryMethods
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.checkboxs.RadioGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.exceptions.DynamicPayloadContent
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.grids.PhotoDraggableGrid
import market.engine.widgets.textFields.DescriptionOfferTextField
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.SeparatorLabel
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
fun CreateOfferContent(
    component: CreateOfferComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOfferViewModel
    val offerId = model.value.offerId
    val type = model.value.createOfferType
    val createOfferResponse = viewModel.responseCreateOffer.collectAsState()
    val dynamicPayloadState = viewModel.responseDynamicPayload.collectAsState()
    val catHistory = viewModel.responseCatHistory.collectAsState()

    val images = viewModel.responseImages.collectAsState()
    val deleteImages = remember { mutableStateOf(arrayListOf<JsonPrimitive>()) }

    val focusManager = LocalFocusManager.current

    val isEditCat = remember { mutableStateOf(false) }
    val categoryName = remember { mutableStateOf("") }
    val categoryID = remember { mutableStateOf(model.value.catPath?.get(0) ?: 1L) }
    val parentID : MutableState<Long?> = remember { mutableStateOf(model.value.catPath?.get(0) ?: 1L) }
    val isLeaf = remember { mutableStateOf(true) }
    val isRefreshingFromFilters = remember { mutableStateOf(true) }
    val choiceCodeSaleType = remember { mutableStateOf<Int?>(null) }

    val catPath = remember { mutableStateOf(arrayListOf<Long>()) }
    val selectedDate = remember { mutableStateOf<String?>(null) }
    val richTextState = rememberRichTextState()
    val columnState = rememberLazyListState(
         initialFirstVisibleItemIndex = viewModel.positionList.value
    )

    val newOfferId = remember { mutableStateOf<Long?>(null) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (categoryID.value == 1L)
                BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )
    val url = remember { mutableStateOf("") }

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

           url.value = when (type) {
                CreateOfferType.CREATE -> "categories/${categoryID.value}/operations/create_offer"
                CreateOfferType.EDIT -> "offers/$offerId/operations/edit_offer"
                CreateOfferType.COPY -> "offers/$offerId/operations/copy_offer"
                CreateOfferType.COPY_WITHOUT_IMAGE -> "offers/$offerId/operations/copy_offer_without_old_photo"
                CreateOfferType.COPY_PROTOTYPE -> "offers/$offerId/operations/copy_offer_from_prototype"
           }
           viewModel.getPage(url.value)
        }
    }

    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(
            maxItems = MAX_IMAGE_COUNT
        ),
        initialDirectory = "market/temp/"
    ) { files ->

        viewModel.getImages(
            files?.map { file ->
                PhotoTemp(
                    file = file,
                    id = Uuid.random().toString()
                )
            } ?: emptyList()
        )
    }


    val isLoading = viewModel.isShowProgress.collectAsState()
    val error : (@Composable () -> Unit)? = null

    LaunchedEffect(dynamicPayloadState.value){
        if (dynamicPayloadState.value?.fields?.find { it.key == "description" }?.hasData == true ||
            dynamicPayloadState.value?.fields?.find { it.key == "description" }?.data != null){
            richTextState.setHtml(dynamicPayloadState.value?.fields?.find { it.key == "description"
            }?.data ?.jsonPrimitive?.content ?: "")
        }

        val tempPhotos: ArrayList<PhotoTemp> = arrayListOf()

        if (images.value.isEmpty()) {
            when (type) {
                CreateOfferType.EDIT, CreateOfferType.COPY -> {
                    val photos =
                        dynamicPayloadState.value?.fields?.filter { it.key?.contains("photo_") == true }
                            ?: emptyList()

                    photos.forEach { field ->
                        if (field.links != null) {
                            tempPhotos.add(
                                PhotoTemp(
                                    id = field.key,
                                    url = field.links.mid?.jsonPrimitive?.content
                                )
                            )
                        }
                    }

                    viewModel.setImages(tempPhotos.toList())
                }

                else -> {
                    if (model.value.externalImages != null) {
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
            }
        }
    }

    LaunchedEffect(viewModel.activeFiltersType){
        snapshotFlow{
            viewModel.activeFiltersType.value
        }.collectLatest { filter ->
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
        }.collectLatest { id ->
            if (!catPath.value.contains(id)) {
                catPath.value.add(id)
            }else{
                catPath.value.remove(id)
            }
        }
    }

    LaunchedEffect(richTextState){
        snapshotFlow{
            richTextState.annotatedString
        }.collectLatest { _ ->
            val text = KsoupEntities.decodeHtml(richTextState.toHtml())
            dynamicPayloadState.value?.fields?.find { it.key == "description" }?.data = JsonPrimitive(text)
        }
    }

    LaunchedEffect(columnState){
        snapshotFlow{
            columnState.firstVisibleItemIndex
        }.collectLatest { index ->
            viewModel.positionList.value = index
        }
    }

    LaunchedEffect(selectedDate){
        snapshotFlow{
            selectedDate.value
        }.collectLatest { date ->
            dynamicPayloadState.value?.fields?.find { it.key == "future_time" }?.data = JsonPrimitive(date)
        }
    }

    BaseContent(
        topBar = {
            CreateOfferAppBar(
                        type,
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
            if (createOfferResponse.value?.status == "operation_success") {

                newOfferId.value = createOfferResponse.value?.body?.jsonPrimitive?.longOrNull

                if (type == CreateOfferType.EDIT) {
                    component.onBackClicked()
                } else {
                    AnimatedVisibility(newOfferId.value != null){
                        //success offer
                        SuccessContent(
                            images.value,
                            dynamicPayloadState.value?.fields?.find { it.key == "title" }?.data?.jsonPrimitive?.content ?: "",
                            futureTime = selectedDate.value,
                            goToOffer = {
                                component.goToOffer(newOfferId.value!!)
                            },
                            addSimilarOffer = {
                                component.createNewOffer(offerId= newOfferId.value, type = CreateOfferType.COPY, catPath = catPath.value)
                            },
                            createNewOffer = {
                                component.createNewOffer(type=CreateOfferType.CREATE)
                            }
                        )
                    }
                }
            }else {
                AnimatedVisibility(
                    scaffoldState.bottomSheetState.isCollapsed && dynamicPayloadState.value != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        state = columnState,
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
                            val titleField =
                                dynamicPayloadState.value?.fields?.find { it.key == "title" }

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
                                    if (!getPermissionHandler().checkImagePermissions()) {
                                        getPermissionHandler().requestImagePermissions {
                                            if (it) {
                                                launcher.launch()
                                            }
                                        }
                                    } else {
                                        launcher.launch()
                                    }
                                }
                            }

                            AnimatedVisibility(
                                images.value.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                PhotoDraggableGrid(images.value, viewModel) {
                                    if (type == CreateOfferType.EDIT || type == CreateOfferType.COPY){
                                        if (it.url != null && it.id != null) {
                                            deleteImages.value.add(JsonPrimitive(it.id!!.last().toString()))
                                        }
                                    }
                                    val newList = images.value.toMutableList()
                                    newList.remove(it)
                                    viewModel.setImages(newList)
                                }
                            }
                        }

                        item {
                            val paramList =
                                dynamicPayloadState.value?.fields?.filter { it.key?.contains("par_") == true }
                                    ?: emptyList()

                            SeparatorLabel(stringResource(strings.parametersLabel))

                            DynamicPayloadContent(
                                paramList,
                                Modifier.fillMaxWidth()
                                    .padding(dimens.smallPadding)
                            )
                        }

                        val sortList = listOf(
                            "category_id",
                            "saletype",
                            "startingprice",
                            "buynowprice",
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

                        sortList.forEach { key ->
                            dynamicPayloadState.value?.fields?.find { it.key == key }
                                ?.let { field ->
                                    when (field.key) {
                                        "category_id" -> {
                                            field.data?.jsonPrimitive?.longOrNull?.let {
                                                categoryID.value = it
                                            }
                                        }

                                        "saletype" -> {
                                            item {
                                                SeparatorLabel(
                                                    stringResource(strings.saleTypeLabel)
                                                )

                                                DynamicSelect(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                ) { choice ->
                                                    choiceCodeSaleType.value = choice?.code?.int
                                                }
                                            }
                                        }

                                        "startingprice" ->{
                                            item {
                                                AnimatedVisibility(
                                                    choiceCodeSaleType.value == 0 || choiceCodeSaleType.value == 1,
                                                    enter = fadeIn(),
                                                    exit = fadeOut()
                                                ) {
                                                    DynamicInputField(
                                                        field,
                                                        Modifier.fillMaxWidth()
                                                            .padding(dimens.smallPadding),
                                                        sufix = stringResource(
                                                            strings.currencySign
                                                        ),
                                                        mandatory = true
                                                    )
                                                }
                                            }
                                        }

                                        "buynowprice" ->{
                                            item {
                                                AnimatedVisibility(
                                                    choiceCodeSaleType.value == 2 || choiceCodeSaleType.value == 1,
                                                    enter = fadeIn(),
                                                    exit = fadeOut()
                                                ) {
                                                    DynamicInputField(
                                                        field,
                                                        Modifier.fillMaxWidth()
                                                            .padding(dimens.smallPadding),
                                                        sufix = stringResource(
                                                            strings.currencySign
                                                        ),
                                                        mandatory = true
                                                    )
                                                }
                                            }
                                        }

                                        "priceproposaltype" -> {
                                            item {
                                                DynamicSelect(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                )
                                            }
                                        }

                                        "length_in_days" -> {
                                            item {
                                                DynamicSelect(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding),
                                                )
                                            }
                                        }

                                        "quantity" -> {
                                            when (choiceCodeSaleType.value) {
                                                1 -> {
                                                    field.data = JsonPrimitive(1)
                                                }
                                                //buynow
                                                2 -> {
                                                    item {
                                                        DynamicInputField(
                                                            field,
                                                            Modifier.fillMaxWidth()
                                                                .padding(dimens.smallPadding),
                                                            mandatory = true
                                                        )
                                                    }
                                                }

                                                0 -> {
                                                    field.data = JsonPrimitive(1)
                                                }
                                            }
                                        }

                                        "relisting_mode" -> {
                                            item {
                                                DynamicSelect(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                )
                                            }
                                        }

                                        "whopaysfordelivery" -> {
                                            item {
                                                SeparatorLabel(
                                                    stringResource(strings.paymentAndDeliveryLabel)
                                                )
                                                DynamicSelect(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                )
                                            }
                                        }

                                        "region" -> {
                                            item {
                                                DynamicSelect(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                )
                                            }
                                        }

                                        "freelocation" -> {
                                            item {
                                                DynamicInputField(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding),
                                                )
                                            }
                                        }

                                        "dealtype" -> {
                                            item {
                                                DynamicCheckboxGroup(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                )
                                            }
                                        }

                                        "paymentmethods" -> {
                                            item {
                                                DynamicCheckboxGroup(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
                                                )
                                            }
                                        }

                                        "deliverymethods" -> {
                                            item {
                                                SeparatorLabel(
                                                    stringResource(strings.deliveryMethodLabel)
                                                )

                                                DeliveryMethods(
                                                    field,
                                                    Modifier.fillMaxWidth()
                                                        .padding(dimens.smallPadding)
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
                                                DescriptionOfferTextField(field, richTextState)
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
                                val dataFields =
                                    dynamicPayloadState.value?.fields?.filter { it.data != null }

                                val jsonBody = buildJsonObject {
                                    dataFields?.forEach { data ->
                                        when (data.key) {
                                            "deliverymethods" -> {
                                                val valuesDelivery = arrayListOf<JsonObject>()
                                                data.data?.jsonArray?.forEach { choices ->
                                                    val deliveryPart = buildJsonObject {
                                                        put("code", JsonPrimitive(choices.jsonObject["code"]?.jsonPrimitive?.intOrNull))

                                                        data.choices?.find { it.code?.jsonPrimitive?.intOrNull ==
                                                                choices.jsonObject["code"]?.jsonPrimitive?.intOrNull
                                                        }?.extendedFields?.forEach { field ->
                                                            if (field.data != null) {
                                                                put(
                                                                    field.key.toString(),
                                                                    field.data!!.jsonPrimitive
                                                                )
                                                            }
                                                        }
                                                    }
                                                    valuesDelivery.add(deliveryPart)
                                                }
                                                put(data.key, JsonArray(valuesDelivery))
                                            }

                                            "category_id" -> {
                                                put(data.key, JsonPrimitive(categoryID.value))
                                            }
                                            "session_start" -> {
                                                if (selectedDate.value == null) {
                                                    put(data.key, data.data!!)
                                                }

                                            }
                                            "future_time" ->{
                                                if (selectedDate.value != null) {
                                                    put(data.key, JsonPrimitive(selectedDate.value))
                                                }
                                            }
                                            else -> {
                                                put(data.key ?: "", data.data!!)
                                            }
                                        }
                                    }

                                    when(type){
                                        CreateOfferType.EDIT, CreateOfferType.COPY ->{
                                            put("delete_images", JsonArray(deleteImages.value))
                                        }
                                        else -> {}
                                    }

                                    val positionArray = buildJsonArray {
                                        images.value.forEach { photo ->
                                            val listIndex = images.value.indexOf(photo) + 1
                                            if (photo.tempId != null && photo.url != null) {
                                                add(buildJsonObject {
                                                    put("key", JsonPrimitive(photo.id))
                                                    put("position", JsonPrimitive(listIndex))
                                                })
                                            }
                                        }
                                    }

                                    val tempImagesArray = buildJsonArray {
                                        images.value.forEach { photo ->
                                            val listIndex = images.value.indexOf(photo) + 1
                                            if (photo.tempId != null && photo.uri != null) {
                                                add(buildJsonObject {
                                                    put("id", JsonPrimitive(photo.tempId))
                                                    put("rotation", JsonPrimitive(photo.rotate))
                                                    put("position", JsonPrimitive(listIndex))
                                                })
                                            }
                                        }
                                    }

                                    if (tempImagesArray.isNotEmpty()) {
                                        put("temp_images", tempImagesArray)
                                    }

                                    if (positionArray.isNotEmpty()) {
                                        put("position_images", positionArray)
                                    }
                                }

                                viewModel.postPage(url.value, jsonBody)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSerializationApi::class)
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
            field.data?.jsonPrimitive?.intOrNull ?: 0
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
            field.data = JsonPrimitive(null)
        }
    }

    AnimatedVisibility(selectedFilterKey.value == 2,
        enter = fadeIn(),
        exit = fadeOut()
    ){
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if(selectedDate.value == null) {
                Text(
                    stringResource(strings.selectTimeActiveLabel),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.black
                )
            }else{
                Text(
                    selectedDate.value?.convertDateWithMinutes() ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor
                )
            }

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
                            if (selectedDate.value == null) {
                                val selectedDateMillis = datePickerState.selectedDateMillis
                                if (selectedDateMillis != null) {
                                    selectedDate.value = selectedDateMillis.toString()
                                }
                            } else {
                                val selectedDateMillis = selectedDate.value?.toLongOrNull() ?: 0L
                                val selectedHour = timePickerState.hour
                                val selectedMinute = timePickerState.minute

                                val localDateTime = Instant
                                    .fromEpochMilliseconds(selectedDateMillis)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                                    .atTime(selectedHour, selectedMinute)

                                selectedDate.value = localDateTime
                                    .toInstant(TimeZone.currentSystemDefault())
                                    .epochSeconds.toString()

                                showActivateOfferForFutureDialog.value = false
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

@Composable
fun SuccessContent(
    images: List<PhotoTemp>,
    title : String,
    futureTime : String? = null,
    goToOffer : () -> Unit,
    createNewOffer : () -> Unit,
    addSimilarOffer : () -> Unit,
){
    Column(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
            .padding(dimens.mediumPadding)
            .background(colors.white, MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val header = if (futureTime != null) {
            stringResource(strings.congratulationsCreateOfferInFutureLabel) + " ${futureTime.convertDateWithMinutes()}"
        } else{
            stringResource(strings.congratulationsLabel)
        }

        SeparatorLabel(header)

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.mediumPadding).clickable {
                    goToOffer()
                }.clip(MaterialTheme.shapes.medium)
            ,
            verticalAlignment = Alignment.Top,
        ) {
            Card(
                shape = MaterialTheme.shapes.medium
            ) {
                LoadImage(
                    images.firstOrNull()?.uri ?: images.firstOrNull()?.url ?: "",
                    size = 100.dp,
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(Modifier.width(dimens.mediumSpacer))

            TitleText(
                title,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // add similar
            AcceptedPageButton(
                strings.createSimilarOfferLabel,
                Modifier.weight(1f)
                    .padding(dimens.smallPadding),
                containerColor = colors.brightGreen
            ) {
                addSimilarOffer()
            }
            // create New
            AcceptedPageButton(
                strings.createNewOfferTitle,
                Modifier.weight(1f)
                    .padding(dimens.smallPadding)
            ) {
                createNewOffer()
            }
        }
    }
}
