package put.poznan.freshguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import put.poznan.freshguard.Routes
import put.poznan.freshguard.db.users.UserDao
import put.poznan.freshguard.db.users.UserSession
import put.poznan.freshguard.db.users.hashPassword
import put.poznan.freshguard.ui.theme.getBackgroundColor
import put.poznan.freshguard.ui.theme.getOnBackgroundColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun LoginScreen(
    navController: NavHostController,
    userDao: UserDao,
    userSession: MutableState<UserSession>
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(color = getBackgroundColor()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Logowanie",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.h4.fontSize,
                    letterSpacing = 0.15.sp,
                    textAlign = TextAlign.Center,
                    color = getOnBackgroundColor()
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Login") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        val hashedPassword = hashPassword(password)
                        val user = userDao.getUser(username, hashedPassword)
                        if (user != null) {
                            userSession.value = UserSession(username, true)
                            navController.navigate(Routes.ShoppingList)
                        } else {
                            showError = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zaloguj")
            }
            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Snackbar(
                    content = { Text("Niepoprawna nazwa użytkownika lub hasło") },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}
