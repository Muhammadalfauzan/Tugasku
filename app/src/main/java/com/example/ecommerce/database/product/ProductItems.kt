package com.example.ecommerce.database.product

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.ecommerce.database.ProductTypeConverter


@Entity(tableName = "product_table")

class ProductItems(
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val image: String,
    val price: Double,
    val ratingCount: Int,
    val ratingRate: Double
)