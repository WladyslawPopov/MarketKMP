package market.engine.fragments.root.main.createOffer

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
import androidx.compose.material3.Card
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.checkboxs.DeliveryMethods
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.checkboxs.RadioGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.widgets.exceptions.LoadImage
import market.engine.fragments.base.onError
import market.engine.widgets.dialogs.DateDialog
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.grids.PhotoDraggableGrid
import market.engine.widgets.textFields.DescriptionOfferTextField
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.SeparatorLabel
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateOfferContent(
    component: CreateOfferComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOfferViewModel
    val offerId = model.value.offerId
    val type = model.value.createOfferType

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val createOfferResponse = viewModel.responseCreateOffer.collectAsState()
    val dynamicPayloadState = viewModel.responseDynamicPayload.collectAsState()
    val catHistory = viewModel.responseCatHistory.collectAsState()
    val images = viewModel.responseImages.collectAsState()
    val deleteImages = remember { mutableStateOf(arrayListOf<JsonPrimitive>()) }

    val focusManager = LocalFocusManager.current

    val isEditCat = remember {viewModel.isEditCat }
    val categoryName = remember { viewModel.categoryName }
    val categoryID = remember { viewModel.categoryID }
    val parentID : MutableState<Long?> = remember { viewModel.parentID }
    val isLeaf = remember { viewModel.isLeaf }
    val isRefreshingFromFilters = remember { viewModel.isRefreshingFromFilters }
    val choiceCodeSaleType = remember { viewModel.choiceCodeSaleType }


    val futureTime = remember { viewModel.futureTime }
    val selectedDate = remember { viewModel.selectedDate }

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

    BackHandler(model.value.backHandler){
        if (viewModel.activeFiltersType.value == "") {
            component.onBackClicked()
        }else{
            viewModel.viewModelScope.launch {
                val newCat =
                    viewModel.onCatBack(parentID.value ?: 1L)
                if (newCat != null) {
                    categoryID.value = newCat.id
                    categoryName.value = newCat.name ?: ""
                    parentID.value = newCat.parentId
                    isLeaf.value = newCat.isLeaf
                    val sd = SD(
                        searchCategoryID = categoryID.value,
                        searchCategoryName = categoryName.value,
                        searchParentID = parentID.value,
                        searchIsLeaf = isLeaf.value
                    )
                    viewModel.getCategories(
                        sd,
                        LD(),
                        true
                    )
                }else{
                    component.onBackClicked()
                }
            }
        }
    }

    val refresh = {
        viewModel.onError(ServerErrorException())
        if(categoryID.value != 1L) {
            viewModel.getCategoriesHistory(categoryID.value)
            if (isEditCat.value) {
                // update params
                viewModel.updateParams(categoryID.value)
                isEditCat.value = false
            } else {
                val url = when (type) {
                    CreateOfferType.CREATE -> "categories/${categoryID.value}/operations/create_offer"
                    CreateOfferType.EDIT -> "offers/$offerId/operations/edit_offer"
                    CreateOfferType.COPY -> "offers/$offerId/operations/copy_offer"
                    CreateOfferType.COPY_WITHOUT_IMAGE -> "offers/$offerId/operations/copy_offer_without_old_photo"
                    CreateOfferType.COPY_PROTOTYPE -> "offers/$offerId/operations/copy_offer_from_prototype"
                }
                if (type != CreateOfferType.CREATE) {
                    viewModel.updateParams(categoryID.value)
                }
                viewModel.getPage(url)
            }
        }
    }

    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(
            maxItems = MAX_IMAGE_COUNT
        ),
        initialDirectory = "market/temp/"
    ) { files ->
        files?.let { viewModel.getImages(it) }
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        {
            onError(err) {
                refresh()
            }
            viewModel.onError(ServerErrorException())
        }
    } else {
        null
    }

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

        futureTime.value = dynamicPayloadState.value?.fields?.find { it.key == "future_time" }
    }

    LaunchedEffect(viewModel.activeFiltersType){
        snapshotFlow{
            viewModel.activeFiltersType.value
        }.collect { filter ->
            if (filter == "") {
                scaffoldState.bottomSheetState.collapse()
            } else {
                scaffoldState.bottomSheetState.expand()
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
                        refresh()
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

                val title = dynamicPayloadState.value?.fields?.find { it.key == "title" }?.data?.jsonPrimitive?.content ?: ""
                val loc = dynamicPayloadState.value?.fields?.find { it.key == "location" }?.data?.jsonPrimitive?.content ?: ""

                val eventParams = mapOf(
                    "lot_id" to offerId,
                    "lot_name" to title,
                    "lot_city" to loc,
                    "lot_category" to "$categoryID",
                    "seller_id" to UserData.userInfo?.id
                )

                if (type != CreateOfferType.EDIT) {
                    viewModel.analyticsHelper.reportEvent("added_offer_success", eventParams)
                } else {
                    viewModel.analyticsHelper.reportEvent("edit_offer_success", eventParams)
                }

                if (type == CreateOfferType.EDIT) {
                    viewModel.viewModelScope.launch {
                        delay(2000L)
                        component.onBackClicked()
                    }
                } else {
                    AnimatedVisibility(newOfferId.value != null){
                        //success offer
                        SuccessContent(
                            images.value,
                            title,
                            dynamicPayloadState.value?.fields?.find { it.key == "session_start" }?.data?.jsonPrimitive?.intOrNull != 1,
                            futureTime = selectedDate.value ?: getCurrentDate().toLong(),
                            goToOffer = {
                                component.goToOffer(newOfferId.value!!)
                            },
                            addSimilarOffer = {
                                component.createNewOffer(offerId= newOfferId.value, type = CreateOfferType.COPY)
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
                        //categories
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(dimens.mediumPadding),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (type != CreateOfferType.COPY_PROTOTYPE) {
                                    if (catHistory.value.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.Start,
                                            verticalArrangement = Arrangement.SpaceAround,
                                            modifier = Modifier.weight(3f)
                                        ) {
                                            catHistory.value.reversed()
                                                .forEachIndexed { index, cat ->
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

                            SetUpDynamicFields(
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
                                                if (categoryID.value == 1L) {
                                                    categoryID.value = it
                                                }
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
                                                        suffix = stringResource(
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
                                                        suffix = stringResource(
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

                                               if (selectedDate.value != null && field.data == null){
                                                   field.data = JsonPrimitive(2)
                                                   selectedDate.value = futureTime.value?.data?.jsonPrimitive?.longOrNull
                                               }else{
                                                   field.data = JsonPrimitive(0)
                                               }

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
                        //create btn
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
                                if (dataFields != null) {
                                    val jsonBody = createJsonBody(
                                        dataFields,
                                        categoryID.value,
                                        selectedDate.value,
                                        deleteImages.value,
                                        images.value,
                                        type
                                    )

                                    val url = when (type) {
                                        CreateOfferType.CREATE -> {
                                            "categories/${categoryID.value}/operations/create_offer"
                                        }
                                        CreateOfferType.EDIT -> {
                                            "offers/$offerId/operations/edit_offer"
                                        }
                                        CreateOfferType.COPY -> {
                                            "offers/$offerId/operations/copy_offer"
                                        }
                                        CreateOfferType.COPY_WITHOUT_IMAGE -> {
                                            "offers/$offerId/operations/copy_offer_without_old_photo"
                                        }
                                        CreateOfferType.COPY_PROTOTYPE -> {
                                            "offers/$offerId/operations/copy_offer_from_prototype"
                                        }
                                    }

                                    viewModel.postPage(url, jsonBody)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalSerializationApi::class)
@Composable
fun SessionStartContent(
    selectedDate : MutableState<Long?>,
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
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(dimens.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                modifier = Modifier.fillMaxWidth(),
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
                        selectedDate.value.toString().convertDateWithMinutes(),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.titleTextColor
                    )
                }

                ActionButton(
                    strings.actionChangeLabel,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    alignment = Alignment.TopEnd,
                ){
                    showActivateOfferForFutureDialog.value = true
                }
            }

            DateDialog(
                showActivateOfferForFutureDialog.value,
                onDismiss = {
                    showActivateOfferForFutureDialog.value = false
                },
                onSucceed = {
                    selectedDate.value = it
                    showActivateOfferForFutureDialog.value = false
                }
            )
        }
    }
}


fun createJsonBody(
    fields: List<Fields>,
    categoryID: Long,
    selectedDate: Long?,
    deleteImages: List<JsonPrimitive>,
    images: List<PhotoTemp>,
    type: CreateOfferType,
) : JsonObject {
    return buildJsonObject {
        fields.forEach { data ->
            when (data.key) {
                "deliverymethods" -> {
                    val valuesDelivery = arrayListOf<JsonObject>()
                    data.data?.jsonArray?.forEach { choices ->
                        val deliveryPart = buildJsonObject {
                            put(
                                "code",
                                JsonPrimitive(choices.jsonObject["code"]?.jsonPrimitive?.intOrNull)
                            )

                            data.choices?.find {
                                it.code?.jsonPrimitive?.intOrNull ==
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
                    put(data.key, JsonPrimitive(categoryID))
                }

                "session_start" -> {
                    if (selectedDate == null) {
                        put(data.key, data.data!!)
                    }

                }
                "future_time" ->{
                    if (selectedDate != null) {
                        put(data.key, data.data!!)
                    }
                }

                else -> {
                    put(data.key ?: "", data.data!!)
                }
            }
        }

        when (type) {
            CreateOfferType.EDIT, CreateOfferType.COPY -> {
                put("delete_images", JsonArray(deleteImages))
            }

            else -> {}
        }

        val positionArray = buildJsonArray {
            images.forEach { photo ->
                val listIndex = images.indexOf(photo) + 1
                if (photo.tempId == null) {
                    add(buildJsonObject {
                        put("key", JsonPrimitive(photo.id))
                        put("position", JsonPrimitive(listIndex))
                    })
                }
            }
        }

        val tempImagesArray = buildJsonArray {
            images.forEach { photo ->
                val listIndex = images.indexOf(photo) + 1
                if (photo.tempId != null) {
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
}

@Composable
fun SuccessContent(
    images: List<PhotoTemp>,
    title : String,
    isActive : Boolean = true,
    futureTime : Long?,
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

        val header = buildAnnotatedString {
            if (isActive) {
                append(stringResource(strings.congratulationsCreateOfferInFutureLabel))
                 withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.yellowSun)) {
                     append(" ${futureTime.toString().convertDateWithMinutes()}")
                 }
            } else{
                append(
                    stringResource(strings.congratulationsLabel)
                )
            }
        }

        SeparatorLabel("", annotatedString = header )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(dimens.mediumPadding).clickable {
                    goToOffer()
                }.clip(MaterialTheme.shapes.medium)
            ,
            verticalAlignment = Alignment.Top,
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.white),
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



