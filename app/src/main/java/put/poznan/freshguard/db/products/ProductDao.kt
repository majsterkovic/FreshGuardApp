package put.poznan.freshguard.db.products


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface ProductDao {
    @Query("SELECT * FROM product_table")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM product_table WHERE barcode = :barcode")
    fun getProductByBarcode(barcode: String): List<ProductItem>

    @Query("SELECT * FROM product_table WHERE id = :id")
    fun getProductById(id: Int): ProductItem?

    @Query("Select * FROM product_table WHERE name LIKE :name")
    fun getProductByName(name: String): ProductItem?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: ProductItem)

    @Delete
    suspend fun delete(product: ProductItem)
}
