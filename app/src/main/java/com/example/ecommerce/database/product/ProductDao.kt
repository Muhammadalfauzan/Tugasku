package com.example.ecommerce.database.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(menu: ProductItems)

    @Query("SELECT * FROM product_table ORDER BY id ASC")
    fun readProduct(): Flow<List<ProductItems>>


    @Query("SELECT * FROM product_table WHERE LOWER(title) LIKE LOWER('%' || :label || '%')")
    fun searchProductsByLabel(label: String): List<ProductItems>
}