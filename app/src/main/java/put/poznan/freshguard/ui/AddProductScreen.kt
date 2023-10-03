package put.poznan.freshguard.ui


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import put.poznan.freshguard.R
import put.poznan.freshguard.Routes
import put.poznan.freshguard.db.fridge.FridgeItem
import put.poznan.freshguard.db.fridge.FridgeViewModel
import put.poznan.freshguard.db.products.ProductItem
import put.poznan.freshguard.db.products.ProductViewModel
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, productViewModel: ProductViewModel, fridgeViewModel: FridgeViewModel, category: String) {

    var newFood by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf(category) }
    var newQuantity by remember { mutableStateOf("1") }
    var newExpirationDate by remember { mutableStateOf("") }
    var newBarcode by remember { mutableStateOf("") }
    var newImage : ByteArray by remember { mutableStateOf(byteArrayOf()) }
    var scanned by remember { mutableStateOf(false)}
    var isChecked by remember { mutableStateOf(false)}
    var isPutDateVisible by remember { mutableStateOf(false)}
    var isPutNameVisible by remember {mutableStateOf(false)}
    var isDataBeingDownloaded by remember { mutableStateOf(false)}
    var showDownloadErrorMessage by remember { mutableStateOf(false)}

    var openDialog by remember { mutableStateOf(false) }

    val categories = listOf("Jedzenie", "Kosmetyki")

    val imagesFood = listOf(
        ImageBitmap.imageResource(R.drawable.pizza),
        ImageBitmap.imageResource(R.drawable.vegetable),
        ImageBitmap.imageResource(R.drawable.mushroom),
        ImageBitmap.imageResource(R.drawable.milk),
        ImageBitmap.imageResource(R.drawable.fruit),
        ImageBitmap.imageResource(R.drawable.meat)
    )

    val imagesCosmetics = listOf(
        ImageBitmap.imageResource(R.drawable.cream),
        ImageBitmap.imageResource(R.drawable.lotion),
        ImageBitmap.imageResource(R.drawable.vegan_cream),
        ImageBitmap.imageResource(R.drawable.soap),
        ImageBitmap.imageResource(R.drawable.hand_sanitizer),
    )

    val images = if (newCategory == "Jedzenie") imagesFood else imagesCosmetics

    val imagesByteArray = images.map { imageBitmap ->
        val bitmap = imageBitmap.asAndroidBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        byteArrayOutputStream.toByteArray()
    }

    var imagesByteArrayMut by remember { mutableStateOf(imagesByteArray)}

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

    LaunchedEffect(scanned) {
        if (scanned) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    isDataBeingDownloaded = true
                    showDownloadErrorMessage = false
                    val websiteUrl = "https://www.barcodelookup.com/$newBarcode"
                    val downloadedContent = downloadWebsite(websiteUrl)
                    if (newFood == "") {
                        newFood = downloadedContent.first
                    }
                    if(downloadedContent.second != "") {
                        val bitmap = downloadPicture(downloadedContent.second)
                        bitmap?.let {
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            imagesByteArrayMut = listOf(byteArray) + imagesByteArrayMut
                        }
                    }
                    isDataBeingDownloaded = false
                    if(downloadedContent.first == "") {
                        showDownloadErrorMessage = true
                    }
                    scanned = false
                }
            }
        }
    }

    if(showDownloadErrorMessage) {
        showToast(message = "Nie udało się pobrać danych")
    }

    //widok ładowania pokazujący się przy pobieraniu danych o kodzie z internetu
    if (isDataBeingDownloaded) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Content of your screen here
                // ...

                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Text(
                    text = "Pobieranie danych",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }



    //TODO: chceck if barcode exists in database and fill details
    BottomSheetScaffold(
        sheetContent = {
            CameraScreen { newCode ->
                newBarcode = newCode
                scanned = true
                coroutineScope.launch {
                    scaffoldState.bottomSheetState.hide()
                }
            }
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
    )  {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Dodaj nowy produkt", style = MaterialTheme.typography.h5)

            // Nazwa
            OutlinedTextField(
                value = newFood,
                onValueChange = { newFood = it },
                label = { Text("Nazwa produktu") },
                modifier = Modifier.fillMaxWidth()
            )
            if (isPutNameVisible) {
                Text(text = "Podaj nazwę produktu", color = colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(4.dp))
            }


            // Kategoria
            Column {
                Text(
                    text = "Kategoria",
                    modifier = Modifier.padding(top = 16.dp)
                )
                categories.forEach { category ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadioButton(
                            selected = newCategory == category,
                            onClick = { newCategory = category },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = category,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Ilość
            Text(
                text = newQuantity,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Slider(
                value = newQuantity.toFloatOrNull() ?: 1f,
                onValueChange = { newValue ->
                    newQuantity = newValue.toInt().toString()
                },
                valueRange = 1f..9f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )

            // Data ważności
            OutlinedTextField(
                value = newExpirationDate,
                enabled = false,
                onValueChange = { newExpirationDate = it },
                label = { Text("Data ważności") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDialog = true }
            )
            if (isPutDateVisible) {
                Text(text = "Podaj datę wazności", color = colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(4.dp))
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        isChecked = it
                        Log.e("checked", "$isChecked")
                      },
                )
                Text(text = "Data orientacyjna")
            }

            if (openDialog) {
                val datePickerState = rememberDatePickerState()
                val confirmEnabled =
                    remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
                DatePickerDialog(
                    onDismissRequest = {
                        openDialog = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDialog = false
                                newExpirationDate = formatDate(datePickerState.selectedDateMillis)

                            },
                            enabled = confirmEnabled.value
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                openDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Kod kreskowy
            OutlinedTextField(
                value = newBarcode,
                onValueChange = { newBarcode = it },
                label = { Text("Kod kreskowy") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            scaffoldState.bottomSheetState.hide()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Skanuj kod kreskowy")
            }

            // Wybór obrazka
            ImageRow(
                images = imagesByteArrayMut,
                onImageSelected = { newImage = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wyślij
            Button(
                onClick = {
                    isPutDateVisible = false
                    isPutNameVisible = false
                    if(newFood == "") {
                        isPutNameVisible = true
                        return@Button
                    }
                    if (newExpirationDate == "") {
                        isPutDateVisible = true
                        return@Button
                    }

                    if (newImage.isEmpty()) {
                        newImage = imagesByteArrayMut[0]
                    }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    val expirationDate: Date? = dateFormat.parse(newExpirationDate)

                    coroutineScope.launch {
                        val databaseBarcode = productViewModel.getProductByBarcode(newBarcode)
                        var newProductId = 0
                        if (databaseBarcode.isNotEmpty() && newBarcode != "") {
                            newProductId = databaseBarcode[0].id
                        } else {
                            newProductId = newFood.hashCode()
                            val product = ProductItem(
                                id = newProductId,
                                name = newFood,
                                barcode = newBarcode,
                                imageBytes = newImage
                            )
                            runBlocking(Dispatchers.IO) {
                                productViewModel.addProduct(product)
                            }
                            // Dodanie produktu do bazy danych za pomocą ProductViewModel
                        }

                        val fridgeProduct = FridgeItem(
                            productId = newProductId,
                            expirationDate = expirationDate!!,
                            quantity = newQuantity.toInt(),
                            category = newCategory,
                            approximateDate = if (isChecked) 1 else 0
                        )
                        fridgeViewModel.addProduct(fridgeProduct)
                    }

                    navController.navigate(Routes.Home)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj")
            }
        }
    }
}

private fun formatDate(dateMillis: Long?): String {
    if (dateMillis == null) {
        return ""
    }

    val instant = Instant.ofEpochMilli(dateMillis)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(localDate)
}

private fun downloadWebsite(urlString: String): Pair<String,String> {
    val url = URL(urlString)
    val html = url.readText()
    var result = ""
    var img = ""
    val document = Jsoup.parse(html)
    val pageTitle = document.title()
    Log.e("html", "$pageTitle")

    if (pageTitle.startsWith("EAN")) {
        val resultElement = document.selectFirst("meta[name=description]")
        resultElement?.let {
            val name = resultElement.attr("content")
            Log.e("html", "$name")
            result= name.split(" - ")[1]
        }
        if (result != "") {
            val imgElement = document.selectFirst("input[name=images]")
            imgElement?.let {img = imgElement.attr("value")}
        }
    }


    return Pair(result, img)
}

private fun downloadPicture(string: String): Bitmap? {
    val url = URL(string)
    val connection: HttpURLConnection?
    try {
        connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val inputStream: InputStream = connection.inputStream
        val bufferedInputStream = BufferedInputStream(inputStream)
        return BitmapFactory.decodeStream(bufferedInputStream)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

@Composable
fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        coroutineScope {
            launch(Dispatchers.Main) {
                Toast.makeText(context, message, duration).show()
            }
        }
    }
}