package put.poznan.freshguard.ui

import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import put.poznan.freshguard.db.products.ProductViewModel
import put.poznan.freshguard.db.shopping.ShoppingItem
import put.poznan.freshguard.db.shopping.ShoppingViewModel
import put.poznan.freshguard.db.users.UserDao
import put.poznan.freshguard.db.users.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopList(shopListViewModel: ShoppingViewModel, userSession: UserSession, userDao: UserDao, productViewModel: ProductViewModel) {
    var shopList by remember { mutableStateOf<List<ShoppingItem>>(emptyList()) }
    val shopListLive = shopListViewModel.allShoppingItems.observeAsState(shopList)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userSession.username) {
        val user = withContext(Dispatchers.IO) {
            userDao.getUserByUsername(userSession.username)
        }
        user?.let {
            val userId = user.id
            val items = withContext(Dispatchers.IO) {
                shopListViewModel.getShoppingItemsByUserId(userId)
            }
            shopList = items
        }
    }

    val context = LocalContext.current

    TopAppBar(
        title = {Text("Lista zakupów")},
        actions = {
            IconButton(onClick = {
                val textToSend = StringBuilder()
                textToSend.append("Lista zakupów:\n")
                coroutineScope.launch {
                    for ((i, item) in shopListLive.value.withIndex()) {
                        val product = productViewModel.getProductById(item.productId)
                        val itemText = "${i+1}. ${product!!.name}: ${item.quantity}\n"
                        textToSend.append(itemText)
                    }
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textToSend.toString())
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, "Prześlij listę składników")
                    context.startActivity(shareIntent)

                }


            }) {
                Icon(Icons.Filled.Share, contentDescription = "")
            }
        }
    )

    LazyColumn {

        items(shopListLive.value) { product ->
            ShopListItem(
                shopProduct = product,
                productViewModel = productViewModel,
                shoppingViewModel = shopListViewModel,
                onDeleteProduct = {
                    shopListViewModel.deleteShoppingItem(product)
                }
            )
        }
    }

}

