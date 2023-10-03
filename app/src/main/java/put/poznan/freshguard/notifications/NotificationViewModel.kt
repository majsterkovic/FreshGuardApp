package put.poznan.freshguard.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.WorkManager
import put.poznan.freshguard.db.PreferenceStore
import put.poznan.freshguard.db.fridge.FridgeViewModel
import put.poznan.freshguard.db.products.ProductViewModel

class NotificationViewModel(application: Application, private val fridgeViewModel: FridgeViewModel, private val productViewModel: ProductViewModel) : AndroidViewModel(application) {

    private val app = application
    private val prefStore = PreferenceStore(application)

    fun scheduleReminderNotification(hourOfDay: Int, minute: Int) {
        prefStore.setReminderTime(hourOfDay, minute)
        ReminderNotificationWorker.schedule(app, hourOfDay, minute, fridgeViewModel, productViewModel)
    }

    fun getReminderTime() = prefStore.getReminderTime()

    fun cancelReminderNotification() {
        prefStore.cancelReminder()
        WorkManager.getInstance(app).cancelAllWorkByTag("reminder_worker")
    }

    /**
     * This sets the default time at the first launch of the app
     */
    fun checkAndSetDefaultReminder() {
        if (!prefStore.isDefaultReminderSet()) {
            scheduleReminderNotification(10, 0) //wartosci defaultowe jeśli użytkownik nie zmieni
            prefStore.saveDefaultReminderIsSet()
        }
    }
}