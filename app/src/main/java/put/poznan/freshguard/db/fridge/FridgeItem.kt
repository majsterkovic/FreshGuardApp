package put.poznan.freshguard.db.fridge


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import put.poznan.freshguard.db.products.ProductItem
import java.util.Date

@TypeConverters(DateConverter::class)
@Entity(tableName = "fridge_table",
    primaryKeys = ["productId", "expirationDate"],
    foreignKeys = [
        ForeignKey(
            entity = ProductItem::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class FridgeItem(
    val productId: Int,
    val expirationDate: Date,
    val quantity: Int,
    val category: String,
    val approximateDate: Int = 0
) {

}
