package put.poznan.freshguard.db.shopping

import androidx.room.Entity
import androidx.room.ForeignKey
import put.poznan.freshguard.db.products.ProductItem
import put.poznan.freshguard.db.users.User

@Entity(tableName = "shopping",
    primaryKeys = ["productId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductItem::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShoppingItem(
    val productId: Int,
    val userId: Int,
    var quantity: Int,
    val checked: Boolean = false,
)
