package put.poznan.freshguard.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import put.poznan.freshguard.db.products.ProductItem
import put.poznan.freshguard.db.products.ProductViewModel
import put.poznan.freshguard.db.shopping.ShoppingItem
import put.poznan.freshguard.db.shopping.ShoppingViewModel


@Composable
fun ShopListItem(shopProduct: ShoppingItem, productViewModel: ProductViewModel, shoppingViewModel: ShoppingViewModel, onDeleteProduct: (ShoppingItem) -> Unit) {

    var product = remember { mutableStateOf(put.poznan.freshguard.db.products.ProductItem(-1, "", null, null)) }


    LaunchedEffect(shopProduct) {
        product.value = productViewModel!!.getProductById(shopProduct.productId)!!
    }

    if (product.value.id != -1) {
        ListItem(shopProduct, product.value, shoppingViewModel, onDeleteProduct)
    }

}



//TODO: dodawanie produktu, którego nie było w lodówce
//TODO: usuwanie wszystkich zaznaczonych produktów z listy

@Composable
fun ListItem(shopProduct: ShoppingItem, product: ProductItem, shoppingViewModel: ShoppingViewModel, onDeleteProduct: (ShoppingItem) -> Unit) {
    val quantity = remember { mutableStateOf(shopProduct.quantity) }
    val checked = remember { mutableStateOf(shopProduct.checked)}

    Row(modifier = Modifier.height(72.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.fillMaxWidth(0.5f),
            verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked.value,
                onCheckedChange = { newValue ->
                        checked.value = newValue
                    },
                )

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(64.dp),
                onClick = {
                    if (quantity.value > 0) { quantity.value-- }
                    shopProduct.quantity = quantity.value
                    shoppingViewModel.updateShoppingItem(shopProduct)
                }
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "")
            }
            Text("${quantity.value}")

            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(64.dp),
                onClick = {
                    quantity.value++
                    shopProduct.quantity = quantity.value
                    shoppingViewModel.updateShoppingItem(shopProduct)
                }
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "")
            }
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(64.dp),
                onClick = { onDeleteProduct(shopProduct) }
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "")
            }

        }
    }
}



