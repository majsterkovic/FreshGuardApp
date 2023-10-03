package put.poznan.freshguard.db.shopping

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping")
    fun getAll(): LiveData<List<ShoppingItem>>

    @Insert
    fun insert(item: ShoppingItem)

    @Update
    fun update(item: ShoppingItem)

    @Delete
    fun delete(item: ShoppingItem)
    @Query("SELECT * FROM shopping WHERE userId = :userId")
    suspend fun getShoppingItemsByUserId(userId: Int): List<ShoppingItem>

    @Query("SELECT * FROM shopping WHERE productId = :productId")
    suspend fun getShoppingItemById(productId: Int): ShoppingItem?

}
