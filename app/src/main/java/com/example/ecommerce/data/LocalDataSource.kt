package com.example.ecommerce.data


import com.example.ecommerce.database.product.ProductDao
import com.example.ecommerce.database.product.ProductItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val productDao: ProductDao,
    /*private val cartDao: CartDao*/
) {
    /** MENU **/
    fun readProduct(): Flow<List<ProductItems>> {
        return productDao.readProduct()
    }

    suspend fun insertProduct(product:ProductItems ){
        productDao.insertProduct(product)
    }


}