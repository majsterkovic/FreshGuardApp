package put.poznan.freshguard.db.products


import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val readAllData: Flow<List<ProductItem>> = productDao.getAllProducts()

    suspend fun addProduct(product: ProductItem) {
        productDao.insert(product)
    }

    suspend fun delete(product: ProductItem) {
        productDao.delete(product)
    }

     fun getProductByBarcode(barcode: String): List<ProductItem> {
        return productDao.getProductByBarcode(barcode)
    }

    fun getProductByName(name: String): ProductItem? {
        return productDao.getProductByName(name)
    }
    fun getProductById(id: Int): ProductItem? {
        return productDao.getProductById(id)
    }
}
