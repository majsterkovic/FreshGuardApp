package put.poznan.freshguard.db.shopping

import androidx.lifecycle.LiveData

class ShoppingRepository(private val shoppingDao: ShoppingDao) {
    val allShoppingItems: LiveData<List<ShoppingItem>> = shoppingDao.getAll()

    suspend fun insertShoppingItem(item: ShoppingItem) {
        shoppingDao.insert(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        shoppingDao.update(item)
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        shoppingDao.delete(item)
    }

    suspend fun getShoppingItemsByUserId(userId: Int): List<ShoppingItem> {
        return shoppingDao.getShoppingItemsByUserId(userId)
    }

    suspend fun getShoppingItemById(productId: Int): ShoppingItem? {
        return shoppingDao.getShoppingItemById(productId)
    }

}
