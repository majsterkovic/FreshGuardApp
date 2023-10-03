package put.poznan.freshguard.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import put.poznan.freshguard.db.fridge.FridgeItem
import put.poznan.freshguard.db.fridge.FridgeViewModel
import put.poznan.freshguard.db.products.ProductViewModel
import put.poznan.freshguard.db.shopping.ShoppingItem
import put.poznan.freshguard.db.shopping.ShoppingViewModel
import put.poznan.freshguard.db.users.UserSession


@Composable
fun ProductList(fridgeViewModel: FridgeViewModel, productViewModel: ProductViewModel, category: String, shoppingViewModel: ShoppingViewModel, userId: Int, userSession: UserSession) {

    val productList: List<FridgeItem> by fridgeViewModel.readAllData.observeAsState(emptyList())
    val products = productList.filter { it.category == category }
    val showDialog = remember { mutableStateOf(false) }
    val productToDelete = remember { mutableStateOf<FridgeItem?>(null) }
    val isLoggedIn = remember { mutableStateOf(userSession.isLoggedIn)}

    //TODO: pokaz wiadomość, jeśli ktoś chce dodać do lsity zakupów coś, co już istnieje
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text(
                text = "Usuwanie",
                color = MaterialTheme.colorScheme.primary
            ) },
            text = { Text(
                text = if (isLoggedIn.value) "Czy chcesz dodać usuwany produkt do listy zakupów?" else "Nie zalogowano, czy chcesz usunąć produkt?",
                color = MaterialTheme.colorScheme.onSurface)},
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        if (isLoggedIn.value) {
                            //quickfix, można zmienic pożniej na cos mniej głupiego
                            if(productList.isNotEmpty()) {
                                val shopItem = ShoppingItem(
                                    productId = productToDelete.value!!.productId,
                                    userId = userId,
                                    checked = false,
                                    quantity = productToDelete.value!!.quantity
                                )
                                shoppingViewModel.insertShoppingItem(shopItem)
                            }
                        }
                        fridgeViewModel.deleteProduct(productToDelete.value!!)

                    },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(text = "Tak",
                        color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        if(isLoggedIn.value) {
                            fridgeViewModel.deleteProduct(productToDelete.value!!)
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ),
                ) {
                    Text(
                        text = "Nie",
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Column {
        Text(
            text = "Wybierz $category",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            itemsIndexed(products) { _, product ->
                ProductItem(
                    productViewModel = productViewModel,
                    fridgeItem = product,
                    onDeleteProduct = {
                        productToDelete.value = it
                        showDialog.value = true
                    }
                )
            }
        }
    }
}




