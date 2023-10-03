package put.poznan.freshguard.db.fridge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import put.poznan.freshguard.db.AppDatabase


class FridgeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FridgeRepository
    private val fridgeDao: FridgeDao
    val readAllData: LiveData<List<FridgeItem>>

    init {
        val appDatabase = AppDatabase.getDatabase(application)
        fridgeDao = appDatabase.fridgeDao()
        repository = FridgeRepository(fridgeDao)
        readAllData = repository.readAllData.asLiveData()
    }

    fun addProduct(food: FridgeItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addProduct(food)
        }
    }

    fun deleteProduct(product: FridgeItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(product)
        }
    }


}