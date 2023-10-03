package put.poznan.freshguard.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import put.poznan.freshguard.db.fridge.FridgeDao
import put.poznan.freshguard.db.fridge.FridgeItem
import put.poznan.freshguard.db.products.ProductDao
import put.poznan.freshguard.db.products.ProductItem
import put.poznan.freshguard.db.shopping.ShoppingDao
import put.poznan.freshguard.db.shopping.ShoppingItem
import put.poznan.freshguard.db.users.User
import put.poznan.freshguard.db.users.UserDao

@Database(entities = [ProductItem::class, User::class, ShoppingItem::class, FridgeItem::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun shoppingDao(): ShoppingDao

    abstract fun fridgeDao(): FridgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
