package put.poznan.freshguard.db.fridge

import kotlinx.coroutines.flow.Flow


class FridgeRepository(private val productDao: FridgeDao) {
    val readAllData: Flow<List<FridgeItem>> = productDao.getAllProducts()

    suspend fun addProduct(product: FridgeItem) {
        productDao.insert(product)
    }

    suspend fun delete(product: FridgeItem) {
        productDao.delete(product)
    }

    suspend fun update(product: FridgeItem) {
        productDao.update(product)
    }
}
