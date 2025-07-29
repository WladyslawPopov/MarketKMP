package market.engine.fragments.root.main.createOffer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.getPermissionHandler
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.processInput
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.checkboxs.RadioOptionRow
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.checkboxs.ThemeCheckBox
import market.engine.widgets.dialogs.DateDialog
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.grids.PhotoDraggableGrid
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.DescriptionTextField
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.ErrorText
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun CreateOfferContent(
    component: CreateOfferComponent
) {
    val model = component.model.subscribeAsState()
    val viewModel = model.value.createOfferViewModel
    val type = model.value.createOfferType

    val isLoading by viewModel.isShowProgress.collectAsState()
    val err by viewModel.errorMessage.collectAsState()

    val uiState by viewModel.createOfferContentState.collectAsState()

    val newOfferId by viewModel.newOfferId.collectAsState()

    val categorySD by uiState.categoryState.categoryViewModel.searchData.collectAsState()
    val categoryID = categorySD.searchCategoryID

    val isEditCategory = uiState.categoryState.openCategory

    val selectedDate by viewModel.selectedDate.collectAsState()
    val payloadState by viewModel.responseGetPage.collectAsState()

    val categoryState = uiState.categoryState
    val appBarState = uiState.appBarState

    val catHistory = uiState.catHistory

    val first = uiState.firstDynamicContent
    val second = uiState.secondDynamicContent
    val third = uiState.thirdDynamicContent
    val end = uiState.endDynamicContent

    val photoTempViewModel = viewModel.photoTempViewModel

    val images by photoTempViewModel.responseImages.collectAsState()

    val choiceCodeSaleType = remember(payloadState) {
        payloadState.find { it.key == "saletype" }?.data?.jsonPrimitive?.intOrNull
    }

    val focusManager = LocalFocusManager.current

    val toastItem by viewModel.toastItem.collectAsState()

    val goToUp = remember { mutableStateOf(false) }

   val columnState = rememberLazyScrollState(viewModel)

    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(
            maxItems = MAX_IMAGE_COUNT
        ),
        initialDirectory = "market/temp/"
    ) { files ->
        files?.let { photoTempViewModel.getImages(it) }
    }

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage.isNotBlank()) {
            {
                OnError(err) {
                    viewModel.refreshPage()
                }
            }
        } else {
            null
        }
    }

    LaunchedEffect(goToUp.value){
        if (goToUp.value){
            delay(300)
            columnState.scrollState.scrollToItem(0)
            goToUp.value = false
        }
    }

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = appBarState
            ) {
                val title = when(type){
                    CreateOfferType.EDIT -> stringResource(strings.editOfferLabel)
                    else -> {
                        stringResource(strings.createNewOfferTitle)
                    }
                }

                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        onRefresh = {
            viewModel.refreshPage()
        },
        error = error,
        noFound = null,
        isLoading = isLoading,
        toastItem = toastItem,
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        when{
            categoryID == 1L || isEditCategory ->{
                CategoryContent(
                    categoryState.categoryViewModel,
                    onClose = {
                        viewModel.closeCategory()
                    },
                    onCompleted = {
                        viewModel.closeCategory()
                        viewModel.refreshPage()
                    },
                    modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                )
            }

            !isEditCategory && payloadState.isNotEmpty() && newOfferId == null ->{
                LazyColumnWithScrollBars(
                    modifierList = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    state = columnState.scrollState,
                    contentPadding = contentPadding,
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                )
                {
                    //categories
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            if (type != CreateOfferType.COPY_PROTOTYPE) {
                                if (catHistory.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.Start,
                                        verticalArrangement = Arrangement.SpaceAround,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        catHistory.reversed()
                                            .forEachIndexed { index, cat ->
                                                Text(
                                                    text = if (catHistory.size - 1 == index)
                                                        cat.name ?: ""
                                                    else (cat.name ?: "") + "->",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = if (catHistory.size - 1 == index) colors.black else colors.steelBlue,
                                                    modifier = Modifier.padding(
                                                        dimens.extraSmallPadding
                                                    )
                                                )
                                            }
                                    }
                                }

                                ActionButton(
                                    stringResource(strings.changeCategory),
                                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                    alignment = Alignment.TopEnd,
                                ) {
                                    viewModel.openCategory()
                                }
                            }
                        }
                    }

                    item {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 2 else 1),
                            modifier = Modifier
                                .heightIn(200.dp, 5000.dp)
                                .wrapContentHeight(),
                            userScrollEnabled = false,
                            horizontalArrangement = Arrangement.spacedBy(
                                dimens.smallPadding,
                                Alignment.CenterHorizontally
                            ),
                            verticalItemSpacing = dimens.smallPadding,
                            content = {
                                first.forEach { key ->
                                    when (key) {
                                        "title" -> {
                                            payloadState.find { it.key == key }
                                                ?.let { field ->
                                                    item {
                                                        Column {
                                                            SeparatorLabel(
                                                                field.shortDescription
                                                                    ?: ""
                                                            )

                                                            DynamicInputField(
                                                                field
                                                            ){
                                                                viewModel.setNewFiles(it)
                                                            }
                                                        }
                                                    }
                                                }
                                        }
                                        "saletype" -> {
                                            item {
                                                Column {
                                                    payloadState.find { it.key == key }
                                                        ?.let { field ->
                                                            Column {
                                                                SeparatorLabel(
                                                                    stringResource(strings.saleTypeLabel)
                                                                )

                                                                DynamicSelect(
                                                                    field,
                                                                ) {
                                                                    viewModel.setNewFiles(it)
                                                                }
                                                            }
                                                        }


                                                    payloadState.find { it.key == "startingprice" }
                                                        ?.let { field ->
                                                            AnimatedVisibility(
                                                                choiceCodeSaleType == 0 || choiceCodeSaleType == 1,
                                                                enter = fadeIn(),
                                                                exit = fadeOut()
                                                            ) {
                                                                DynamicInputField(
                                                                    field,
                                                                    suffix = stringResource(
                                                                        strings.currencySign
                                                                    ),
                                                                    mandatory = true
                                                                ){
                                                                    viewModel.setNewFiles(it)
                                                                }
                                                            }
                                                        }


                                                    payloadState.find { it.key == "buynowprice" }
                                                        ?.let { field ->
                                                            AnimatedVisibility(
                                                                choiceCodeSaleType == 2 || choiceCodeSaleType == 1,
                                                                enter = fadeIn(),
                                                                exit = fadeOut()
                                                            ) {
                                                                DynamicInputField(
                                                                    field,
                                                                    suffix = stringResource(
                                                                        strings.currencySign
                                                                    ),
                                                                    mandatory = true
                                                                ){
                                                                    viewModel.setNewFiles(it)
                                                                }
                                                            }
                                                        }

                                                    payloadState.find { it.key == "priceproposaltype" }
                                                        ?.let { field ->
                                                            DynamicSelect(
                                                                field,
                                                            ){
                                                                viewModel.setNewFiles(it)
                                                            }
                                                        }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    second.forEach { key ->
                        when (key) {
                            "params" -> {
                                item {
                                    val paramList = payloadState.filter {
                                        it.key?.contains(
                                            "par_"
                                        ) == true
                                    }

                                    SeparatorLabel(stringResource(strings.parametersLabel))

                                    SetUpDynamicFields(
                                        paramList,
                                        modifier = Modifier.fillMaxWidth(if(isBigScreen.value) 0.5f else 1f)
                                    ){
                                        viewModel.setNewFiles(it)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(if (isBigScreen.value) 2 else 1),
                            modifier = Modifier
                                .heightIn(200.dp, 5000.dp)
                                .wrapContentHeight(),
                            userScrollEnabled = false,
                            verticalItemSpacing = dimens.smallPadding,
                            horizontalArrangement = Arrangement.spacedBy(
                                dimens.smallPadding,
                                Alignment.CenterHorizontally
                            ),
                            content = {
                                third.forEach { key ->
                                    payloadState.find { it.key == key }
                                        ?.let { field ->
                                            when (field.key) {
                                                "length_in_days" -> {
                                                    item {
                                                        DynamicSelect(
                                                            field,
                                                        ){
                                                            viewModel.setNewFiles(it)
                                                        }
                                                    }
                                                }

                                                "quantity" -> {
                                                    when (choiceCodeSaleType) {
                                                        1 -> {
                                                            field.data = JsonPrimitive(1)
                                                        }
                                                        //buynow
                                                        2 -> {
                                                            item {
                                                                DynamicInputField(
                                                                    field,
                                                                    mandatory = true
                                                                ){
                                                                    viewModel.setNewFiles(it)
                                                                }
                                                            }
                                                        }

                                                        0 -> {
                                                            field.data = JsonPrimitive(1)
                                                        }

                                                        else -> {}
                                                    }
                                                }

                                                "relisting_mode" -> {
                                                    item {
                                                        DynamicSelect(
                                                            field,
                                                        ){
                                                            viewModel.setNewFiles(it)
                                                        }
                                                    }
                                                }

                                                "whopaysfordelivery" -> {
                                                    item {
                                                        Column {
                                                            SeparatorLabel(
                                                                stringResource(strings.paymentAndDeliveryLabel)
                                                            )
                                                            DynamicSelect(
                                                                field,
                                                            ){
                                                                viewModel.setNewFiles(it)
                                                            }

                                                            payloadState.find { it.key == "region" }
                                                                ?.let { field ->
                                                                    DynamicSelect(
                                                                        field,
                                                                    ){
                                                                        viewModel.setNewFiles(it)
                                                                    }
                                                                }

                                                            payloadState.find { it.key == "freelocation" }
                                                                ?.let { field ->
                                                                    DynamicInputField(
                                                                        field,
                                                                    ){
                                                                        viewModel.setNewFiles(it)
                                                                    }
                                                                }
                                                        }
                                                    }
                                                }

                                                "paymentmethods" -> {
                                                    item {
                                                        DynamicCheckboxGroup(
                                                            field,
                                                        ){
                                                            viewModel.setNewFiles(it)
                                                        }
                                                    }
                                                }

                                                "dealtype" -> {
                                                    item {
                                                        DynamicCheckboxGroup(
                                                            field,
                                                        ){
                                                            viewModel.setNewFiles(it)
                                                        }
                                                    }
                                                }

                                                "deliverymethods" -> {
                                                    item {
                                                        Column {
                                                            SeparatorLabel(
                                                                stringResource(strings.deliveryMethodLabel)
                                                            )

                                                            DeliveryMethods(
                                                                field,
                                                            ){
                                                                viewModel.setNewFiles(it)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        )
                    }

                    end.forEach { key ->
                        if (key == "images") {
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
                                            contentDescription = stringResource(
                                                strings.actionAddPhoto
                                            ),
                                            tint = colors.black
                                        )
                                    }


                                    ActionButton(
                                        stringResource(strings.chooseAction),
                                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                                        alignment = Alignment.TopEnd,
                                        enabled = images.size < MAX_IMAGE_COUNT
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
                                    images.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    PhotoDraggableGrid(images, photoTempViewModel)
                                }
                            }
                        }
                        payloadState.find { it.key == key }
                            ?.let { field ->
                                when (field.key) {
                                    "session_start" -> {
                                        item {
                                            if (type != CreateOfferType.EDIT) {
                                                SeparatorLabel(
                                                    stringResource(strings.offersGroupStartTSTile)
                                                )

                                                SessionStartContent(selectedDate, field, onValueChange = {
                                                    viewModel.setNewFiles(it)
                                                    viewModel.setSelectData()
                                                }){
                                                    viewModel.setSelectData(it)
                                                }
                                            }
                                        }
                                    }

                                    "description" -> {
                                        item {
                                            SeparatorLabel(
                                                stringResource(strings.description)
                                            )

                                            DescriptionTextField(field) { description ->
                                                viewModel.setDescription(description)
                                            }
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
                            text = stringResource(label),
                            modifier = Modifier.fillMaxWidth()
                                .padding(dimens.mediumPadding),
                            enabled = !isLoading
                        ) {
                            viewModel.postPage()
                        }
                    }
                }
            }
            newOfferId != null -> {
                val title = remember {
                    payloadState.find { it.key == "title" }?.data?.jsonPrimitive?.content
                        ?: ""
                }
                //success offer
                SuccessContent(
                    images,
                    title,
                    payloadState.find { it.key == "session_start" }?.data?.jsonPrimitive?.intOrNull != 1,
                    modifier = Modifier.padding(contentPadding)
                        .padding(dimens.mediumPadding),
                    futureTime = selectedDate ?: getCurrentDate().toLong(),
                    goToOffer = {
                        component.goToOffer(newOfferId!!)
                    },
                    addSimilarOffer = {
                        component.createNewOffer(offerId= newOfferId, type = CreateOfferType.COPY)
                    },
                    createNewOffer = {
                        component.createNewOffer(type=CreateOfferType.CREATE)
                    }
                )
            }
        }
    }
}


@Composable
fun SessionStartContent(
    selectedDate : Long?,
    field: Fields,
    modifier: Modifier = Modifier,
    onValueChange : (Fields) -> Unit,
    onSetSelectedDate : (Long) -> Unit
){
    val saleTypeFilters = listOf(
        0 to stringResource(strings.offerStartNowLabel),
        1 to stringResource(strings.offerStartInactiveLabel),
        2 to stringResource(strings.offerStartInFutureLabel)
    )

    val selectedFilterKey = remember(field.data) { mutableStateOf(field.data?.jsonPrimitive?.intOrNull ?: 0) }

    val showActivateOfferForFutureDialog = remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(dimens.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        saleTypeFilters.forEach {
            RadioOptionRow(
                it,
                selectedFilterKey.value,
                rbColor = colors.inactiveBottomNavIconColor
            ) { isChecked, choice ->
                onValueChange(
                    field.copy(
                        data = if(!isChecked) {
                            JsonPrimitive(choice)
                        }else{
                            null
                        }
                    )
                )
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
                if(selectedDate == null) {
                    Text(
                        stringResource(strings.selectTimeActiveLabel),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.black
                    )
                }else{
                    Text(
                        selectedDate.toString().convertDateWithMinutes(),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.titleTextColor
                    )
                }

                ActionButton(
                    stringResource(strings.actionChangeLabel),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    alignment = Alignment.TopEnd,
                ){
                    showActivateOfferForFutureDialog.value = true
                }
            }

            DateDialog(
                showActivateOfferForFutureDialog.value,
                isSelectableDates = true,
                onDismiss = {
                    showActivateOfferForFutureDialog.value = false
                },
                onSucceed = {
                    onSetSelectedDate(it)
                    showActivateOfferForFutureDialog.value = false
                }
            )
        }
    }
}

@Composable
fun DeliveryMethods(
    field: Fields,
    modifier: Modifier = Modifier,
    onValueChange: (Fields) -> Unit
) {
    val isMandatory = remember(field.validators) {
        field.validators?.any { it.type == "mandatory" } == true
    }

    val initialSelected = remember(field) {
        buildList {
            field.data?.jsonArray?.forEach { item ->
                item.jsonObject["code"]?.jsonPrimitive?.intOrNull?.let {
                    add(it)
                }
            }
        }
    }

    val error = remember(field.errors) { processInput(field.errors) }

    val onClickListener : (Int) -> Unit = remember(initialSelected) {
        { choiceCode ->
            val currentSet = initialSelected.toMutableList()
            if (currentSet.contains(choiceCode)) {
                currentSet.remove(choiceCode)
            } else {
                currentSet.add(choiceCode)
            }

            onValueChange(field.copy(
                data = buildJsonArray {
                    currentSet.forEach {
                        add(JsonObject(mapOf("code" to JsonPrimitive(it))))
                    }
                }
            ))
        }
    }

    Column(modifier = modifier) {

        DynamicLabel(
            text = field.longDescription ?: field.shortDescription.orEmpty(),
            isMandatory = isMandatory,
            modifier = Modifier.padding(dimens.smallPadding)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            field.choices?.forEach { choice ->
                val choiceCode = choice.code?.intOrNull ?: 0

                // A single checkbox row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClickListener(choiceCode)
                        }
                ) {
                    ThemeCheckBox(
                        isSelected = initialSelected.contains(choiceCode),
                        onSelectionChange = {
                            onClickListener(choiceCode)
                        },
                        modifier = Modifier
                    )

                    Text(
                        text = choice.name.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black,
                        modifier = Modifier.padding(start = dimens.smallPadding)
                    )
                }

                AnimatedVisibility(initialSelected.contains(choiceCode) && choice.extendedFields != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(dimens.mediumPadding),
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        horizontalAlignment = Alignment.Start
                    ){
                        choice.extendedFields?.forEach { extendField ->
                            when (extendField.key) {
                                "delivery_price_city" -> {
                                    if (extendField.data == null) {
                                        extendField.data = field.data?.jsonArray?.find {
                                            it.jsonObject["code"]?.jsonPrimitive?.intOrNull ==
                                                    choice.code?.intOrNull
                                        }?.jsonObject?.get("delivery_price_city")?.jsonPrimitive
                                    }

                                    DynamicInputField(
                                        extendField,
                                        suffix = stringResource(strings.currencyCode),
                                        label = stringResource(strings.deliveryCityParameterLabel),
                                    ){ newField ->
                                        onValueChange( field.copy(
                                            choices = field.choices?.map { choice ->
                                                if (choice.extendedFields?.contains(extendField) == true){
                                                    choice.copy(
                                                        extendedFields = choice.extendedFields.map {
                                                            if (it.key == extendField.key){
                                                                newField.copy()
                                                            }else{
                                                                it.copy()
                                                            }
                                                        }
                                                    )
                                                }else{
                                                    choice.copy()
                                                }
                                            }
                                        ))
                                    }
                                }
                                "delivery_price_country" -> {
                                    if (extendField.data == null) {
                                        extendField.data = field.data?.jsonArray?.find {
                                            it.jsonObject["code"]?.jsonPrimitive?.intOrNull ==
                                                    choice.code?.intOrNull
                                        }?.jsonObject?.get("delivery_price_country")?.jsonPrimitive
                                    }

                                    DynamicInputField(
                                        extendField,
                                        suffix = stringResource(strings.currencyCode),
                                        label = stringResource(strings.deliveryCountryParameterLabel),
                                    ){ newField ->
                                        onValueChange( field.copy(
                                            choices = field.choices?.map { choice ->
                                                if (choice.extendedFields?.contains(extendField) == true){
                                                    choice.copy(
                                                        extendedFields = choice.extendedFields.map {
                                                            if (it.key == extendField.key){
                                                                newField.copy()
                                                            }else{
                                                                it.copy()
                                                            }
                                                        }
                                                    )
                                                }else{
                                                    choice.copy()
                                                }
                                            }
                                        ))
                                    }
                                }
                                "delivery_price_world" -> {
                                    if (extendField.data == null) {
                                        extendField.data = field.data?.jsonArray?.find {
                                            it.jsonObject["code"]?.jsonPrimitive?.intOrNull ==
                                                    choice.code?.intOrNull
                                        }?.jsonObject?.get("delivery_price_world")?.jsonPrimitive
                                    }

                                    DynamicInputField(
                                        extendField,
                                        suffix = stringResource(strings.currencyCode),
                                        label = stringResource(strings.deliveryWorldParameterLabel),
                                    ){ newField ->
                                        onValueChange( field.copy(
                                            choices = field.choices?.map { choice ->
                                                if (choice.extendedFields?.contains(extendField) == true){
                                                    choice.copy(
                                                        extendedFields = choice.extendedFields.map {
                                                            if (it.key == extendField.key){
                                                                newField.copy()
                                                            }else{
                                                                it.copy()
                                                            }
                                                        }
                                                    )
                                                }else{
                                                    choice.copy()
                                                }
                                            }
                                        ))
                                    }
                                }
                                "delivery_comment" -> {
                                    if (extendField.data == null) {
                                        extendField.data = field.data?.jsonArray?.find {
                                            it.jsonObject["code"]?.jsonPrimitive?.intOrNull ==
                                                    choice.code?.intOrNull
                                        }?.jsonObject?.get("delivery_comment")?.jsonPrimitive
                                    }

                                    DynamicInputField(
                                        extendField,
                                        label = stringResource(strings.commentLabel),
                                        singleLine = false,
                                    ){ newField ->
                                        onValueChange( field.copy(
                                            choices = field.choices?.map { choice ->
                                                if (choice.extendedFields?.contains(extendField) == true){
                                                    choice.copy(
                                                        extendedFields = choice.extendedFields.map {
                                                            if (it.key == extendField.key){
                                                                newField.copy()
                                                            }else{
                                                                it.copy()
                                                            }
                                                        }
                                                    )
                                                }else{
                                                    choice.copy()
                                                }
                                            }
                                        ))
                                    }
                                }
                                else -> {
                                    DynamicInputField(
                                        extendField,
                                    ){ newField ->
                                        onValueChange( field.copy(
                                            choices = field.choices?.map { choice ->
                                                if (choice.extendedFields?.contains(extendField) == true){
                                                    choice.copy(
                                                        extendedFields = choice.extendedFields.map {
                                                            if (it.key == extendField.key){
                                                                newField.copy()
                                                            }else{
                                                                it.copy()
                                                            }
                                                        }
                                                    )
                                                }else{
                                                    choice.copy()
                                                }
                                            }
                                        ))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (error != null) {
            ErrorText(text = error, modifier = Modifier.padding(dimens.smallPadding))
        }
    }
}
