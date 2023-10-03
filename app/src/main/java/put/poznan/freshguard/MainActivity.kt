package put.poznan.freshguard

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Snackbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import put.poznan.freshguard.db.AppDatabase
import put.poznan.freshguard.db.fridge.FridgeViewModel
import put.poznan.freshguard.db.products.ProductItem
import put.poznan.freshguard.db.products.ProductViewModel
import put.poznan.freshguard.db.shopping.ShoppingItem
import put.poznan.freshguard.db.shopping.ShoppingViewModel
import put.poznan.freshguard.db.users.UserDao
import put.poznan.freshguard.db.users.UserSession
import put.poznan.freshguard.notifications.NotificationViewModel
import put.poznan.freshguard.ui.AddProductScreen
import put.poznan.freshguard.ui.LoginScreen
import put.poznan.freshguard.ui.ProductList
import put.poznan.freshguard.ui.ProfileScreen
import put.poznan.freshguard.ui.RegisterLoginScreen
import put.poznan.freshguard.ui.RegistrationScreen
import put.poznan.freshguard.ui.ShopList
import put.poznan.freshguard.ui.theme.MyApplicationTheme
import put.poznan.freshguard.ui.theme.getOnPrimaryColor
import put.poznan.freshguard.ui.theme.getPrimaryColor
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDatabase = AppDatabase.getDatabase(applicationContext)

        val productViewModel = ProductViewModel(application = application)
        val fridgeViewModel = FridgeViewModel(application = application)
        val shopListViewModel = ShoppingViewModel(application = application)

        val userSession = mutableStateOf(UserSession())
        val userDao = appDatabase.userDao()

        val notificationViewModel = NotificationViewModel(application = application, fridgeViewModel = fridgeViewModel, productViewModel = productViewModel)
        notificationViewModel.checkAndSetDefaultReminder()

        setContent {
            val navController = rememberNavController()
            MyApplicationTheme {

                NavHost(navController = navController, startDestination = Routes.Home) {

                    composable(Routes.Home) {
                        MyTabScreen(
                            navController = navController,
                            productViewModel = productViewModel,
                            fridgeViewModel = fridgeViewModel,
                            shopListViewModel = shopListViewModel,
                            userSession = userSession,
                            userDao = userDao
                        )
                    }
                    composable(Routes.ShoppingList) {
                        var fabVisible by remember { mutableStateOf(false)}
                        var showDialog by remember { mutableStateOf(false) }
                        var inputText by remember { mutableStateOf("") }
                        var showSnackBar by remember { mutableStateOf(false)}
                        val coroutineScope = rememberCoroutineScope()
                        Scaffold(
                            bottomBar = {BottomBar(navController = navController)},
                            content = { paddingValues ->
                                Column(modifier = Modifier.padding(paddingValues)) {
                                    if (userSession.value.isLoggedIn) {
                                        fabVisible = true
                                        ShopList(
                                            shopListViewModel = shopListViewModel,
                                            userSession = userSession.value,
                                            userDao = userDao,
                                            productViewModel = productViewModel)
                                    }
                                    else {
                                        fabVisible = false
                                        RegisterLoginScreen(
                                            onRegisterClicked = {navController.navigate(Routes.Registration)},
                                            onLoginClicked = {navController.navigate(Routes.Login)}
                                        )
                                    }
                                }

                                if(showSnackBar) {
                                    Snackbar(
                                        action = {
                                            Button(onClick = { showSnackBar = false }) {
                                                Text(text = "OK")
                                            }
                                        }
                                    ) {
                                        Text(text = "Produkt jest już na liście")
                                    }
                                }

                                if(showDialog) {
                                    AlertDialog(
                                        onDismissRequest = {showDialog = false},
                                        shape = RoundedCornerShape(16.dp),
                                        title = { Text(
                                            text = "Dodaj do listy",
                                            color = MaterialTheme.colorScheme.primary) },
                                        text = {
                                            Column {
                                                TextField(
                                                    value = inputText,
                                                    onValueChange = { inputText = it },
                                                    label = { Text(text = "Nazwa produktu") }
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    showDialog = false
                                                    //TODO: try-catch na istnienie elementu w liście
                                                    coroutineScope.launch {
                                                        val product = productViewModel.getProductByName(inputText.lowercase())
                                                        val user = withContext(Dispatchers.IO) {
                                                            userDao.getUserByUsername(userSession.value.username)
                                                        }
                                                        if (product != null) {
                                                            var shopProduct = shopListViewModel.getShoppingItemById(product.id)
                                                            if (shopProduct == null) {
                                                                shopProduct = ShoppingItem(product.id, user!!.id, 1)
                                                                shopListViewModel.insertShoppingItem(shopProduct)
                                                            } else {
                                                                showSnackBar = true
                                                            }
                                                        } else {
                                                            val newProduct = ProductItem(inputText.hashCode(), inputText, null, null)

                                                            runBlocking(Dispatchers.IO) {
                                                                productViewModel.addProduct(
                                                                    newProduct
                                                                )

                                                            }

                                                            withContext(Dispatchers.IO) {
                                                                val shopProduct = ShoppingItem(
                                                                    newProduct.id,
                                                                    user!!.id,
                                                                    1
                                                                )
                                                                //ja nie wiem, jak to inaczej rozwiązać, ciągle były błędy wynikające z tego, że produkt nie dodał się jeszcze do bazy
                                                                //może trzeba jakoś to robić w jednej funkcji operującej na dwóch Dao
                                                                delay(20)
                                                                shopListViewModel.insertShoppingItem(shopProduct)

                                                            }
                                                        }
                                                    }


                                                }
                                            ) {
                                                Text(text = "Dodaj")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(
                                                onClick = {showDialog = false}
                                            ) {
                                                Text(text = "Anuluj")
                                            }
                                        }
                                    )
                                }

                            },
                            floatingActionButton = {
                                if(fabVisible) {
                                    FloatingActionButton(
                                        onClick = {
                                            showDialog = true
                                            inputText = ""
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Dodaj")
                                    }
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                        )
                    }
                    composable(Routes.RegisterLogin) {
                        Scaffold(
                            bottomBar = {BottomBar(navController = navController)},
                            content = { paddingValues ->
                                Column(modifier = Modifier.padding(paddingValues)) {
                                    RegisterLoginScreen(
                                        onRegisterClicked = {navController.navigate(Routes.Registration)},
                                        onLoginClicked = {navController.navigate(Routes.Login)}
                                    )
                                }
                            }
                        )

                    }

                    composable(Routes.Registration) {
                        RegistrationScreen(
                            navController = navController,
                            userDao = userDao
                        )
                    }
                    composable(Routes.Login) {
                        LoginScreen(
                            navController = navController,
                            userDao = userDao,
                            userSession = userSession
                        )
                    }

                    composable((Routes.Profile)) {

                        Scaffold(
                            bottomBar = {BottomBar(navController = navController)},
                            content = { paddingValues ->
                                ProfileScreen(
                                    userSession = userSession.value,
                                    notificationViewModel = notificationViewModel,
                                    paddingValues = paddingValues,
                                    navController = navController
                                )
                            }
                        )
                    }

                    composable(Routes.Credits) {
                        Scaffold(
                            bottomBar = {BottomBar(navController = navController)},
                            content = { paddingValues ->
                                Column(modifier = Modifier.padding(paddingValues)) {
                                    CreditsScreen()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

}



@Composable
fun MyTabScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    fridgeViewModel: FridgeViewModel,
    userSession: MutableState<UserSession>,
    shopListViewModel: ShoppingViewModel,
    userDao: UserDao
) {

    val tabTitles = listOf("Jedzenie", "Kosmetyki")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val userId = remember { mutableIntStateOf(0) }
    val isViewShown = remember { mutableStateOf(false)}

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("ExampleScreen","PERMISSION GRANTED")
        } else {
            Log.d("ExampleScreen","PERMISSION DENIED")
        }
    }
    val context = LocalContext.current


    LaunchedEffect(isViewShown.value) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                // Some works that require permission
                Log.d("ExampleScreen","Code requires permission")
            }
            else -> {
                // Asking for permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        coroutineScope.launch {
            val id = userDao.getUserId(userSession.value.username)
            if (id != null) {
                userId.value = id
            }
        }
    }


    isViewShown.value = true
    var showAddProduct by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabIndex) {
        coroutineScope.launch {
            showAddProduct = false
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController) },
            floatingActionButton = {
                if (!showAddProduct) {
                    FloatingActionButton(
                        onClick = {
                            showAddProduct = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj")
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End, // Pozycja FAB

        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                TabRow(selectedTabIndex) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                if (showAddProduct) {

                    AddProductScreen(
                        navController = navController,
                        productViewModel = productViewModel,
                        fridgeViewModel = fridgeViewModel,
                        category = tabTitles[selectedTabIndex],
                    )
                }
                else {
                    when (selectedTabIndex) {
                        0 -> {
                            ProductList(
                                fridgeViewModel = fridgeViewModel,
                                productViewModel = productViewModel,
                                category = "Jedzenie",
                                shoppingViewModel = shopListViewModel,
                                userId = userId.value,
                                userSession = userSession.value
                            )
                        }
                        1 -> {
                            ProductList(
                                fridgeViewModel = fridgeViewModel,
                                productViewModel = productViewModel,
                                category = "Kosmetyki",
                                shoppingViewModel = shopListViewModel,
                                userId = userId.value,
                                userSession = userSession.value
                            )
                        }
                    }
                }
            }
        },
    )
}




@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Autorzy", Routes.Credits, painterResource(id = R.drawable.baseline_copyright_24)),
        BottomNavItem("Zakupy", Routes.ShoppingList, painterResource(id = R.drawable.baseline_shopping_basket_24)),
        BottomNavItem("Produkty", Routes.Home, painterResource(id = R.drawable.baseline_kitchen_24)),
        BottomNavItem("Profil", Routes.Profile, painterResource(id = R.drawable.baseline_person_24)),
    )
    BottomNavigation(
        elevation = 16.dp,
        backgroundColor = getPrimaryColor(),
        contentColor = getOnPrimaryColor(),

    ) {
        items.forEach { item ->
            val selected = item.route == navController.currentDestination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route)
                },
                icon = {
                    Column(horizontalAlignment = CenterHorizontally) {
                        Icon(
                            painter = item.icon,
                            contentDescription = "",
                            tint = if (selected) Color.White else Color.LightGray
                        )
                        if (selected) {
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                },
            )
        }

    }
}

@Composable
fun DialogWithInput(onConfirm: () -> Unit, onCancel: () -> Unit) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(16.dp),
        title = { Text(
            text = "Enter Input",
            color = MaterialTheme.colorScheme.primary) },
        text = {
            Column {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(text = "Input") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = "Dodaj",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier
                        .background(
                        color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            ) {
                Text(
                    text = "Anuluj",
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}






