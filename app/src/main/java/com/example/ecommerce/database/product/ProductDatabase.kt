package com.example.ecommerce.database.product


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [ProductItems::class],
    version = 1,
    exportSchema = false
)


abstract class ProductDatabase : RoomDatabase() {

    abstract fun productDao() : ProductDao
}