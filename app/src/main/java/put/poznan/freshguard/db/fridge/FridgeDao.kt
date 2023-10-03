package put.poznan.freshguard.db.fridge

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface FridgeDao {
    @Query("SELECT * FROM fridge_table")
    fun getAllProducts(): Flow<List<FridgeItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: FridgeItem)

    @Delete
    suspend fun delete(product: FridgeItem)

    @Update
    suspend fun update(product: FridgeItem)
}