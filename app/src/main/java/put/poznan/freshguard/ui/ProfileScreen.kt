package put.poznan.freshguard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chargemap.compose.numberpicker.FullHours
import com.chargemap.compose.numberpicker.Hours
import com.chargemap.compose.numberpicker.HoursNumberPicker
import put.poznan.freshguard.Routes
import put.poznan.freshguard.db.users.UserSession
import put.poznan.freshguard.notifications.NotificationViewModel
import put.poznan.freshguard.ui.theme.getOnPrimaryColor
import put.poznan.freshguard.ui.theme.getOnSurfaceColor
import put.poznan.freshguard.ui.theme.getPrimaryColor
import put.poznan.freshguard.ui.theme.getSecondaryColor

@Composable
fun ProfileScreen(userSession: UserSession, notificationViewModel: NotificationViewModel, paddingValues: PaddingValues, navController: NavController) {

    val reminderTime = notificationViewModel.getReminderTime()
    var pickerValue by remember { mutableStateOf<Hours>(FullHours(reminderTime.first, reminderTime.second)) }
    var isLoggedIn by remember { mutableStateOf(userSession.isLoggedIn)}


    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text =
            if (isLoggedIn)
                "Zalogowano jako ${userSession.username}"
            else
                "Nie zalogowano",
            style = MaterialTheme.typography.headlineMedium,
            color = getOnSurfaceColor(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = {
            if (isLoggedIn) {
                userSession.isLoggedIn = false
                isLoggedIn = false
            } else {
                navController.navigate(Routes.RegisterLogin)
            }
        }) {
            Text(
                text =
                if (isLoggedIn)
                    "Wyloguj"
                else
                    "Zaloguj")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Wybierz godzinę powiadomień:",
            style = MaterialTheme.typography.headlineSmall,
            color = getOnSurfaceColor()
        )
        Spacer(modifier = Modifier.height(16.dp))
        HoursNumberPicker(
            leadingZero = false,
            value = pickerValue,
            modifier = Modifier.padding(16.dp),
            onValueChange = {
                pickerValue = it
            },
            hoursDivider = {
                Text(
                    modifier = Modifier.size(24.dp),
                    textAlign = TextAlign.Center,
                    text = ":"
                )
            },
            dividersColor = getSecondaryColor(),
            textStyle = TextStyle(color = getPrimaryColor()) // Zmień kolor zgodnie z preferencjami
        )

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = {
            notificationViewModel.cancelReminderNotification()
            notificationViewModel.scheduleReminderNotification(pickerValue.hours, pickerValue.minutes)
        }) {
            Text(text = "Zapisz")
        }
    }
}
