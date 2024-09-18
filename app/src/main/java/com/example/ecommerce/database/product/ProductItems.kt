package com.example.ecommerce.database.product

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.ecommerce.data.apimodel.Product
import com.example.ecommerce.database.ProductTypeConverter


@Entity(tableName = "product_table")
@TypeConverters(ProductTypeConverter::class) // Ensure TypeConverter is referenced
class ProductItems( // Change to a data class

    var listProductResponse: Product // Use Product and convert it with TypeConverter
) {
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0
}