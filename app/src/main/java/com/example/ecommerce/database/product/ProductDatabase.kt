package com.example.ecommerce.database.product


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ecommerce.database.ProductTypeConverter

@Database(
    entities = [ProductItems::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(ProductTypeConverter::class)
abstract class ProductDatabase : RoomDatabase() {

    abstract fun productDao() : ProductDao
}