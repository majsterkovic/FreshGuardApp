package put.poznan.freshguard.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.foundation.layout.offset

import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import put.poznan.freshguard.db.fridge.FridgeItem
import put.poznan.freshguard.db.products.ProductItem
import put.poznan.freshguard.db.products.ProductViewModel
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun ProductItem(productViewModel: ProductViewModel, fridgeItem: FridgeItem, onDeleteProduct: (FridgeItem) -> Unit) {

    //val showDialog = remember { mutableStateOf(false) }

    var product = remember { mutableStateOf(ProductItem(-1, "", null, null))}

    val expirationDate = fridgeItem.expirationDate
    val approximateDate = fridgeItem.approximateDate
    val category = fridgeItem.category
    val quantityTextColor = if (category == "Jedzenie" && approximateDate == 0 && expirationDate.before(Date())) Color.White else Color.Black
    val borderColor = if (category == "Jedzenie") {
            if(approximateDate == 0) {
                when {
                    expirationDate.before(Date()) -> Color.Black
                    expirationDate.before(dateAfterDays(3)) -> Color(0xFFE82D27)
                    expirationDate.before(dateAfterDays(7)) -> Color(0xFFFFE946)
                    else -> Color(0xFF218F4F)
                }
            } else {
                when {
                    expirationDate.before(Date()) -> Color(0xFFEA5B5A)
                    expirationDate.before(dateAfterDays(10)) -> Color(0xFFFFF37C)
                    else -> Color(0xFFACF59B)
                }
            }
        } else {
            if(approximateDate == 0) {
                when {
                    expirationDate.before(Date()) -> Color.Black
                    expirationDate.before(dateAfterDays(15)) -> Color(0xFFE82D27)
                    expirationDate.before(dateAfterDays(30)) -> Color(0xFFFFE946)
                    else -> Color(0xFF218F4F)
                }
            } else {
                when {
                    expirationDate.before(Date()) -> Color(0xFFEA5B5A)
                    expirationDate.before(dateAfterDays(30)) -> Color(0xFFFFF37C)
                    else -> Color(0xFFACF59B)
                }
        }
    }


    val today = Date()
    val diff = expirationDate.time - today.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    LaunchedEffect(fridgeItem.productId) {
        product.value = productViewModel.getProductById(fridgeItem.productId)!!
    }

    if (product.value.id != -1) {
        var productName = product.value.name
        var fontsize = 12
        if (productName.length > 24) {
            val split = productName.split(" ")
            productName = split[0] + " " + split[1]
        }
        if (productName.length > 14) {
            fontsize = 10
        }
        if (productName.length > 12) {
            fontsize = 11
        }

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            //showDialog.value = true
                            onDeleteProduct(fridgeItem)

                        }
                    )
                }
        ) {
            BadgedBox(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).size(110.dp).background(Color.Gray).border(
                    width = 2.dp, color = borderColor, shape = RoundedCornerShape(10.dp)),
                badge = { Badge (
                                    modifier = Modifier.offset(y=(12).dp).offset(x = (-20).dp),
                                    backgroundColor = borderColor
                                ) { Text(text = "x${fridgeItem.quantity}  ", color = quantityTextColor) } },
            ) {
                Text(
                    text = if (abs(days).toInt() == 1) "$days dzień" else "$days dni",
                    color = borderColor,
                    modifier = Modifier.align(Alignment.TopCenter).padding(vertical = 4.dp),
                    fontSize = 12.sp
                )

                // Wyświetlanie obrazka
                product.value.imageBytes?.let { imageBytes ->
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Image(
                        bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Text(
                    text = "$productName",
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(vertical = 4.dp),
                    fontSize = fontsize.sp,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

        }
    }

}

fun dateAfterDays(days: Int): Date {
    val currentDate = Date()
    val calendar = Calendar.getInstance()
    calendar.time = currentDate
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}