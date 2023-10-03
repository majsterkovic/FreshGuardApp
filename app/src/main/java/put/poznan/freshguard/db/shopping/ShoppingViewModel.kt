package put.poznan.freshguard.db.shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import put.poznan.freshguard.db.AppDatabase

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val shoppingDao: ShoppingDao
    private val shoppingRepository: ShoppingRepository
    val allShoppingItems: LiveData<List<ShoppingItem>>

    init {
        val appDatabase = AppDatabase.getDatabase(application)
        shoppingDao = appDatabase.shoppingDao()
        shoppingRepository = ShoppingRepository(shoppingDao)
        allShoppingItems = shoppingRepository.allShoppingItems
    }

    fun insertShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.insertShoppingItem(item)
        }
    }

    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.updateShoppingItem(item)
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.deleteShoppingItem(item)
        }
    }

    suspend fun getShoppingItemsByUserId(userId: Int): List<ShoppingItem> {
        return withContext(Dispatchers.IO) {
            shoppingRepository.getShoppingItemsByUserId(userId)
        }
    }

    suspend fun getShoppingItemById(productId: Int): ShoppingItem? {
        return withContext(Dispatchers.IO) {
            shoppingRepository.getShoppingItemById(productId)
        }
    }


}

