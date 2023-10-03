package put.poznan.freshguard.notifications

import android.app.Application
import android.content.Context
import android.icu.util.Calendar
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.work.CoroutineWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import put.poznan.freshguard.db.fridge.FridgeViewModel
import put.poznan.freshguard.db.products.ProductViewModel
import put.poznan.freshguard.ui.dateAfterDays
import java.util.Date
import kotlin.coroutines.coroutineContext

class ReminderNotificationWorker(private val appContext: Context, workerParameters: WorkerParameters) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            val stringBuilder = StringBuilder()
            var count = 0
            val items = fridgeViewModel!!.readAllData.value

            Log.e("Notif", "${items?.size}")
            if (items != null) {
                for (elem in items) {
                    Log.e("Notif", "${elem.productId}")
                    if (elem.approximateDate == 0) {
                        if (elem.expirationDate.before(dateAfterDays(3))) {
                            if (count < 3) {
                                val name = productViewModel!!.getProductById(elem.productId)!!.name
                                Log.e("Notif", "name: $name")
                                stringBuilder.append(if (count == 0) name else ", $name")
                            }
                            count += 1
                        }
                    }
                }
            }

            Log.e("Notif", "$count")

            if (count > 0) {
                var expiringItemsString = stringBuilder.toString()
                if (count == 4) {
                    expiringItemsString += " i jeszcze jeden"
                }
                if (count > 4) {
                    expiringItemsString += " i ${count-3} inne"
                }
                Log.e("Notif", "expiringItemsString")
                NotificationHandler.createReminderNotification(appContext, expiringItemsString)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }


    companion object {

        private var fridgeViewModel: FridgeViewModel? = null
        private var productViewModel: ProductViewModel? = null
        fun schedule(appContext: Context, hourOfDay: Int, minute: Int, fViewModel: FridgeViewModel, pViewModel: ProductViewModel) {
            Log.e("Notif", "Reminder scheduling request received for $hourOfDay:$minute")
            fridgeViewModel = fViewModel
            productViewModel = pViewModel
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }

            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }


            val notificationRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(24, TimeUnit.HOURS)
                .addTag("reminder_worker")
                .setInitialDelay(target.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS).build()
            WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(
                    "reminder_notification_work",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    notificationRequest
                )
        }
    }
}