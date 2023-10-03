package put.poznan.freshguard.db.products


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import put.poznan.freshguard.db.AppDatabase


class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepository
    private val productDao: ProductDao
    val readAllData: LiveData<List<ProductItem>>

    init {
        val appDatabase = AppDatabase.getDatabase(application)
        productDao = appDatabase.productDao()
        repository = ProductRepository(productDao)
        readAllData = repository.readAllData.asLiveData()
    }

    fun addProduct(food: ProductItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addProduct(food)
        }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(product)
        }
    }

    suspend fun getProductByBarcode(barcode: String): List<ProductItem> {
        return withContext(Dispatchers.IO) {
            repository.getProductByBarcode(barcode)
        }
    }

    suspend fun getProductById(id: Int): ProductItem? {
        return withContext(Dispatchers.IO) {
            repository.getProductById(id)
        }
    }

    suspend fun getProductByName(name: String): ProductItem? {
        return withContext(Dispatchers.IO) {
            repository.getProductByName(name)
        }
    }
}
